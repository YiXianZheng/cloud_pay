package com.cloud.finance.third.yunji.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.ainong.utils.Base64Util;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service("YunjifuPayService")
public class YunjifuPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(YunjifuPayService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;
    @Autowired
    private SysBankProvider sysBankProvider;

    private String getBasePayUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_BASE_URL");
    }

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

        logger.info("[yunji create ali_h5_wake params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());// 1.加载通道信息

        String channelPayOrderNo = shopPayDto.getSysPayOrderNo();
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        Map<String, String> params = new HashMap<>();

        String mchNo = thirdChannelDto.getMerchantId();
        String orderID = shopPayDto.getSysPayOrderNo();
        String money = String.valueOf(shopPayDto.getMerchantPayMoney());
        String body = "电子产品";
        String payType = "alipaysm";

        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
        String callbackurl = "http://www.baidu.com";
        String clientip = "127.0.0.1";

        params.put("mchNo", mchNo);
        params.put("orderID", orderID);
        params.put("money", money);
        params.put("body", body);
        params.put("payType", payType);
        params.put("notifyUrl", Base64Util.encode(notifyUrl));
        params.put("callbackurl", Base64Util.encode(callbackurl));
        params.put("clientip", clientip);

        String signBefore = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("【yunji channel sign before msg】: " + signBefore);
        String sign = com.cloud.sysconf.common.utils.MD5Util.md5(signBefore);
        logger.info("【yunji channel sign msg】: " + sign);
        params.put("sign", sign);

        String jsonStr = JSONObject.toJSONString(params);
        String requestBody = "requestBody=" + jsonStr;
        String respStr = PostUtils.sendPost(thirdChannelDto.getPayUrl(), requestBody);
        logger.info("【yunji ali_h5_wake request result】：" + respStr);
        Map<String, String> respMap = JSONObject.parseObject(respStr, HashMap.class);
        logger.info("【yunji ali_h5_wake response map】: " + respMap);

        if ("0000".equals(respMap.get("resultCode")) && "成功".equals(respMap.get("resultMsg"))) {
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setStatus("true");
            payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
            payCreateResult.setResultMessage("生成跳转地址成功");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setChannelOrderNo(channelPayOrderNo);
            payCreateResult.setPayUrl(respMap.get("codeImageUrl"));
        } else {
            payCreateResult.setStatus("false");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setResultMessage("生成跳转地址失败：" + respMap.get("resultMsg"));
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            logger.error("【通道支付请求失败】错误代码：" + respMap.get("resultCode") + "---->" + respMap.get("resultMsg"));
        }

        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        logger.info("[yunji create gateSyt params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        String actionRespCode = SysPayResultConstants.SUCCESS_MAKE_ORDER + "";
        String actionRespMessage = "生成跳转地址成功";
        // 判断通道是否支持银行编码
        if(shopPayDto.getBankCode() != null && !shopPayDto.getBankCode().equals(shopPayDto.getChannelTypeCode())){
            logger.info("【支付请求】 bank code :" + shopPayDto.getBankCode());
            ApiResponse apiResponse = sysBankProvider.toChannelCode(shopPayDto.getBankCode(), thirdChannelDto.getId());
            logger.info("【支付请求】 bank response :" + apiResponse);
            if(!(ResponseCode.Base.SUCCESS.getCode() + "").equals(apiResponse.getCode())){
                actionRespCode = SysPayResultConstants.ERROR_THIRD_BANK + "";
                actionRespMessage = "生成跳转地址失败[不支持的银行]";
            }
        }

        String actionRespUrl = getBasePayUrl() + "/d8/yj_" + shopPayDto.getSysPayOrderNo() + ".html";
        String channelPayOrderNo = shopPayDto.getSysPayOrderNo();
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
        payCreateResult.setResultCode(actionRespCode);
        payCreateResult.setResultMessage(actionRespMessage);
        payCreateResult.setChannelOrderNo(channelPayOrderNo);

        if (actionRespCode.equals(SysPayResultConstants.SUCCESS_MAKE_ORDER + "")) {
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setStatus("true");
            payCreateResult.setPayUrl(actionRespUrl);
        } else {
            payCreateResult.setStatus("false");
        }
        return payCreateResult;
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
