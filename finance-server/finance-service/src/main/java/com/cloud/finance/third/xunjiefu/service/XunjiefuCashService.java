package com.cloud.finance.third.xunjiefu.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.jinxin.utils.MD5Util;
import com.cloud.finance.third.jinxin.utils.PayUtil;
import com.cloud.finance.third.xunjiefu.utils.XJFSignUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("XunjiefuCashService")
public class XunjiefuCashService implements BaseCashService {
    private static Logger logger = LoggerFactory.getLogger(XunjiefuCashService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {
        logger.info("...[xunjiefu cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        //请求参数
        String version="1.0.0";     //版本号
        String transType = "PROXY_PAY"; //业务类型
        String productId = thirdChannelDto.getAppId();  //产品类型  8001, 8002
        String merNo = thirdChannelDto.getMerchantId();//商户号
        String orderDate = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_11);//订单日期
        String orderNo = shopRecharge.getRechargeNo();
        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();//通知地址
        String transAmt = SafeComputeUtils.numberFormate2(SafeComputeUtils.multiply(
                SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney()), 100D));
        String commodityName = "";//产品名称
        String cardName = shopRecharge.getBankAccount();
        String cardNo = shopRecharge.getBankNo();

        Map<String, String> params = new HashMap<>();
        if(StringUtils.isNotBlank(version)) {
            params.put("version", version);
        }
        if(StringUtils.isNotBlank(transType)) {
            params.put("transType", transType);
        }
        if(StringUtils.isNotBlank(productId)) {
            params.put("productId", productId);
        }
        if(StringUtils.isNotBlank(merNo)) {
            params.put("merNo", merNo);
        }
        if(StringUtils.isNotBlank(orderDate)) {
            params.put("orderDate", orderDate);
        }
        if(StringUtils.isNotBlank(orderNo)) {
            params.put("orderNo", orderNo);
        }
        if(StringUtils.isNotBlank(notifyUrl)) {
            params.put("notifyUrl", notifyUrl);
        }
        if(StringUtils.isNotBlank(commodityName)) {
            params.put("commodityName", commodityName);
        }
        if(StringUtils.isNotBlank(cardName)) {
            params.put("cardName", cardName);
        }
        if(StringUtils.isNotBlank(transAmt)) {
            params.put("transAmt", transAmt);
        }
        if(StringUtils.isNotBlank(cardNo)) {
            params.put("cardNo", cardNo);
        }

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", "");
        logger.info("[xunjiefu before sign msg]:"+ stringSignTemp );
        if(StringUtils.isBlank(stringSignTemp)){
            logger.error("【支付请求失败】签名为空");
            cashRespData.setStatus("签名为空");

            return cashRespData;
        }
        String signStr = XJFSignUtil.doSign(stringSignTemp);
        logger.info("[xunjiefu sign msg]:"+signStr);

        params.put("signature", signStr);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【通道代付请求请求结果为空】");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash post result == > "+ jsonObject.toJSONString());

            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("0000".equalsIgnoreCase(respMap.get("respCode")) || "P000".equalsIgnoreCase(respMap.get("respCode"))
                    || "P999".equalsIgnoreCase(respMap.get("respCode"))){
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);

                logger.info("【通道代付成功】---- " + respMap.get("respDesc"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");

            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("respCode") + "--->" + respMap.get("respDesc"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("respDesc"));
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
        logger.info("...[xunjiefu admin cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        //请求参数
        String version="1.0.0";     //版本号
        String transType = "PROXY_PAY"; //业务类型
        String productId = "8001";  //产品类型
        String merNo = thirdChannelDto.getMerchantId();//商户号
        String orderDate = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_11);//订单日期
        String orderNo = "S" + DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13) +"0";
        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();//通知地址
        String transAmt = SafeComputeUtils.numberFormate2(SafeComputeUtils.multiply(cashReqData.getAmount(), 100D));
        String commodityName = "";//产品名称
        String cardName = cashReqData.getBankAccount();
        String cardNo = cashReqData.getBankNo();

        Map<String, String> params = new HashMap<>();
        if(StringUtils.isNotBlank(version)) {
            params.put("version", version);
        }
        if(StringUtils.isNotBlank(transType)) {
            params.put("transType", transType);
        }
        if(StringUtils.isNotBlank(productId)) {
            params.put("productId", productId);
        }
        if(StringUtils.isNotBlank(merNo)) {
            params.put("merNo", merNo);
        }
        if(StringUtils.isNotBlank(orderDate)) {
            params.put("orderDate", orderDate);
        }
        if(StringUtils.isNotBlank(orderNo)) {
            params.put("orderNo", orderNo);
        }
        if(StringUtils.isNotBlank(notifyUrl)) {
            params.put("notifyUrl", notifyUrl);
        }
        if(StringUtils.isNotBlank(commodityName)) {
            params.put("commodityName", commodityName);
        }
        if(StringUtils.isNotBlank(cardName)) {
            params.put("cardName", cardName);
        }
        if(StringUtils.isNotBlank(transAmt)) {
            params.put("transAmt", transAmt);
        }
        if(StringUtils.isNotBlank(cardNo)) {
            params.put("cardNo", cardNo);
        }

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", "");
        logger.info("[xunjiefu before sign msg]:"+ stringSignTemp );
        if(StringUtils.isBlank(stringSignTemp)){
            logger.error("【支付请求失败】签名为空");
            cashRespData.setStatus("签名为空");

            return cashRespData;
        }
        String signStr = XJFSignUtil.doSign(stringSignTemp);
        logger.info("[xunjiefu sign msg]:"+signStr);

        params.put("signature", signStr);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【通道代付请求请求结果为空】");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash post result == > "+ jsonObject.toJSONString());

            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("0000".equalsIgnoreCase(respMap.get("respCode"))){
                logger.info("【通道代付成功】---- " + respMap.get("respDesc"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");

            }else if("P000".equalsIgnoreCase(respMap.get("respCode")) || "P999".equalsIgnoreCase(respMap.get("respCode"))){
                logger.info("【通道代付受理成功】---- " + respMap.get("respDesc"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");
            }else{
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("respCode") + "--->" + respMap.get("respDesc"));
                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("respDesc"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
        }
        return cashRespData;
    }

}
