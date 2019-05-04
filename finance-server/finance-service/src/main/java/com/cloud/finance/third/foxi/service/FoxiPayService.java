package com.cloud.finance.third.foxi.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.enums.SysPaymentTypeEnum;
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
import com.cloud.sysconf.common.utils.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: ${description}
 * @author: zyx
 * @create: 2019-04-12 17:21
 **/
@Service("FoxiPayService")
public class FoxiPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(FoxiPayService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }
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

        String payType = shopPayDto.getChannelTypeCode();
        String bizType = "100007";

        if (payType.equals(SysPaymentTypeEnum.WX_H5_JUMP.getValue())) {
            bizType = "100013";
        } else if (payType.equals(SysPaymentTypeEnum.WX_QR_CODE.getValue())) {
            bizType = "100006";
        } else if (payType.equals(SysPaymentTypeEnum.ALI_H5_JUMP.getValue())) {
            bizType = "100014";
        }
        logger.info("[ainong create H5 params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

        Date date = new Date();
        Map<String, String> map =new HashMap<>();

        map.put("merCode", thirdChannelDto.getMerchantId());
        map.put("branchId", thirdChannelDto.getAppId());
        map.put("bizType", bizType);
        map.put("settType", "T0");
        map.put("orderId", shopPayDto.getSysPayOrderNo() + "0");
        map.put("transDate", DateUtil.DateToString(date, DateUtil.DATE_PATTERN_11));
        map.put("transTime", DateUtil.DateToString(date, DateUtil.DATE_PATTERN_20));
        map.put("transAmt", String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100)));
        map.put("subject", "扫码");
        map.put("returnUrl", getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl());

        String signStr = ASCIISortUtil.buildSign(map, "=", "&" + thirdChannelDto.getPayMd5Key());
        logger.info("签名前字符串：" + signStr);
        String sign = MD5Util.md5(signStr);
        logger.info("签名结果：" + sign);
        map.put("signature", sign);
        String json = JSONObject.toJSONString(map);
        logger.info("json数据：" + json);
        try {
            String respStr = PostUtils.postWithJson(thirdChannelDto.getPayUrl(), json);
            if (respStr == null) {
                payCreateResult.setResultMessage("支付请求结果为空");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                return payCreateResult;
            }
            logger.info("请求结果：" + respStr);
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            logger.info("结果转换：" + respMap);
            if ("F5".equals(respMap.get("respCode").toString())) {
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setPayUrl(respMap.get("qrCodeURL").toString());
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            } else {
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败 ===>" + respMap.get("respMsg"));
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                logger.error("【通道支付请求失败】-------" + respMap.get("respMsg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setResultMessage("请求链接异常");
        }
        return payCreateResult;
    }
}
