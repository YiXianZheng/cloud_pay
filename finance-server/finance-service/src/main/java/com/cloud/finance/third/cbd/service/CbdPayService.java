package com.cloud.finance.third.cbd.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.MapUtils;
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
import com.cloud.sysconf.common.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("CbdPayService")
public class CbdPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(CbdPayService.class);
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
        return null;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        logger.info("[cdb h5 pay create params] channel: " + thirdChannelDto.getId() + ", sysPayNo: " + shopPayDto.getSysPayOrderNo());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("true");

        Map<String, String> params = new HashMap<>();
        params.put("versionId", "1.0");
        params.put("orderAmount", String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100)));
        params.put("orderDate", DateUtil.getSystemTime(DateUtil.DATE_PATTERN_18));
        params.put("currency", "RMB");
        params.put("accountType", "0");
        params.put("transType", "008");
        params.put("asynNotifyUrl", getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl());
        params.put("synNotifyUrl", "http://www.baidu.com");
        params.put("signType", "MD5");
        params.put("merId", thirdChannelDto.getMerchantId());
        params.put("prdOrdNo", shopPayDto.getSysPayOrderNo());
        params.put("payMode", "0");
        logger.info("[cbd before sign msg]: " + params);

        // 签名   key不参与排序
        String signBefore = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[cbd sign before]: " + signBefore);
        String sign = com.cloud.sysconf.common.utils.MD5Util.md5(signBefore).toUpperCase();
        logger.info("[cbd sign result]: " + sign);
        params.put("signData", sign);

        String respStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
        if (respStr == null) {
            payCreateResult.setResultMessage("支付请求结果为空");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            return payCreateResult;
        }
        logger.info("请求结果：" + respStr);
        Map<Object, Object> respMap = MapUtils.json2Map(respStr);

        logger.info("结果转换：" + respMap);
        if ("1".equals(respMap.get("retCode"))) {
//            String payUrl = getBasePayUrl() + "/d8/cbd_" + shopPayDto.getSysPayOrderNo() + ".html";
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setStatus("true");
            payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
            payCreateResult.setResultMessage("成功生成支付链接");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setPayUrl(respMap.get("retMsg").toString());
            logger.info("【通道支付请求成功】-------成功生成支付链接");
        } else {
            payCreateResult.setStatus("false");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setResultMessage("生成跳转地址失败 ===>" + respMap.get("message"));
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            logger.error("【通道支付请求失败】-------" + respMap.get("message"));
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
