package com.cloud.finance.third.hc.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.common.vo.pay.req.RepPayCreateData;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.ainong.enums.RespCodeEnum;
import com.cloud.finance.third.ainong.utils.MD5Util;
import com.cloud.finance.third.ainong.vo.AccountQueryData;
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
@Service("HcPayService")
public class HcPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(HcPayService.class);

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
        logger.info("[hc pay create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        MidPayCreateResult midPayCreateResult = new MidPayCreateResult();
        midPayCreateResult.setStatus("error");

        Map<String, String> params = new HashMap<>();

        /*** 公共参数 ***/
        //商户账号  商户在支付平台的唯一标识
        String assCode = thirdChannelDto.getMerchantId();
        String paymentType = "ali_h5_wake";
        String subPayCode = "ali_h5_wake";
        //签名
        String sign = "";
        //商户订单号  商户系统产生的唯一订单号
        String assPayOrderNo = shopPayDto.getSysPayOrderNo();
        //订单金额  以“元”为单位，仅允许两位小数，必须大于零
        String assPayMoney = SafeComputeUtils.numberFormate2(SafeComputeUtils.multiply(shopPayDto.getMerchantPayMoney(), 100D));
        String assPayMessage = shopPayDto.getMerchantGoodsTitle() + shopPayDto.getSysPayOrderNo();
        String assGoodsTitle = shopPayDto.getMerchantGoodsTitle();
        String assGoodsDesc = shopPayDto.getMerchantGoodsDesc();
        String assReturnUrl = shopPayDto.getMerchantReturnUrl();
        String assCancelUrl = shopPayDto.getMerchantCancelUrl();
        //支付结果通知地址  非必须字段
        String assNotifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();

        RepPayCreateData req = null;
        try {
            req = new RepPayCreateData(assCode, assPayOrderNo, assNotifyUrl, assReturnUrl,
                    assCancelUrl, paymentType, subPayCode, assPayMoney, assPayMessage, assGoodsTitle,
                    assGoodsDesc, thirdChannelDto.getPayMd5Key());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(StringUtils.isNotBlank(assCode)) {
            params.put("assCode", assCode);
        }
        if(StringUtils.isNotBlank(paymentType)) {
            params.put("paymentType", paymentType);
        }
        if(StringUtils.isNotBlank(subPayCode)) {
            params.put("subPayCode", subPayCode);
        }
        if(StringUtils.isNotBlank(assPayOrderNo)) {
            params.put("assPayOrderNo", assPayOrderNo);
        }
        if(StringUtils.isNotBlank(assPayMoney)) {
            params.put("assPayMoney", assPayMoney);
        }
        if(StringUtils.isNotBlank(assPayMessage)) {
            params.put("assPayMessage", assPayMessage);
        }
        if(StringUtils.isNotBlank(assGoodsTitle)) {
            params.put("assGoodsTitle", assGoodsTitle);
        }
        if(StringUtils.isNotBlank(assGoodsDesc)){
            params.put("assGoodsDesc", assGoodsDesc);
        }
        if(StringUtils.isNotBlank(assReturnUrl)){
            params.put("assReturnUrl", assReturnUrl);
        }
        if(StringUtils.isNotBlank(assCancelUrl)){
            params.put("assCancelUrl", assCancelUrl);
        }
        if(StringUtils.isNotBlank(assNotifyUrl)) {
            params.put("assNotifyUrl", assNotifyUrl);
        }

        params.put("sign", req.getSign());

        logger.info("[hc pay sign msg]:signMsg:"+req.getSign());

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                midPayCreateResult.setResultMessage(SysPayResultConstants.ERROR_SYS_PARAMS + "");

                return midPayCreateResult;
            }
            logger.info("hc pay post result == > "+ jsonStr);

            net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(jsonStr);

            if("10000".equals(jsonObject.get("code"))){
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                logger.info("【生成支付链接成功】");
                midPayCreateResult.setStatus(jsonObject.get("success").toString());
                midPayCreateResult.setResultMessage(jsonObject.get("message").toString());
                midPayCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
                midPayCreateResult.setPayUrl(jsonObject.get("payUrl").toString());
                midPayCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            }else{
                logger.info("【生成支付链接失败】");
                midPayCreateResult.setStatus(jsonObject.get("success").toString());
                midPayCreateResult.setResultMessage(jsonObject.get("message").toString());
                midPayCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
                midPayCreateResult.setPayUrl(jsonObject.get("payUrl").toString());
                midPayCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【生成支付链接请求异常】-------");

            midPayCreateResult.setResultMessage("请求异常");
            midPayCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");
            midPayCreateResult.setSysOrderNo(shopPayDto.getMerchantOrderNo());
        }

        return  midPayCreateResult;
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
        logger.info("[ainong query account]:channelId:" + thirdChannelDto.getId());

        ChannelAccountData channelAccountData = new ChannelAccountData();
        channelAccountData.setStatus(ChannelAccountData.STATUS_ERROR);

        Map<String, String> params = new HashMap<>();

        /*** 公共参数 ***/
        //appid
        String appid = thirdChannelDto.getAppId();
        //商户账号  商户在支付平台的唯一标识
        String mch_id = thirdChannelDto.getMerchantId();
        //随机字符串
        String nonce_str = new Date().getTime() + "";
        //签名方式
        String sign_type = "MD5";
        //签名
        String sign = "";
        //请求时间戳
        String timestamp = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18);

        if(StringUtils.isNotBlank(appid)) {
            params.put("appid", appid);
        }
        if(StringUtils.isNotBlank(mch_id)) {
            params.put("mch_id", mch_id);
        }
        if(StringUtils.isNotBlank(nonce_str)) {
            params.put("nonce_str", nonce_str);
        }
        if(StringUtils.isNotBlank(sign_type)) {
            params.put("sign_type", sign_type);
        }
        if(StringUtils.isNotBlank(timestamp)) {
            params.put("timestamp", timestamp);
        }

        logger.info("[before MD5 sign] -> " + ASCIISortUtil.buildSign(params, "=", "&key="+thirdChannelDto.getPayMd5Key()));
        sign = MD5Util.MD5Encode(ASCIISortUtil.buildSign(params, "=", "&key="+thirdChannelDto.getPayMd5Key()));
        params.put("sign", sign);

        logger.info("[ainong sign msg]:signMsg:"+sign);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getQueryUrl(), params);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                channelAccountData.setMsg("通道支付请求请求结果为空");
                return channelAccountData;
            }
            logger.info("query post result == > "+ jsonStr);

            net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(jsonStr);
            AccountQueryData accountQueryData = (AccountQueryData) net.sf.json.JSONObject.toBean(jsonObject, AccountQueryData.class);

            if(RespCodeEnum.CODE_SUCCESS.getCode().equals(accountQueryData.getCode())){
                channelAccountData.setStatus(ChannelAccountData.STATUS_SUCCESS);
                channelAccountData.setAmount(accountQueryData.getData().getAmount());
                channelAccountData.setFrozenAmount(accountQueryData.getData().getFreeze_amount());
            }else{
                logger.info("【通道查询失败】----> 查询结果状态错误：" + accountQueryData.getMsg());
                channelAccountData.setMsg("通道查询失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道查询请求异常】-------");
            channelAccountData.setMsg("通道查询请求异常");
        }
        return channelAccountData;
    }
}
