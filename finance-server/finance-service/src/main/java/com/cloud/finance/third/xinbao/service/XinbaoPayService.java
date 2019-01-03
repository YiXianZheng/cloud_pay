package com.cloud.finance.third.xinbao.service;

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
import com.cloud.finance.third.xinbao.enums.RespCodeEnum;
import com.cloud.finance.third.xinbao.utils.SignUtil;
import com.cloud.finance.third.xinbao.vo.PayNotifyData;
import com.cloud.finance.third.xinbao.vo.PayRespData;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/9/20 21:12
 * @Description:
 */
@Service("XinbaoPayService")
public class XinbaoPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(XinbaoPayService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;
    @Autowired
    private SysBankProvider sysBankProvider;

    private String getBasePayUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_BASE_URL");
    }

    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO: 2018/9/27
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        logger.info("[xinbao create qrcode params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());// 1.加载通道信息

        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

        Map<String, String> params = new HashMap<>();

        /*** 公共请求参数 ***/
        //请求号
        String requestNo = shopPayDto.getSysPayOrderNo();
        //服务名称
        String service = "scanPay";
        //商户ID
        String partnerId = thirdChannelDto.getMerchantId();
        //签名方式
        String signType = "MD5";
        //异步通知地址
        String notifyUrl = thirdChannelDto.getNotifyUrl();

        /*** 请求参数 ***/
        //付款方式
        String payMethod = "ALIPAY";
        //付款类型
        String payType = "BESCANNED";
        //支付金额
        String amount = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());
        //订单名称
        String orderName = shopPayDto.getMerchantGoodsTitle() + shopPayDto.getSysPayOrderNo();

        if(StringUtils.isNotBlank(requestNo)) {
            params.put("requestNo", requestNo);
        }
        if(StringUtils.isNotBlank(service)) {
            params.put("service", service);
        }
        if(StringUtils.isNotBlank(partnerId)) {
            params.put("partnerId", partnerId);
        }
        if(StringUtils.isNotBlank(signType)) {
            params.put("signType", signType);
        }
        if(StringUtils.isNotBlank(notifyUrl)) {
            params.put("notifyUrl", notifyUrl);
        }
        if(StringUtils.isNotBlank(payMethod)) {
            params.put("payMethod", payMethod);
        }
        if(StringUtils.isNotBlank(payType)) {
            params.put("payType", payType);
        }
        if(StringUtils.isNotBlank(amount)){
            params.put("amount", amount);
        }
        if(StringUtils.isNotBlank(orderName)) {
            params.put("orderName", orderName);
        }

        logger.info("[before MD5 sign] -> " + SignUtil.buildWaitingForSign(params));
        String sign = SignUtil.signMD5(SignUtil.buildWaitingForSign(params), thirdChannelDto.getPayMd5Key());
        params.put("sign", sign);

        logger.info("[xinbao sign msg]:signMsg:"+sign);


        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");

                return payCreateResult;
            }
            logger.info("pay post result == > "+ jsonStr);

            JSONObject jsonObject = JSONObject.fromObject(jsonStr);
            PayRespData payRespData = (PayRespData) JSONObject.toBean(jsonObject, PayRespData.class);

            if("true".equals(payRespData.getSuccess())) {
                if (RespCodeEnum.EXECUTE_PROCESSING.getCode().equals(payRespData.getResultCode())) {
                    payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                    payCreateResult.setStatus("true");
                    payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                    payCreateResult.setResultMessage(payRespData.getResultMessage());
                    payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                    payCreateResult.setChannelOrderNo(payRespData.getPaymentNo());
                    payCreateResult.setPayUrl(payRespData.getPayUrl());
                } else if(RespCodeEnum.EXECUTE_SUCCESS.getCode().equals(payRespData.getResultCode())){
                    payCreateResult.setStatus("false");
                    payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_DOING + "");
                    payCreateResult.setResultMessage("生成跳转地址失败[交易已成功]");
                    payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                    payCreateResult.setChannelOrderNo(payRespData.getPaymentNo());
                } else{
                    payCreateResult.setStatus("false");
                    payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");
                    payCreateResult.setResultMessage("生成跳转地址失败");
                    payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                    payCreateResult.setChannelOrderNo(payRespData.getPaymentNo());
                }
            }else{
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.CHANNEL_ERROR + "");
                payCreateResult.setResultMessage("生成跳转地址失败[接口调用失败]");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setChannelOrderNo(payRespData.getPaymentNo());
            }
            return payCreateResult;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道支付请求异常】-------");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");
        }
        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        logger.info("[xinbao create quickJump params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());// 1.加载通道信息

        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

        Map<String, String> params = new HashMap<>();

        /*** 公共请求参数 ***/
        //请求号
        String requestNo = shopPayDto.getSysPayOrderNo();
        //服务名称
        String service = "bankPay";
        //商户ID
        String partnerId = thirdChannelDto.getMerchantId();
        //签名方式
        String signType = "MD5";
        //异步通知地址
        String notifyUrl = thirdChannelDto.getNotifyUrl();

        /*** 请求参数 ***/
        //银行账户类型
        String accountType = "PERSON";
        //付款方名称
        String payerName = shopPayDto.getSysPayOrderNo();
        //支付金额
        String amount = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());
        //银行编码
        ApiResponse response = sysBankProvider.toChannelCode(shopPayDto.getBankCode(), thirdChannelDto.getId());
        if(!(ResponseCode.Base.SUCCESS+"").equals(response.getCode())) {
            logger.error("【通道不支持的该银行的支付请求】-------系统银行编码："+ shopPayDto.getBankCode());
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_THIRD_BANK+"");

            return payCreateResult;
        }
        String bankId = response.getData().toString();


        if(StringUtils.isNotBlank(requestNo)) {
            params.put("requestNo", requestNo);
        }
        if(StringUtils.isNotBlank(service)) {
            params.put("service", service);
        }
        if(StringUtils.isNotBlank(partnerId)) {
            params.put("partnerId", partnerId);
        }
        if(StringUtils.isNotBlank(signType)) {
            params.put("signType", signType);
        }
        if(StringUtils.isNotBlank(notifyUrl)) {
            params.put("notifyUrl", notifyUrl);
        }
        if(StringUtils.isNotBlank(accountType)) {
            params.put("accountType", accountType);
        }
        if(StringUtils.isNotBlank(payerName)) {
            params.put("payerName", payerName);
        }
        if(StringUtils.isNotBlank(amount)){
            params.put("amount", amount);
        }
        if(StringUtils.isNotBlank(bankId)) {
            params.put("bankId", bankId);
        }

        logger.info("[before MD5 sign] -> " + SignUtil.buildWaitingForSign(params));
        String sign = SignUtil.signMD5(SignUtil.buildWaitingForSign(params), thirdChannelDto.getPayMd5Key());
        params.put("sign", sign);

        logger.info("[xinbao sign msg]:signMsg:"+sign);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");

                return payCreateResult;
            }
            logger.info("pay post result == > "+ jsonStr);

            JSONObject jsonObject = JSONObject.fromObject(jsonStr);
            PayNotifyData payNotifyData= (PayNotifyData) JSONObject.toBean(jsonObject, PayNotifyData.class);

            if("true".equals(payNotifyData.getSuccess())) {
                if (RespCodeEnum.EXECUTE_PROCESSING.getCode().equals(payNotifyData.getResultCode())) {
                    payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                    payCreateResult.setStatus("true");
                    payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                    payCreateResult.setResultMessage(payNotifyData.getResultMessage());
                    payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                    payCreateResult.setChannelOrderNo(payNotifyData.getPaymentNo());
                    payCreateResult.setPayUrl(payNotifyData.getPayUrl());
                } else if(RespCodeEnum.EXECUTE_SUCCESS.getCode().equals(payNotifyData.getResultCode())){
                    payCreateResult.setStatus("false");
                    payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_DOING + "");
                    payCreateResult.setResultMessage("生成跳转地址失败[交易已成功]");
                    payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                    payCreateResult.setChannelOrderNo(payNotifyData.getPaymentNo());
                } else{
                    payCreateResult.setStatus("false");
                    payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");
                    payCreateResult.setResultMessage("生成跳转地址失败");
                    payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                    payCreateResult.setChannelOrderNo(payNotifyData.getPaymentNo());
                }
            }else{
                payCreateResult.setStatus("false");
                payCreateResult.setResultCode(SysPayResultConstants.CHANNEL_ERROR + "");
                payCreateResult.setResultMessage("生成跳转地址失败[接口调用失败]");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setChannelOrderNo(payNotifyData.getPaymentNo());
            }
            return payCreateResult;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道支付请求异常】-------");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");
        }
        return payCreateResult;
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
        return null;
    }
}
