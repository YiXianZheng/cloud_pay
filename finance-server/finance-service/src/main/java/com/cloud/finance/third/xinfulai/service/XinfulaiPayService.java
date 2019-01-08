package com.cloud.finance.third.xinfulai.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.xinfulai.util.XFLUtils;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.StringUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/9/20 21:12
 * @Description:
 */
@Service("XinfulaiPayService")
public class XinfulaiPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(XinfulaiPayService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }

    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        logger.info("[xinfulai channel 1 pay create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

        /*** 公共参数 ***/
        //商户账号  商户在支付平台的唯一标识
        String merchantId = thirdChannelDto.getMerchantId();
        /** 支付请求参数 **/
        //平台订单号
        String merOrderId = shopPayDto.getSysPayOrderNo();
        //订单金额  以“分”为单位，不允许小数
        String txnAmt = String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100));
        //支付结果通知地址
        String backUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
        //商品标题
        String subject = "手机充电线";
        // 签名方法 不参与签名
        String signMethod = "MD5";

        Map<String, String> params = new HashMap<>();
        params.put("merchantId", merchantId);
        params.put("merOrderId", merOrderId);
        params.put("txnAmt", txnAmt);
        params.put("subject", subject);
        params.put("content", subject);
        params.put("userId", "");
        params.put("gateway", "alipayqr");
        params.put("backUrl", backUrl);

        //签名
        String signature = XFLUtils.signMap(params, thirdChannelDto.getPayMd5Key());
        logger.info("[xinfulai channel pay sign before]:" + signature);

        params.put("subject", XFLUtils.encodeBASE64(subject));
        params.put("content", XFLUtils.encodeBASE64(subject));
        params.put("signature", signature);
        params.put("signMethod", signMethod);

        String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
        logger.info("请求结果：" + jsonStr);

        if(StringUtils.isEmpty(jsonStr)){
            logger.error("【通道支付请求请求结果为空】");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");

            return payCreateResult;
        }

        Map<String, Object> respMap = JSONObject.parseObject(jsonStr, HashedMap.class);
        logger.info("[xinfulai channel pay result map]: " + respMap);
        if("0".equals(respMap.get("code") + "") && "1".equals(respMap.get("success") + "")){
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setStatus("true");
            payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setPayUrl((String) respMap.get("payLink"));
            logger.info("【通道支付请求成功】-------成功生成支付链接");
        }else{
            payCreateResult.setStatus("false");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setResultMessage("生成跳转地址失败");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            logger.error("【通道支付请求失败】-------"+respMap.get("msg"));
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
        return null;
    }
}
