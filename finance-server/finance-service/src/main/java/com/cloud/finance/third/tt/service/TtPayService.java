package com.cloud.finance.third.tt.service;

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

@Service("TtPayService")
public class TtPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(TtPayService.class);
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
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        logger.info("[tt ali_h5_wake pay create params] channel: " + thirdChannelDto.getId() + ", sysPayNo: " + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("false");
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());

        Map<String, String> params = new HashMap<>();
        params.put("money", String.valueOf(shopPayDto.getMerchantPayMoney()));
        params.put("appCodes", thirdChannelDto.getAppId());
        params.put("clientId", thirdChannelDto.getMerchantId());
        params.put("tradeNo", shopPayDto.getSysPayOrderNo());

        String signBefore = params.get("clientId") + params.get("money") + params.get("appCodes") + params.get("tradeNo") + thirdChannelDto.getPayMd5Key();
        logger.info("[tt sign before str]：" + signBefore);
        String sign = MD5Util.md5(signBefore);
        logger.info("[tt sign result str]：" + sign);
        params.put("sign", sign);
        params.put("pushUrl", getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl());

        String postJson = JSONObject.toJSONString(params);
        logger.info("[tt pay post json]: " + postJson);
        String respStr = PostUtils.postWithJson(thirdChannelDto.getPayUrl(), postJson);
        logger.info("[tt pay post result str]: " + respStr);
        Map<String, Object> respMap = JSONObject.parseObject(respStr, HashMap.class);
        logger.info("[tt pay post result map]: " + respMap);

        if (respMap == null) {
            payCreateResult.setResultMessage("通道支付请求结果为空");
            return payCreateResult;
        }
        if ("0".equals(respMap.get("code").toString())) {
            // 成功
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setStatus("true");
            payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
            payCreateResult.setResultMessage("生成跳转地址成功");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setPayUrl(respMap.get("msg").toString());
        } else {
            payCreateResult.setStatus("false");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setResultMessage("生成跳转地址失败：" + respMap.get("msg"));
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            logger.error("【通道支付请求失败】：" + respMap.get("msg"));
        }

        return payCreateResult;
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
}
