package com.cloud.finance.third.zhaocai.service;

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
import com.cloud.sysconf.common.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 招财支付
 * @author: zyx
 * @create: 2019-04-03 10:38
 **/
@Service("ZhaocaiPayService")
public class ZhaocaiPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(ZhaocaiPayService.class);
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;

    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return createPayUrl(thirdChannelDto, shopPayDto);
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

        logger.info("[zhaocai h5 pay create params] channel: " + thirdChannelDto.getId() + ", sysPayNo: " + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

        Map<String, String> params = new HashMap<>();
        params.put("pay_version", "1.0.7");
        params.put("pay_amount", String.valueOf(shopPayDto.getMerchantPayMoney()));
        params.put("ac", "pays");
        params.put("pay_bankcode", thirdChannelDto.getAppId());
        params.put("appid", thirdChannelDto.getMerchantId());
        params.put("pay_orderid", shopPayDto.getSysPayOrderNo());
        params.put("pay_time", DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18));
        logger.info("[zhaocai before sign msg]: " + params);

        // 签名   key不参与排序
        String signBefore = ASCIISortUtil.buildSign(params, "=", thirdChannelDto.getPayMd5Key());
        logger.info("[zhaocai sign before]: " + signBefore);
        String sign = com.cloud.sysconf.common.utils.MD5Util.md5(signBefore);
        logger.info("[zhaocai sign result]: " + sign);
        params.put("sg", sign);

        try {
            String respStr = GetUtils.sendGetMethod(thirdChannelDto.getPayUrl(), params);
            if (respStr == null) {
                payCreateResult.setResultMessage("支付请求结果为空");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                return payCreateResult;
            }
            logger.info("请求结果：" + respStr);
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            logger.info("结果转换：" + respMap);
            if ("1".equals(respMap.get("code").toString())) {
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                Map<Object, Object> payUrl = MapUtils.json2Map(respMap.get("msg"));
                payCreateResult.setPayUrl(payUrl.get("payresult").toString());
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            } else {
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败 ===>" + respMap.get("msg"));
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                logger.error("【通道支付请求失败】-------" + respMap.get("msg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setResultMessage("请求链接异常");
        }
        return payCreateResult;
    }
}
