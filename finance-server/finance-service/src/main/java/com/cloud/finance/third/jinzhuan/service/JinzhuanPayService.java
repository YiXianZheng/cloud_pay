package com.cloud.finance.third.jinzhuan.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.*;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: 金砖支付
 * @author: zyx
 * @create: 2019-04-25 16:56
 **/
@Service("JinzhuanPayService")
public class JinzhuanPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(JinzhuanPayService.class);
    @Autowired
    private ShopPayService payService;
    @Autowired
    private RedisClient redisClient;

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }
    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return createPayUrl(thirdChannelDto, shopPayDto);
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createQuickJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createSytAllIn(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCheckResult checkOrderResult(ThirdChannelDto thirdChannelDto, ShopPay shopPay) {
        return null;
    }

    @Override
    public String createSysPayOrderId(String channelId, String assId, String assPayOrderNo) {
        return null;
    }

    @Override
    public ChannelAccountData queryAccount(ThirdChannelDto thirdChannelDto) {
        return null;
    }

    private MidPayCreateResult createPayUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        logger.info("[jinzhuan h5 pay create params] channel: " + thirdChannelDto.getId() + ", sysPayNo: " + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

        Map<String, String> params = new HashMap<>();
        params.put("notify_url", getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl());
        params.put("amount", SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney()));
        params.put("pay_type", "1");
        params.put("return_url", shopPayDto.getMerchantReturnUrl());
        params.put("mch_code", thirdChannelDto.getMerchantId());
        params.put("mch_trade_no", shopPayDto.getSysPayOrderNo());
        params.put("timespan", System.currentTimeMillis() + "");
        logger.info("[jinzhuan before sign msg]: " + params);

        // 签名   key不参与排序
        String signBefore = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[jinzhuan sign before]: " + signBefore);
        String sign = MD5Util.md5(signBefore);
        logger.info("[jinzhuan sign result]: " + sign);
        params.put("sign", sign);

        try {
            String respStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if (respStr == null) {
                payCreateResult.setResultMessage("支付请求结果为空");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                return payCreateResult;
            }
            logger.info("请求结果：" + respStr);
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            logger.info("结果转换：" + respMap);
            if ("true".equals(respMap.get("success").toString())) {
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setPayUrl(respMap.get("url").toString());
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            } else {
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败 ===>" + respMap.get("message"));
                logger.error("【通道支付请求失败】-------" + respMap.get("message"));
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            }
        } catch (Exception e) {
            e.printStackTrace();
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setResultMessage("请求链接异常");
        }
        return payCreateResult;
    }
}
