package com.cloud.finance.third.hankou.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.hankou.utils.HKUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.cloud.finance.common.utils.PostUtils.xmlPost;

@Service("HankouPayService")
public class HankouPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(HankouPayService.class);
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

        logger.info("[hankou pay create params] channel: " + thirdChannelDto.getId() + ", sysPayNo: " + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("false");
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());

        // 请求参数
        // 商户号
        String mch_id = thirdChannelDto.getMerchantId();
        // 随机字符串
        String nonce_str = HKUtil.getRandomString(32);
        // 商品描述
        String body = "test";
        // 商户订单号
        String out_trade_no = shopPayDto.getSysPayOrderNo();
        // 标价金额 不能带小数
        String total_fee = String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100));
        // 终端IP
        String spbill_create_ip = "27.154.2.30";
        // 通知地址
        String notify_url = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
        // 交易类型
        String trade_type = "MWEB";
        // 支付渠道
        String pay_channel = "WEIXIN";

        Map<String, String> params = new HashMap<>();
        params.put("mch_id", mch_id);
        params.put("nonce_str", nonce_str);
        params.put("body", body);
        params.put("total_fee", total_fee);
        params.put("spbill_create_ip", spbill_create_ip);
        params.put("notify_url", notify_url);
        params.put("out_trade_no", out_trade_no);
        params.put("trade_type", trade_type);
        params.put("pay_channel", pay_channel);

        logger.info("[hankou before sign msg]: " + params);
        // 签名   key不参与排序
        String sign = "";
        try {
            sign = HKUtil.generateMd5Sign(params, thirdChannelDto.getPayMd5Key());
            logger.info("[hankou sign str]: " + sign);
            params.put("sign", sign);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[hankou sign exception]");
        }
        String xmlStr = ASCIISortUtil.buildXmlSign(params);
        logger.info("[hankou post xml]" + xmlStr);
        try {
            // 发送POST请求
            String contentType = "application/xml; charset=utf-8";
            String jsonStr = xmlPost(thirdChannelDto.getPayUrl(), xmlStr, contentType);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");

                return payCreateResult;
            }
            logger.info("pay post result == > " + jsonStr);

            Map<String, String> respMap = (Map<String, String>) JSONObject.parse(jsonStr);

            logger.info("pay post result map == > " + respMap);
            logger.info("pay post result map == > " + (respMap != null ? respMap.get("result_code") : null));

            if(respMap != null && "SUCCESS".equals(respMap.get("result_code"))){

                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setPayUrl(respMap.get("location"));
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            }else{
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败");
                logger.error("【通道支付请求失败】-------" + jsonStr);
            }
            return payCreateResult;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道支付请求异常】-------");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");
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
