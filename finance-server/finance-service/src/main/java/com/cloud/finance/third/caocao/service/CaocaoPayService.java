package com.cloud.finance.third.caocao.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.HttpClientUtil;
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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("CaocaoPayService")
public class CaocaoPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(CaocaoPayService.class);

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

        logger.info("[caocao " + shopPayDto.getChannelTypeCode() + " create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("false");

        Map<String, String> params = new HashMap<>();
        params.put("mchId", thirdChannelDto.getMerchantId());
        params.put("appId", thirdChannelDto.getAppId());
        params.put("productId", "8006");
        params.put("mchOrderNo", shopPayDto.getSysPayOrderNo());
        params.put("currency", "cny");
        params.put("amount", String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100)));
        params.put("notifyUrl", getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl());
        params.put("subject", "测试商品1");
        params.put("body", "测试商品描述");
        params.put("device", "WEB");
        params.put("clientIp", "211.94.116.218");

        // 签名
        String signBefore = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[caocao pay sign before]: " + signBefore);
        String sign = MD5Util.md5(signBefore).toUpperCase();
        logger.info("[caocao pay sign msg]: " + sign);
        params.put("sign", sign);
        params.put("param1", "");
        params.put("param2", "");

        String respStr;
        try {
            respStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
        } catch (Exception e) {
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            logger.error("【通道支付请求结果为空】");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            return payCreateResult;
        }
        logger.info("[caocao pay post respStr]: " + respStr);

        Map<String, Object> respMap = JSONObject.parseObject(respStr, HashMap.class);

        logger.info("[caocao pay post respMap]: " + respMap);
        if ("SUCCESS".equals(respMap.get("retCode")) && StringUtils.isNotEmpty(respMap.get("payParams").toString())) {
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setStatus("true");
            payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
            payCreateResult.setResultMessage("成功生成支付链接");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            Map<String, String> payResult = JSONObject.parseObject(respMap.get("payParams").toString(), HashMap.class);
            payCreateResult.setPayUrl(payResult.get("codeUrl"));
            logger.info("【通道支付请求成功】------- 成功生成支付链接");
        } else {
            payCreateResult.setResultMessage("生成跳转地址失败");
            payCreateResult.setResultCode(respMap.get("errCode").toString());
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            logger.error("【通道支付请求失败】====> " + respMap.get("errDes"));
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
