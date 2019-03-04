package com.cloud.finance.third.anyinfu.service;

import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.anyinfu.utils.AYFUtil;
import com.cloud.finance.third.hangzhou.utils.XmlUtil;
import com.cloud.finance.third.hankou.utils.HKUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.cloud.finance.common.utils.PostUtils.xmlPost;

@Service("AnyinfuCashService")
public class AnyinfuCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(AnyinfuCashService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("...[anyinfu cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        // 鉴权
        // 应用ID
        String appid = thirdChannelDto.getAppId();
        // 商户秘钥
        String key = thirdChannelDto.getPayMd5Key();
        // 随机字符串
        String random = HKUtil.getRandomString(20);
        Map<String, String> params = new HashMap<>();
        params.put("appid", appid);
        params.put("random", random);

        Map<String, String> map = new HashMap<>();

        // 商户号
        String mch_id = thirdChannelDto.getMerchantId();
        // 商户订单号
        String out_trade_out = shopRecharge.getRechargeNo();
        // 总金额
        String total_fee = String.valueOf((int) (shopRecharge.getRechargeMoney() * 100));
        // 提现类别
        String withdraw_type = thirdChannelDto.getCashMd5Key();
        // 卡号
        String card_no = shopRecharge.getBankNo();
        // 银行编码
        String bank_code = shopRecharge.getBankCode();
        ApiResponse response = sysBankProvider.toChannelCode(bank_code, thirdChannelDto.getId());
        logger.info("第三方银行：" + response);
        // 银行联行号
        String bank_no = "2123123";
        // 收款人
        String payee = shopRecharge.getBankAccount();
        // 收款人身份证号
        String id_card_no = "232323322333312312";
        // 手机号
        String mobile = "15004764771";
        // 随机操作符
        String nonce_str = HKUtil.getRandomString(32);
        // 代付通道编码
        String sp_id = thirdChannelDto.getAppKey();
        // 省份代码
        String province = "11";
        // 城市代码
        String city = "1000";
        // 银行名称
        String card_bank;
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(bank_code);
        if((ResponseCode.Base.SUCCESS.getCode() + "").equals(apiResponse.getCode())){
            card_bank = apiResponse.getData().toString();
        }else{
            logger.error("【代付请求失败】不支持的银行");
            cashRespData.setMsg("【代付请求失败】不支持的银行");
            return cashRespData;
        }
        // 支行名称
        String card_sub_bank = shopRecharge.getBankSubbranch();
        // 渠道编码
        String channel_type = thirdChannelDto.getQueryUrl();

        map.put("mch_id", mch_id);
        map.put("out_trade_no", out_trade_out);
        map.put("total_fee", total_fee);
        map.put("withdraw_type", withdraw_type);
        map.put("card_no", card_no);
        map.put("bank_code", response.getData().toString());
        map.put("bank_no", bank_no);
        map.put("payee", payee);
        map.put("id_card_no", id_card_no);
        map.put("mobile", mobile);
        map.put("nonce_str", nonce_str);
        map.put("sp_id", sp_id);
        map.put("province", province);
        map.put("city", city);
        map.put("card_bank", card_bank);
        map.put("card_sub_bank", card_sub_bank);
        map.put("channel_type", channel_type);

        // 签名
        logger.info("[anyinfu before sign msg]: " + map);
        try {
            String sign = HKUtil.generateMd5Sign(map, key);

            map.put("sign", sign);

            String xmlStr = ASCIISortUtil.buildXmlSign(map);

            String token = AYFUtil.getToken(params, key, thirdChannelDto.getAdminUrl());

            // 交易请求地址
            String payURL = thirdChannelDto.getPayUrl() + "?token=" + token;
            String contentType = "application/xml; charset=utf-8";
            String jsonStr = xmlPost(payURL, xmlStr, contentType);
            logger.info("[anyinfu cash post result]: " + jsonStr);

            Map<String, String> respMap = XmlUtil.xmlToMap(jsonStr);
            logger.info("[anyinfu cash success result]: " + respMap);
            if (respMap == null) {
                cashRespData.setStatus("false");
                cashRespData.setMsg("代付失败 ====> 代付请求结果为空");
                return cashRespData;
            }
            if ("0".equals(respMap.get("status")) && "0".equals(respMap.get("result_code"))) {
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);

                logger.info("【通道代付成功】---- " + respMap.get("message"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[ " + respMap.get("message") + " ]");
            } else {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.error("【通道代付失败】----> 代付结果状态错误：" + respMap.get("status") + "--->" + respMap.get("message"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("message"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            cashRespData.setMsg("代付异常");
            cashRespData.setStatus("false");
            shopRecharge.setRechargeStatus(4);
            shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
            shopRecharge.setThirdChannelRespMsg("代付异常");
            shopRechargeService.rechargeFail(shopRecharge);
            return cashRespData;
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
