package com.cloud.finance.third.shtd1.service;

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
import com.cloud.finance.third.shtd1.util.MD5Utils;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/9/20 21:12
 * @Description:
 */
@Service("Shtd1PayService")
public class Shtd1PayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(Shtd1PayService.class);

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
        logger.info("[shanghai channel 1 pay create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

        /*** 公共参数 ***/
        //商户账号  商户在支付平台的唯一标识
        String userid = thirdChannelDto.getMerchantId();
        //交易命令
        String orderCode = "hc_aliPay";
        /** 支付请求参数 **/
        //下游订单号
        String pay_number = shopPayDto.getSysPayOrderNo();
        //订单金额  以“元”为单位，仅允许两位小数，必须大于零
        String amount = SafeComputeUtils.numberFormate2(shopPayDto.getMerchantPayMoney());
        //交易成功跳转页
        String pageNotifyUrl = shopPayDto.getMerchantReturnUrl();
        //支付结果通知地址
        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
        //商品标题
        String subject = "电子产品";

        Map<String, String> params = new HashMap<>();
        params.put("userid", userid);
        params.put("notifyUrl", notifyUrl);
        params.put("amount", amount);
        params.put("pageNotifyUrl", pageNotifyUrl);
        params.put("subject", subject);

        //签名
        String sign = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 before sign msg]:"+sign+"; key:" +thirdChannelDto.getPayMd5Key());
        sign = MD5Utils.getKeyedDigest(sign, thirdChannelDto.getPayMd5Key());
        logger.info("[shanghai channel 1 sign msg]:"+sign);

        params.put("sign", sign);
        params.put("orderCode", orderCode);
        params.put("pay_number", pay_number);

        String baowen = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 request params]:"+baowen);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");

                return payCreateResult;
            }
            logger.info("pay post result == > "+ jsonStr);

            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("0000".equals(respMap.get("respCode"))){
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER+"");
                payCreateResult.setResultMessage(respMap.get("respInfo"));
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setChannelOrderNo(respMap.get("orderId"));
                payCreateResult.setPayUrl(respMap.get("payUrl"));
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            }else{
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                payCreateResult.setResultMessage("生成跳转地址失败");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                logger.error("【通道支付请求失败】-------"+respMap.get("respInfo"));
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
        logger.info("[shanghai channel 1 query balance]");

        ChannelAccountData channelAccountData = new ChannelAccountData();
        channelAccountData.setStatus(ChannelAccountData.STATUS_ERROR);

        /*** 公共参数 ***/
        //商户账号  商户在支付平台的唯一标识
        String userid = thirdChannelDto.getMerchantId();
        //交易命令
        String orderCode = "ali_balanceQuery";
        /** 支付请求参数 **/
        //下游订单号
        String pay_number = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13);

        Map<String, String> params = new HashMap<>();
        params.put("userid", userid);

        //签名
        String sign = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 before sign msg]:"+sign+"; key:" +thirdChannelDto.getPayMd5Key());
        sign = MD5Utils.getKeyedDigest(sign, thirdChannelDto.getPayMd5Key());
        logger.info("[shanghai channel 1 sign msg]:"+sign);

        params.put("sign", sign);
        params.put("orderCode", orderCode);
        params.put("pay_number", pay_number);

        String baowen = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 request params]:"+baowen);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道余额查询请求请求结果为空】");
                channelAccountData.setMsg("余额查询请求结果为空");

                return channelAccountData;
            }
            logger.info("query post result == > "+ jsonStr);

            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("0000".equals(respMap.get("respCode"))){
                String codeBalanceStr = respMap.get("code_balance");
                String maxCodeBalanceStr = respMap.get("max_code_balance");
                Double balance = StringUtils.isNotBlank(codeBalanceStr)?Double.parseDouble(codeBalanceStr):0D;
                Double usable = StringUtils.isNotBlank(maxCodeBalanceStr)?Double.parseDouble(maxCodeBalanceStr):0D;
                channelAccountData.setAmount(usable);
                channelAccountData.setFrozenAmount(SafeComputeUtils.sub(balance, usable));
                channelAccountData.setStatus(ChannelAccountData.STATUS_SUCCESS);
                channelAccountData.setMsg("通道余额查询成功");
                logger.info("【通道余额查询请求成功】-------");
            }else{
                logger.error("【通道余额查询请求请求失败】-------" + respMap.get("respInfo"));
                channelAccountData.setMsg(respMap.get("respInfo"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道余额查询请求异常】-------");
            channelAccountData.setMsg(SysPayResultConstants.ERROR_SYS_PARAMS+"");
        }
        return channelAccountData;
    }
}
