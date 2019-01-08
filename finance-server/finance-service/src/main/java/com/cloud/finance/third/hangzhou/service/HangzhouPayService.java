package com.cloud.finance.third.hangzhou.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.hangzhou.utils.SignUtil;
import com.cloud.finance.third.hangzhou.utils.XmlUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.cloud.finance.common.utils.PostUtils.xmlPost;


@Service("HangzhouPayService")
public class HangzhouPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(HangzhouPayService.class);

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
        logger.info("[hangzhou pay create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

        //请求参数
        String notify_url = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();			    //支付结果异步地址
        String pay_id = thirdChannelDto.getMerchantId();                                        //商户号
        String out_trade_no = shopPayDto.getSysPayOrderNo();  			                        //订单ID
        String create_time = String.valueOf(new Date().getTime());                              //提交时间
        String total_fee = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());    //支付面值
        String user_account = shopPayDto.getMerchantCode();                                     //用户标识

        Map<String, String> params = new HashMap<>();

        params.put("create_time", create_time);
        if(StringUtils.isNotBlank(pay_id)) {
            params.put("pay_id", pay_id);
        }
        if(StringUtils.isNotBlank(notify_url)) {
            params.put("notify_url", notify_url);
        }
        if(StringUtils.isNotBlank(out_trade_no)) {
            params.put("out_trade_no", out_trade_no);
        }
        if(StringUtils.isNotBlank(total_fee)) {
            params.put("total_fee", total_fee);
        }
        if(StringUtils.isNotBlank(user_account)) {
            params.put("user_account", user_account);
        }

        String sign = "";
        //签名
        try {
            sign = SignUtil.generateSignature(params, thirdChannelDto.getPayMd5Key());
            logger.info("[hangzhou sign msg]:" + sign);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("---- sign exception ----");
        }

        params.put("sign", sign);

        logger.info("[before change to xml]:" + params);

        // 对map进行ASCII码排序并转成xml
        String xmlStr = ASCIISortUtil.buildXmlSign(params);

        try {
            // 发送POST请求
            String contentType = "text/xml";
            String jsonStr = xmlPost(thirdChannelDto.getPayUrl(), xmlStr, contentType);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");

                return payCreateResult;
            }
            logger.info("pay post result xml == > " + jsonStr);

            Map<String, String> respMap = XmlUtil.xmlToMap(jsonStr);

            logger.info("pay post result map == > " + respMap);
            logger.info("pay post result map == > " + (respMap != null ? respMap.get("code") : null));

            if(respMap != null && "200".equals(respMap.get("code").toString())){

                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setPayUrl(respMap.get("location"));
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            }else{
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
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
