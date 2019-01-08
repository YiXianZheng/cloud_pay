package com.cloud.finance.third.moshang.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.jinxin.utils.PayUtil;
import com.cloud.finance.third.moshang.utils.MSSignUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/11/01 14:12
 * @Description:
 */
@Service("MoshangPayService")
public class MoshangPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(MoshangPayService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }

    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        logger.info("[moshang " + shopPayDto.getChannelTypeCode() + " create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("false");

        //请求参数
        String callbackurl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();			//支付结果异步地址
        String notitybackurl = "";//shopPayDto.getMerchantReturnUrl();			//支付结果同步地址
        String orderid = shopPayDto.getSysPayOrderNo();  			//订单ID
        String type = "1006";               	//支付卡类型
        String value = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());              	//支付面值
        String attach = shopPayDto.getMerchantGoodsTitle();													//备注信息
        String payerIp = "";

        Map<String, String> params = new HashMap<>();
        if(StringUtils.isNotBlank(thirdChannelDto.getMerchantId())) {
            params.put("merchantid", thirdChannelDto.getMerchantId());
        }
        if(StringUtils.isNotBlank(callbackurl)) {
            params.put("callbackurl", callbackurl);
        }
        if(StringUtils.isNotBlank(notitybackurl)) {
            params.put("notitybackurl", notitybackurl);
        }
        if(StringUtils.isNotBlank(orderid)) {
            params.put("orderid", orderid);
        }
        if(StringUtils.isNotBlank(value)) {
            params.put("value", value);
        }
        if(StringUtils.isNotBlank(type)) {
            params.put("type", type);
        }
        if(StringUtils.isNotBlank(attach)) {
            params.put("attach", attach);
        }
        if(StringUtils.isNotBlank(payerIp)) {
            params.put("payerIp", payerIp);
        }

        //签名
        String sign = MSSignUtil.aiyangpayBankMd5Sign(thirdChannelDto.getMerchantId(),type,value,orderid,callbackurl,thirdChannelDto.getPayMd5Key());//签名
        logger.info("[moshang sign msg]:"+sign);

        params.put("sign", sign);
        logger.info("[pay post params] == > " + PayUtil.getSignParam(params));

        try {
            String jsonStr = PostUtils.jsonPostForCharset(thirdChannelDto.getPayUrl(), params, "GB2312");
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");

                return payCreateResult;
            }
            logger.info("pay post result == > "+ jsonStr);

            Map<String, Object> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("000".equals(respMap.get("resCode").toString())){
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                payCreateResult.setResultMessage("成功生成支付链接");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setPayUrl(respMap.get("codeUrl").toString());
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            }else{
                payCreateResult.setResultMessage("生成跳转地址失败");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                logger.error("【通道支付请求失败】-------" + jsonStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道支付请求异常】-------");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");
        }
        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return  null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createQuickJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;

    }

    @Override
    public MidPayCreateResult createSytAllIn(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCheckResult checkOrderResult(ThirdChannelDto thirdChannelDto, ShopPay shopPay) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createSysPayOrderId(String channelId, String assId, String assPayOrderNo) {
        String tm = System.currentTimeMillis() + "";// 获取系统毫秒时间戳
        // 4位+13位+3位随机数
        String sysPayOrderNo = channelId + tm + StringUtil.getRandom(3);
        return sysPayOrderNo;
    }

    @Override
    public ChannelAccountData queryAccount(ThirdChannelDto thirdChannelDto) {
        // TODO Auto-generated method stub
        return null;
    }
}
