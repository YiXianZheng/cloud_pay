package com.cloud.finance.third.anyinfu.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.enums.PayAmountEnum;
import com.cloud.finance.common.enums.SysPaymentTypeEnum;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.GetUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.anyinfu.utils.AYFUtil;
import com.cloud.finance.third.anyinfu.utils.QrCodeUtils;
import com.cloud.finance.third.hangzhou.utils.XmlUtil;
import com.cloud.finance.third.hankou.utils.HKUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.finance.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.cloud.finance.common.utils.PostUtils.xmlPost;

@Service("AnyinfuPayService")
public class AnyinfuPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(AnyinfuPayService.class);
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

        logger.info("[anyinfu wx_qrcode pay create params] channel: " + thirdChannelDto.getId() + ", sysPayNo: " + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("true");
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());

        String actionRespUrl = getBasePayUrl() + "/d8/ayfQrcode_" + shopPayDto.getSysPayOrderNo() + ".html";
        payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
        payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
        payCreateResult.setPayUrl(actionRespUrl);
        payCreateResult.setResultMessage("生成跳转地址成功");
        /*try {
            // 鉴权
            // 应用ID
            String appid = thirdChannelDto.getAppId();
            // 商户秘钥
            String key = thirdChannelDto.getPayMd5Key();
            // 随机字符串
            String random = HKUtil.getRandomString(20);
            Map<String, String> params = new HashMap<>();
            params.put("appid", appid);
            params.put("random", random);

            // 商户号
            String mch_id = thirdChannelDto.getMerchantId();
            // 商户订单号
            String out_trade_no = shopPayDto.getSysPayOrderNo();
            // 商品描述
            String body = "test";
            // 总金额
            Integer total_fee = (int) (shopPayDto.getMerchantPayMoney() * 100);
            // 终端IP
            String mch_create_ip = "127.0.0.1";
            // 通知地址
            String notify_url = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
            // 随机字符串
            String nonce_str = HKUtil.getRandomString(32);
            // 签名

            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("mch_id", mch_id);
            reqParams.put("nonce_str", nonce_str);
            reqParams.put("body", body);
            reqParams.put("total_fee", String.valueOf(total_fee));
            reqParams.put("mch_create_ip", mch_create_ip);
            reqParams.put("notify_url", notify_url);
            reqParams.put("out_trade_no", out_trade_no);

            logger.info("[anyinfu before sign msg]: " + reqParams);
            String sign = HKUtil.generateMd5Sign(reqParams, key);

            reqParams.put("sign", sign);
            String xmlStr = ASCIISortUtil.buildXmlSign(reqParams);

            String token = AYFUtil.getToken(params, key, thirdChannelDto.getAdminUrl());

            // 交易请求地址
            String payURL = thirdChannelDto.getPayUrl() + "?token=" + token;
            String contentType = "application/xml; charset=utf-8";
            String jsonStr = xmlPost(payURL, xmlStr, contentType);
            logger.info("[anyinfu wx_qrcode post result]: " + jsonStr);

            Map<String, String> respMap = XmlUtil.xmlToMap(jsonStr);
            logger.info("[anyinfu wx_qrcode success result]: " + respMap);
            if (respMap == null) {
                payCreateResult.setResultMessage("支付请求结果为空");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                return payCreateResult;
            }
            if ("0".equals(respMap.get("status")) && "0".equals(respMap.get("result_code"))) {

                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setPayUrl(respMap.get("pay_info"));
                logger.info("【通道支付请求成功】-------成功生成支付链接");

//                QrCodeUtils.createQrCode(respMap.get("pay_info"), response);
            } else {
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败 ===>" + respMap.get("message"));
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                logger.error("【通道支付请求失败】-------" + respMap.get("message"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道支付请求异常】-------");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");
        }*/
        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        logger.info("[anyinfu ali_h5_wake pay create params] channel: " + thirdChannelDto.getId() + ", sysPayNo: " + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("false");
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());

        try {
            // 鉴权
            // 应用ID
            String appid = thirdChannelDto.getAppId();
            // 商户秘钥
            String key = thirdChannelDto.getPayMd5Key();
            // 随机字符串
            String random = HKUtil.getRandomString(20);
            Map<String, String> params = new HashMap<>();
            params.put("appid", appid);
            params.put("random", random);

            String token = AYFUtil.getToken(params, key, thirdChannelDto.getAdminUrl());

            // 交易请求地址
            String payURL = thirdChannelDto.getPayUrl() + "?token=" + token;

            // 商户号
            String mch_id = thirdChannelDto.getMerchantId();
            // 商户订单号
            String out_trade_no = shopPayDto.getSysPayOrderNo();
            // 商品描述
            String body = "test";
            // 总金额
            String total_fee = String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100));
            // 终端IP
            String mch_create_ip = "127.0.0.1";
            // 通知地址
            String notify_url = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
            // 随机字符串
            String nonce_str = HKUtil.getRandomString(32);

            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("mch_id", mch_id);
            reqParams.put("nonce_str", nonce_str);
            reqParams.put("body", body);
            reqParams.put("total_fee", total_fee);
            reqParams.put("mch_create_ip", mch_create_ip);
            reqParams.put("notify_url", notify_url);
            reqParams.put("out_trade_no", out_trade_no);

            logger.info("[anyinfu before sign msg]: " + reqParams);
            // 签名
            String sign = HKUtil.generateMd5Sign(reqParams, key);

            reqParams.put("sign", sign);
            String xmlStr = ASCIISortUtil.buildXmlSign(reqParams);

            String contentType = "application/xml; charset=utf-8";
            String jsonStr = xmlPost(payURL, xmlStr, contentType);
            logger.info("[anyinfu ali_h5_wake pay post result]: " + jsonStr);

            Map<String, String> respMap = XmlUtil.xmlToMap(jsonStr);
            logger.info("[anyinfu ali_h5_wake pay success result]: " + respMap);
            if (respMap != null && "0".equals(respMap.get("status")) && "0".equals(respMap.get("result_code"))) {
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setPayUrl(respMap.get("code_url"));
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            } else {
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败");
                logger.error("【通道支付请求失败】-------" + jsonStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            payCreateResult.setResultCode(SysPayResultConstants.CHANNEL_REQUEST_ERROR + "");
            payCreateResult.setStatus("false");
            logger.error("【通道支付请求异常】-------");
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

    private MidPayCreateResult createPayUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        String payType = shopPayDto.getChannelTypeCode();
        logger.info("[anyinfu " + payType + " pay channelId]: " + thirdChannelDto.getId() + ", sysPayNo: " + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
        Map<String, String> params = new HashMap<>();

        try {
            String mch_id = thirdChannelDto.getMerchantId();
            String out_trade_no = shopPayDto.getSysPayOrderNo();
            String body = "test";
            String total_fee = String.format("%.2f", shopPayDto.getMerchantPayMoney());
            String mch_create_ip = "127.0.0.1";
            String notify_url = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
            String nonce_str = HKUtil.getRandomString(32);
            String card_no = "1212121212121212121";

            params.put("mch_id", mch_id);
            params.put("out_trade_no", out_trade_no);
            params.put("body", body);
            params.put("total_fee", total_fee);
            params.put("mch_create_ip", mch_create_ip);
            params.put("notify_url", notify_url);
            params.put("nonce_str", nonce_str);
            if (shopPayDto.getChannelTypeCode().equals(SysPaymentTypeEnum.GATE_WEB_SYT.getValue())) {
                params.put("card_no", card_no);
            }

            String sign = HKUtil.generateMd5Sign(params, thirdChannelDto.getPayMd5Key());
            logger.info("[anyinfu " + payType + " sign msg]: " + sign);

            params.put("sign", sign);
            String xmlStr = ASCIISortUtil.buildXmlSign(params);

            String contentType = "application/xml; charset=utf-8";
            String jsonStr = xmlPost(thirdChannelDto.getPayUrl(), xmlStr, contentType);
            logger.info("[anyinfu " + payType + " pay post result]: " + jsonStr);

            Map<String, String> respMap = XmlUtil.xmlToMap(jsonStr);

            logger.info("[anyinfu " + payType + " pay success result]: " + respMap);
            if (respMap != null && "0".equals(respMap.get("status")) && "0".equals(respMap.get("result_code"))) {
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setPayUrl(respMap.get("pay_info"));
                payCreateResult.setResultMessage("成功生成支付链接");
                logger.info("【" + payType + " 通道支付请求成功】-------成功生成支付链接");
            } else {
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败");
                logger.error("【" + payType + " 通道支付请求失败】------- respMap: " + respMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            payCreateResult.setResultCode(SysPayResultConstants.CHANNEL_REQUEST_ERROR + "");
            payCreateResult.setStatus("false");
            logger.error("[anyinfu create " + payType + " pay url exception]");
        }

        return payCreateResult;
    }
}
