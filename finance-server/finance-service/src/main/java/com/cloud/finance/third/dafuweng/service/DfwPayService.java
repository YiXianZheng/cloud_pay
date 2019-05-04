package com.cloud.finance.third.dafuweng.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.GetUtils;
import com.cloud.finance.common.utils.MapUtils;
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
import com.cloud.sysconf.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;

/**
 * @description: 大富翁支付
 * @author: zyx
 * @create: 2019-03-07 17:00
 **/
@Service("DfwPayService")
public class DfwPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(DfwPayService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;

    private String getBasePayUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_BASE_URL");
    }

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

        logger.info("dfw create qrcode pay params ===> orderId: " + shopPayDto.getSysPayOrderNo() + " channelId: " + thirdChannelDto.getId());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
        String payType = shopPayDto.getChannelTypeCode();
        logger.info("[pay channel]: " + payType);
        String payCode;
        switch (payType) {
            case "wx_qrcode":
                payCode = "1";
                break;
            case "wx_self_wap":
                payCode = "2";
                break;
            case "wx_h5_wake":
                payCode = "2";
                break;
            case "qq_qrcode":
                payCode = "31";
                break;
            default:
                payCode = "12";
                break;
        }
        Map<String, String> params = new TreeMap<>();
        params.put("companyId", thirdChannelDto.getMerchantId());
        params.put("userOrderId", shopPayDto.getSysPayOrderNo());
        params.put("payType", payCode);
        params.put("item", "电子产品");
        params.put("fee", String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100)));
        params.put("callbackUrl", shopPayDto.getMerchantReturnUrl());
        params.put("syncUrl", getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl());
        params.put("ip", "127.0.0.1");

        String signStr = params.get("companyId") + "_" + params.get("userOrderId") + "_" + params.get("fee") + "_" + thirdChannelDto.getPayMd5Key();
        logger.info("[dfw pay sign str]: " + signStr);
        String sign = MD5Util.md5(signStr);
        logger.info("[dfw pay sign result]: " + sign);
        params.put("sign", sign);
        logger.info("[dfw pay request params]: " + params);
        try {
            String respStr = GetUtils.sendGetMethod(thirdChannelDto.getPayUrl(), params);
            logger.info("[dfw pay get result str]: " + respStr);
            if (StringUtil.isEmpty(respStr)) {
                payCreateResult.setResultMessage("支付请求结果为空");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                return payCreateResult;
            }
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);
            String resultCode = respMap.get("result").toString();
            String resultMsg = respMap.get("msg").toString();
            if (resultCode.equals("0")) {
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setPayUrl(respMap.get("param").toString());
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
