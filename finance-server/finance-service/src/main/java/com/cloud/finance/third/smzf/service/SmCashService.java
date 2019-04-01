package com.cloud.finance.third.smzf.service;

import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.HttpClientUtil;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.hangzhou.utils.XmlUtil;
import com.cloud.finance.third.hankou.utils.HKUtil;
import com.cloud.finance.third.smzf.utils.SMUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("SmCashService")
public class SmCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(SmCashService.class);

    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("[ainong cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        String tranCode = thirdChannelDto.getAppKey();
        String bankCode = shopRecharge.getBankCode();
        logger.info("bankCode: " + bankCode);
        ApiResponse response = sysBankProvider.toChannelCode(bankCode, thirdChannelDto.getId());
        if (!response.getCode().equals(ResponseCode.Base.SUCCESS + "")) {
            logger.error("【通道不支持的该银行的支付请求】-------系统银行编码：" + bankCode);
            cashRespData.setMsg("通道不支持的该银行的支付请求");

            shopRecharge.setRechargeStatus(4);
            shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
            shopRecharge.setThirdChannelRespMsg("通道不支持的该银行的支付请求");
            shopRechargeService.rechargeFail(shopRecharge);

            return cashRespData;
        }
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(bankCode);

        String charset = "utf-8";
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sBuilder.append("<merchant>");
        sBuilder.append("<head>");
        sBuilder.append("<version>1.0.0</version>");
        sBuilder.append("<msgType>01</msgType>");
        sBuilder.append("<reqDate>").append(DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18)).append("</reqDate>");
        sBuilder.append("</head>");
        sBuilder.append("<body>");
        sBuilder.append("<merchantCode>").append(thirdChannelDto.getMerchantId()).append("</merchantCode>");
        sBuilder.append("<accNo>").append(shopRecharge.getBankNo()).append("</accNo>");
        sBuilder.append("<accName>").append(shopRecharge.getBankAccount()).append("</accName>");
        sBuilder.append("<bankType>").append(response.getData()).append("</bankType>");
        sBuilder.append("<bankName>").append(apiResponse.getData()).append("</bankName>");
        sBuilder.append("<bsBankType>123123</bsBankType>");
        sBuilder.append("<bsBankName>").append(shopRecharge.getBankSubbranch()).append("</bsBankName>");
        sBuilder.append("<drawAmount>").append(shopRecharge.getRechargeMoney() - shopRecharge.getRechargeRateMoney()).append("</drawAmount>");
        sBuilder.append("</body>");
        sBuilder.append("</merchant>");
        try {
            String plainXML = sBuilder.toString();
            logger.info("【代付请求参数：】" + plainXML);
            byte[] plainBytes = plainXML.getBytes(charset);
            String keyStr = HKUtil.getRandomString(16);
            byte[] keyBytes = keyStr.getBytes(charset);
            String encryptData = new String(Base64.encodeBase64((SMUtil
                    .AESEncrypt(plainBytes, keyBytes, "AES",
                            "AES/ECB/PKCS5Padding", null))), charset);
            PublicKey yhPubKey = SMUtil.getRSAPublicKeyByFileSuffix();
            final PrivateKey hzfPriKey = SMUtil.getRSAPrivateKeyByFileSuffix("pem", null, "RSA");
            String signData = new String(Base64.encodeBase64(SMUtil
                    .digitalSign(plainBytes, hzfPriKey, "SHA1WithRSA")),
                    charset);
            String encryptKey = new String(Base64.encodeBase64(SMUtil
                    .RSAEncrypt(keyBytes, yhPubKey, 2048, 11,
                            "RSA/ECB/PKCS1Padding")), charset);
            Map<String, String> params = new HashMap<>();
            params.put("encryptData", encryptData);
            params.put("encryptKey", encryptKey);
            params.put("cooperator", thirdChannelDto.getAppId());
            params.put("signData", signData);
            params.put("tranCode", tranCode);
            params.put("callBack", thirdChannelDto.getNotifyUrl());
            params.put("reqMsgId", shopRecharge.getRechargeNo());

            String respStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
            logger.info("返回报文[{}]",new Object[]{respStr});
            JSONObject jsonObject = JSONObject.fromObject(respStr);
            String resEncryptData = jsonObject.getString("encryptData");
            String resEncryptKey = jsonObject.getString("encryptKey");
            byte[] decodeBase64KeyBytes = Base64.decodeBase64(resEncryptKey
                    .getBytes(charset));
            // 解密encryptKey得到merchantAESKey
            byte[] merchantAESKeyBytes = SMUtil.RSADecrypt(
                    decodeBase64KeyBytes, hzfPriKey, 2048, 11,
                    "RSA/ECB/PKCS1Padding");
            // 使用base64解码商户请求报文
            byte[] decodeBase64DataBytes = Base64.decodeBase64(resEncryptData
                    .getBytes(charset));
            // 用解密得到的merchantAESKey解密encryptData
            byte[] merchantXmlDataBytes = SMUtil.AESDecrypt(decodeBase64DataBytes,
                    merchantAESKeyBytes, "AES", "AES/ECB/PKCS5Padding", null);
            String resXml = new String(merchantXmlDataBytes,charset);
            logger.info("resXml[{}]",new Object[]{resXml});
            Map<String, String> respMap = SMUtil.readStringXmlOut(resXml);
            logger.info("resMap: " + respMap);
            String status = respMap.get("respType");
            String respMsg = respMap.get("respMsg");
            if (status.equals("S") || status.equals("R")) {
                // 成功
                logger.info("【通道代付成功】");
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功");
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(respMsg);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);
            } else {
                // 失败
                shopRecharge.setRechargeStatus(4);

                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(respMap.get("respMsg"));
                shopRechargeService.rechargeFail(shopRecharge);
                logger.error("【通道代付失败】----> 代付结果状态错误：" + status + "--->" + respMsg);

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMsg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return cashRespData;
    }

    @Override
    public CashRespData queryCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {
        return null;
    }

    @Override
    public CashRespData adminApplyCash(CashReqData cashReqData, ThirdChannelDto thirdChannelDto) {
        return null;
    }
}
