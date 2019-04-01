package com.cloud.finance.third.ainong.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.HttpClientUtil;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.ainong.utils.AES;
import com.cloud.finance.third.ainong.utils.Base64;
import com.cloud.finance.third.ainong.vo.HeadReqData;
import com.cloud.finance.third.ainong.vo.PayForReq;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("AinongCashService")
public class AinongCashService implements BaseCashService {
    private static Logger logger = LoggerFactory.getLogger(AinongCashService.class);

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

        Date time = new Date();
        //请求参数
        //请求头
        //合作方标识号
        String partnerNo = thirdChannelDto.getMerchantId();
        //版本
        String version = "1.0.0";
        //字符集
        String charset = "UTF-8";
        //合作方类型
        String  partnerType = "OUTER";
        //交易代码
        String txnCode = "102002";
        //交易跟踪号
        String traceId = shopRecharge.getRechargeNo();
        //请求日期  格式为yyyyMMdd
        String reqDate = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_11);
        //请求时间  格式为yyyyMMddHHmmss
        String reqTime = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_18);

        //交易请求参数
        //交易金额  单位分
        String amount = String.valueOf((int)((shopRecharge.getRechargeMoney() - shopRecharge.getRechargeRateMoney()) * 100));

        ApiResponse response = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        String bank_name = response.getData().toString();

        /*** 请求参数 ***/
        HeadReqData headReqDTO = new HeadReqData();
        headReqDTO.setPartnerNo(partnerNo);
        headReqDTO.setVersion(version);
        headReqDTO.setCharset(charset);
        headReqDTO.setPartnerType(partnerType);
        headReqDTO.setTxnCode(txnCode);
        headReqDTO.setTraceId(traceId);
        headReqDTO.setReqDate(reqDate);
        headReqDTO.setReqTime(reqTime);
        PayForReq req = new PayForReq();
        req.setAmount(amount);
        req.setBankAccountName(shopRecharge.getBankAccount());
        req.setBankAccountNo(shopRecharge.getBankNo());
        req.setBankAccountType("PRIVATE");
        req.setBankName(bank_name);
//        req.setBankChannelNo("03080000");
        req.setProvince(shopRecharge.getProvince());
        req.setCity(shopRecharge.getCity());
        req.setPayForType(thirdChannelDto.getAppKey()); //T0 代付
        req.setHead(headReqDTO);

        //加密
        String plainText = JSONObject.toJSONString(req);
        String encryptData = com.cloud.finance.third.ainong.utils.Base64.encode(AES.encode(plainText, thirdChannelDto.getPayMd5Key()));
        logger.info("加密结果：" + encryptData);
        // 签名
        String signData = DigestUtils.sha1Hex(plainText + thirdChannelDto.getCashMd5Key());
        logger.info("[ailong sign msg]:" + signData);

        Map<String, String> params = new HashMap<>();
        params.put("encryptData", encryptData);
        params.put("signData", signData);
        params.put("partnerNo", partnerNo);
        logger.info("pay post params == >  " + params);

        try {
            String jsonStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
            logger.info("代付结果：" + jsonStr);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道代付请求结果为空】");
                cashRespData.setMsg("通道代付请求结果为空");
                return cashRespData;
            }

            Map<Object, Object> encryptMap = MapUtils.json2Map(jsonStr);
            logger.info("转换结果：" + encryptData);
            String plainStr = AES.decode(Base64.decode(encryptMap.get("encryptData").toString()), thirdChannelDto.getPayMd5Key());
            logger.info("解密结果：" + plainStr);

            Map<Object, Object> respMap = MapUtils.json2Map(plainStr);
            logger.info("参数map：" + respMap);

            if (!respMap.containsKey("orderStatus")) {
                logger.info("【通道代付失败】----> 查询结果状态错误：" + respMap.get("orderStatus"));
                Map<Object, Object> failRes = MapUtils.json2Map(respMap.get("head"));
                logger.info("继续转换：" + failRes);
                cashRespData.setMsg(failRes.get("respMsg").toString());
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                return cashRespData;
            }
            String status = respMap.get("orderStatus").toString();
            logger.info("代付返回状态码：" + status);
            if (status.equals("01") || status.equals("04")) {
                logger.info("【通道代付成功】");
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(plainStr);
                shopRecharge.setCompleteTime(new Date());
                cashRespData.setMsg("代付成功");
                shopRechargeService.rechargeSuccess(shopRecharge);
            } else if (status.equals("02")){
                shopRecharge.setRechargeStatus(4);
                Map<Object, Object> failRes = MapUtils.json2Map(respMap.get("head"));
                logger.info("继续转换：" + failRes);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(failRes.toString());
                shopRechargeService.rechargeFail(shopRecharge);
                logger.error("【通道代付失败】----> 代付结果状态错误：" + status + "--->" + failRes.get("respMsg"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(failRes.get("respMsg").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
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
