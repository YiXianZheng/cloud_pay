package com.cloud.finance.third.jinxin.service;

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
import com.cloud.finance.third.jinxin.utils.MD5Util;
import com.cloud.finance.third.jinxin.utils.PayUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.provider.SysBankProvider;
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
@Service("JinxinPayService")
public class JinxinPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(JinxinPayService.class);

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
        logger.info("[jinxin pay create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

        //请求参数
        String pay_bankcode = "904";  //银行编码 支付宝
        String pay_memberid = thirdChannelDto.getMerchantId();//商户id
        String pay_orderid = PayUtil.generateOrderId();//20位订单号 时间戳+6位随机字符串组成
        String pay_applydate = PayUtil.generateTime();//yyyy-MM-dd HH:mm:ss
        String pay_notifyurl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();//通知地址
        String pay_callbackurl = shopPayDto.getMerchantReturnUrl();//回调地址
        String pay_amount = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());
        String pay_attach = shopPayDto.getSysPayOrderNo();
        String pay_productname = shopPayDto.getMerchantGoodsTitle();
        String pay_productnum="";
        String pay_productdesc="";
        String pay_producturl="";

        Map<String, String> params = new HashMap<>();
        if(StringUtils.isNotBlank(pay_bankcode)) {
            params.put("pay_bankcode", pay_bankcode);
        }
        if(StringUtils.isNotBlank(pay_memberid)) {
            params.put("pay_memberid", pay_memberid);
        }
        if(StringUtils.isNotBlank(pay_orderid)) {
            params.put("pay_orderid", pay_orderid);
        }
        if(StringUtils.isNotBlank(pay_applydate)) {
            params.put("pay_applydate", pay_applydate);
        }
        if(StringUtils.isNotBlank(pay_notifyurl)) {
            params.put("pay_notifyurl", pay_notifyurl);
        }
        if(StringUtils.isNotBlank(pay_callbackurl)) {
            params.put("pay_callbackurl", pay_callbackurl);
        }
        if(StringUtils.isNotBlank(pay_amount)) {
            params.put("pay_amount", pay_amount);
        }

        //签名
        String stringSignTemp="pay_amount="+pay_amount+"&pay_applydate="+pay_applydate+"&pay_bankcode="+pay_bankcode+
                "&pay_callbackurl="+pay_callbackurl+"&pay_memberid="+pay_memberid+"&pay_notifyurl="+pay_notifyurl+
                "&pay_orderid="+pay_orderid+"&key="+thirdChannelDto.getPayMd5Key()+"";
        logger.info("[jinxin before sign msg]:"+ stringSignTemp +"; key:" +thirdChannelDto.getPayMd5Key());
        String signStr = "";
        try {
            signStr = MD5Util.md5(stringSignTemp);
        }catch (Exception e){
            logger.error("【系统计算签名错误】-----------");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");

            return payCreateResult;
        }
        logger.info("[jinxin sign msg]:"+signStr);

        params.put("pay_md5sign", signStr);
        if(StringUtils.isNotBlank(pay_attach)) {
            params.put("pay_attach", pay_attach);
        }
        if(StringUtils.isNotBlank(pay_productname)) {
            params.put("pay_productname", pay_productname);
        }
        if(StringUtils.isNotBlank(pay_productnum)) {
            params.put("pay_productnum", pay_productnum);
        }
        if(StringUtils.isNotBlank(pay_productdesc)) {
            params.put("pay_productdesc", pay_productdesc);
        }
        if(StringUtils.isNotBlank(pay_producturl)) {
            params.put("pay_producturl", pay_producturl);
        }

        logger.info("pay post params == > "+ PayUtil.getSignParam(params));

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");

                return payCreateResult;
            }
            logger.info("pay post result == > "+ jsonStr);

            Map<String, Object> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if(respMap.get("status") != null && "ok".equalsIgnoreCase(respMap.get("status").toString())){
                Map<String, String> data = (Map<String, String>) respMap.get("data");

                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER+"");
                payCreateResult.setResultMessage(data.get("body"));
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                payCreateResult.setChannelOrderNo(pay_orderid);
                payCreateResult.setPayUrl(data.get("qrcodeUrl"));
                logger.info("【通道支付请求成功】-------成功生成支付链接");
            }else{
                payCreateResult.setStatus("false");
                payCreateResult.setResultMessage("生成跳转地址失败");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                logger.error("【通道支付请求失败】-------"+jsonStr);
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
