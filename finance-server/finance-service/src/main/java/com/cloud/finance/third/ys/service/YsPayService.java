package com.cloud.finance.third.ys.service;

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
import com.cloud.sysconf.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: Ys支付请求
 * @author: zyx
 * @create: 2019-03-16 23:03
 **/
//@Service("YsPayService")
public class YsPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(YsPayService.class);
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }
    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return createYsPayUrl(thirdChannelDto, shopPayDto);
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return createYsPayUrl(thirdChannelDto, shopPayDto);
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return createYsPayUrl(thirdChannelDto, shopPayDto);
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

    private MidPayCreateResult createYsPayUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        String payType = shopPayDto.getChannelTypeCode();
        logger.info("ys create " + payType + " pay params ===> orderId: " + shopPayDto.getSysPayOrderNo() + " channelId: " + thirdChannelDto.getId());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        Map<String, String> params = new HashMap<>();
        payCreateResult.setStatus("error");
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
        String orderCode;
        switch (payType) {
            case "wx_qrcode":
            case "wx_h5_wake":
                orderCode = "ali_weChatPay";
                params.put("subject", "可口可乐");
                break;
            case "ali_h5_wake":
            case "ali_qrcode":
                orderCode = "ali_wapPay";
                params.put("subject", "可口可乐");
                break;
            default:
                orderCode = "ys_unionPay";
                params.put("productName", "可口可乐");
                break;
        }

        params.put("userid", thirdChannelDto.getMerchantId());
        params.put("amount", SafeComputeUtils.numberFormate2(shopPayDto.getMerchantPayMoney()));
        params.put("pageNotifyUrl", shopPayDto.getMerchantReturnUrl());
        params.put("notifyUrl", getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl());

        String signStr = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[ys pay sign str]: " + signStr);
        String sign = MD5Util.md5(signStr);
        logger.info("[ys pay sign result]: " + sign);
        params.put("sign", sign);
        params.put("pay_number", shopPayDto.getSysPayOrderNo());
        params.put("orderCode", orderCode);

        String baowen = ASCIISortUtil.buildSign(params, "=", "");
        logger.info("上送请求报文: " + baowen);
        try {
            String respStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
            logger.info("[ys pay get result str]: " + respStr);
            if (StringUtil.isEmpty(respStr)) {
                payCreateResult.setResultMessage("支付请求结果为空");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                return payCreateResult;
            }
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);
            String resultCode = respMap.get("respCode").toString();
            String resultMsg = respMap.get("respInfo").toString();
            if (resultCode.equals("0000")) {
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setPayUrl(respMap.get("payUrl").toString());
                logger.info("【通道支付请求成功】------- 成功生成支付链接");
            } else {
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败 ===>" + resultMsg);
                logger.error("【通道支付请求失败】------- 状态码：" + resultCode + " 错误消息：" + resultMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            payCreateResult.setResultCode(SysPayResultConstants.CHANNEL_REQUEST_ERROR + "");
            payCreateResult.setResultMessage("通道请求异常");
        }
        return payCreateResult;
    }
}
