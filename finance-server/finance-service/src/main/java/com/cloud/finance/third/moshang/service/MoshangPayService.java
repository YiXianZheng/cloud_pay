package com.cloud.finance.third.moshang.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.*;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.jinxin.utils.PayUtil;
import com.cloud.finance.third.moshang.utils.MSSignUtil;
import com.cloud.finance.third.moshang.utils.MSUtils;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.MD5Util;
import com.cloud.sysconf.common.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/11/01 14:12
 * @Description:
 */
@Service("MoshangPayService")
public class MoshangPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(MoshangPayService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }

    private String getBasePayUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_BASE_URL");
    }

    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        logger.info("[moshang " + shopPayDto.getChannelTypeCode() + " create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("false");

        String mch_id = thirdChannelDto.getMerchantId();
        String total_fee = String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100));
        String service = "WECHAT_FAST";
        String out_trade_no = shopPayDto.getSysPayOrderNo();
        String sign_type = "MD5";
        String notify_url = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
        String body = "test";
        String return_url = "http://www.baidu.com";

        Map<String, String> params = new HashMap<>();
        params.put("mch_id", mch_id);
        params.put("total_fee", total_fee);
        params.put("service", service);
        params.put("out_trade_no", out_trade_no);
        params.put("sign_type", sign_type);
        params.put("notify_url", notify_url);
        params.put("body", body);
        params.put("return_url", return_url);

        String signBefore = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[moshang sign before]: " + signBefore);
        String sign = MD5Util.md5(signBefore).toUpperCase();
        logger.info("[moshang sign]: " + sign);
        params.put("sign", sign);

        String respStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
        logger.info("[moshang post result str]: " + respStr);
        Map<String, Object> respMap = JSONObject.parseObject(respStr, HashMap.class);
        logger.info("[moshang post result map]: " + respMap);
        if (respMap == null) {
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            logger.error("【通道支付请求结果为空】");
            return payCreateResult;
        }

        if ("SUCCESS".equals(respMap.get("ret_code"))) {
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setStatus("true");
            payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
            payCreateResult.setResultMessage("成功生成支付链接");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setPayUrl((String) respMap.get("payinfo"));
            logger.info("【通道支付请求成功】-------成功生成支付链接");
        } else {
            payCreateResult.setResultMessage("生成跳转地址失败");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            logger.error("【通道支付请求失败】-------" + respMap.get("ret_message"));
        }
        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        logger.info("[moshang " + shopPayDto.getChannelTypeCode() + " create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        /*String actionRespCode = SysPayResultConstants.SUCCESS_MAKE_ORDER + "";
        String actionRespMessage = "生成跳转地址成功";

        String actionRespUrl = getBasePayUrl() + "/d8/moshang_" + shopPayDto.getSysPayOrderNo() + ".html";
        String channelPayOrderNo = shopPayDto.getSysPayOrderNo();
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
        payCreateResult.setResultCode(actionRespCode);
        payCreateResult.setChannelOrderNo(channelPayOrderNo);
        payCreateResult.setResultMessage(actionRespMessage);

        payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
        payCreateResult.setStatus("true");
        payCreateResult.setPayUrl(actionRespUrl);
        return payCreateResult;*/
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("false");

        //请求参数
        String callbackurl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();			//支付结果异步地址
        String notitybackurl = "";//shopPayDto.getMerchantReturnUrl();			//支付结果同步地址
        String orderid = shopPayDto.getSysPayOrderNo();  			//订单ID
        String type = "1006";               	//支付卡类型
        String value = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());              	//支付面值
        String attach = shopPayDto.getMerchantGoodsTitle();													//备注信息
        String payerIp = "";

        Map<String, String> params = new HashMap<>();
        if(StringUtils.isNotBlank(thirdChannelDto.getMerchantId())) {
            params.put("merchantid", thirdChannelDto.getMerchantId());
        }
        if(StringUtils.isNotBlank(callbackurl)) {
            params.put("callbackurl", callbackurl);
        }
        if(StringUtils.isNotBlank(notitybackurl)) {
            params.put("notitybackurl", notitybackurl);
        }
        if(StringUtils.isNotBlank(orderid)) {
            params.put("orderid", orderid);
        }
        if(StringUtils.isNotBlank(value)) {
            params.put("value", value);
        }
        if(StringUtils.isNotBlank(type)) {
            params.put("type", type);
        }
        if(StringUtils.isNotBlank(attach)) {
            params.put("attach", attach);
        }
        if(StringUtils.isNotBlank(payerIp)) {
            params.put("payerIp", payerIp);
        }

        //签名
        String sign = MSSignUtil.aiyangpayBankMd5Sign(thirdChannelDto.getMerchantId(),type,value,orderid,callbackurl,thirdChannelDto.getPayMd5Key());//签名
        logger.info("[moshang sign msg]:"+sign);

        params.put("sign", sign);
        logger.info("[pay post params] == > " + PayUtil.getSignParam(params));

        try {
            String jsonStr = PostUtils.jsonPostForCharset(thirdChannelDto.getPayUrl(), params, "GB2312");
            boolean isJson = MSUtils.isJson(jsonStr);
            if (!isJson) {
                payCreateResult.setResultMessage(jsonStr);
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                logger.error("【支付请求失败】-------" + jsonStr);
                return payCreateResult;
            }
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【支付请求请求结果为空】");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");

                return payCreateResult;
            }
            logger.info("pay post result == > "+ jsonStr);

            Map<Object, Object> respMap = MapUtils.json2Map(jsonStr);

            if("000".equals(respMap.get("resCode").toString())){
                String payUrl = getBasePayUrl() + "/d8/moshang_" + shopPayDto.getSysPayOrderNo() + ".html";
                shopPayDto.setThirdChannelRespMsg(respMap.get("codeUrl").toString());
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setPayUrl(payUrl);
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            }else{
                payCreateResult.setResultMessage("生成跳转地址失败");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                logger.error("【通道支付请求失败】-------" + jsonStr);
            }
        } catch (Exception e) {
            payCreateResult.setStatus("error");
            e.printStackTrace();
            logger.error("【通道支付请求异常】-------");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");
        }
        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return  null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createQuickJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;

    }

    @Override
    public MidPayCreateResult createSytAllIn(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCheckResult checkOrderResult(ThirdChannelDto thirdChannelDto, ShopPay shopPay) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createSysPayOrderId(String channelId, String assId, String assPayOrderNo) {
        String tm = System.currentTimeMillis() + "";// 获取系统毫秒时间戳
        // 4位+13位+3位随机数
        String sysPayOrderNo = channelId + tm + StringUtil.getRandom(3);
        return sysPayOrderNo;
    }

    @Override
    public ChannelAccountData queryAccount(ThirdChannelDto thirdChannelDto) {
        // TODO Auto-generated method stub
        return null;
    }
}
