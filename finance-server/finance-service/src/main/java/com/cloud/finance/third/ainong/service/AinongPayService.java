package com.cloud.finance.third.ainong.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.ainong.enums.RespCodeEnum;
import com.cloud.finance.third.ainong.utils.MD5Util;
import com.cloud.finance.third.ainong.vo.*;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
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
@Service("AinongPayService")
public class AinongPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(AinongPayService.class);

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
        logger.info("[ainong create H5 params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        String actionRespCode = SysPayResultConstants.SUCCESS_MAKE_ORDER+"";
        String actionRespMessage = "生成跳转地址成功";

        String actionRespUrl = getBasePayUrl() + "/d8/an_" + shopPayDto.getSysPayOrderNo() + ".html";
        String channelPayOrderNo = shopPayDto.getSysPayOrderNo();
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        if (actionRespCode.equals(SysPayResultConstants.SUCCESS_MAKE_ORDER+"")) {
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setResultCode(actionRespCode);
            payCreateResult.setStatus("true");
            payCreateResult.setResultMessage(actionRespMessage);
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setChannelOrderNo(channelPayOrderNo);
            payCreateResult.setPayUrl(actionRespUrl);

        } else {
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");
            payCreateResult.setStatus("false");
            payCreateResult.setResultMessage(actionRespMessage);
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setChannelOrderNo(channelPayOrderNo);
            payCreateResult.setPayUrl(actionRespUrl);
        }
        return payCreateResult;

    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        logger.info("[ailong create gateDirect params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());// 1.加载通道信息

        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");

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

        /**** 请求参数 ****/
        //商户订单号  商户系统产生的唯一订单号
        String out_trade_no = shopPayDto.getSysPayOrderNo();
        //银行ID
        ApiResponse response = sysBankProvider.toChannelCode(shopPayDto.getBankCode(), thirdChannelDto.getId());
        if(!(ResponseCode.Base.SUCCESS+"").equals(response.getCode())) {
            logger.error("【通道不支持的该银行的支付请求】-------系统银行编码："+ shopPayDto.getBankCode());
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_THIRD_BANK+"");

            return payCreateResult;
        }
        String bank_id = response.getData().toString();
        //订单金额  以“元”为单位，仅允许两位小数，必须大于零
        String total_fee = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());
        //商品名称  非必须字段
        String title = shopPayDto.getMerchantGoodsTitle() + shopPayDto.getSysPayOrderNo();
        //备注      非必须字段
        String remark = "";
        //支付结果通知地址  非必须字段
        String notify_url = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();

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
        if(StringUtils.isNotBlank(out_trade_no)) {
            params.put("out_trade_no", out_trade_no);
        }
        if(StringUtils.isNotBlank(bank_id)) {
            params.put("bank_id", bank_id);
        }
        if(StringUtils.isNotBlank(total_fee)){
            params.put("total_fee", total_fee);
        }
        if(StringUtils.isNotBlank(title)){
            params.put("title", title);
        }
        if(StringUtils.isNotBlank(remark)){
            params.put("remark", remark);
        }
        if(StringUtils.isNotBlank(notify_url)) {
            params.put("notify_url", notify_url);
        }

        logger.info("[before MD5 sign] -> " + ASCIISortUtil.buildSign(params, "=", "&key="+thirdChannelDto.getPayMd5Key()));
        sign = MD5Util.MD5Encode(ASCIISortUtil.buildSign(params, "=", "&key="+thirdChannelDto.getPayMd5Key()));
        params.put("sign", sign);

        logger.info("[ailong sign msg]:signMsg:"+sign);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");

                return payCreateResult;
            }
            logger.info("pay post result == > "+ jsonStr);

            net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(jsonStr);
            PayRespData payRespData = (PayRespData) net.sf.json.JSONObject.toBean(jsonObject, PayRespData.class);

            if(RespCodeEnum.CODE_SUCCESS.getCode().equals(payRespData.getCode())){
                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
                payCreateResult.setStatus("true");
                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER+"");
                payCreateResult.setResultMessage(payRespData.getMsg());
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                if(payRespData.getData() != null) {
                    payCreateResult.setChannelOrderNo(payRespData.getData().getBillno());
                    payCreateResult.setPayUrl(payRespData.getData().getPay_url());
                }
            }else{
                payCreateResult.setStatus("false");
                if(RespCodeEnum.CODE_PARAMS_ERROR.equals(payRespData.getCode())) {
                    payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");
                }else if(RespCodeEnum.CODE_SIGN_ERROR.equals(payRespData.getCode())) {
                    payCreateResult.setResultCode(SysPayResultConstants.ERROR_SIGN_RESULT_ERROR + "");
                }
                payCreateResult.setResultMessage("生成跳转地址失败");
                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
                if(payRespData.getData() != null) {
                    payCreateResult.setChannelOrderNo(payRespData.getData().getBillno());
                    payCreateResult.setPayUrl(payRespData.getData().getPay_url());
                }
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
        logger.info("[ainong query order]:orderNo:" + shopPay.getSysPayOrderNo());

        MidPayCheckResult checkResult = new MidPayCheckResult();
        checkResult.setStatus("false");

        Map<String, String> params = new HashMap<>();

        Date time = new Date();
        //请求参数
        /**** 请求参数 ****/
        //请求头
        //合作方标识号
        String partnerNo = thirdChannelDto.getMerchantId();
        //版本
        String version = "1.0.0";
        //字符集
        String charset = "UTF-8";
        //合作方类型
        String  partnerType = "OUTER";
        //交易代码
        String txnCode = "102010";
        //交易跟踪号
        String traceId = shopPay.getSysPayOrderNo()+"0";
        //请求日期  格式为yyyyMMdd
        String reqDate = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_11);
        //请求时间  格式为yyyyMMddHHmmss
        String reqTime = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_18);

        //交易请求参数
        //原交易跟踪号
        String oriTraceId = traceId;
        //原平台支付交易流水号
        String oriPlatformId = shopPay.getThirdChannelOrderNo();
        //
        String body = "pay action";

        QueryReqData queryReqData = new QueryReqData();
        HeadReqData headReqDTO = new HeadReqData();
        headReqDTO.setPartnerNo(partnerNo);
        headReqDTO.setVersion(version);
        headReqDTO.setCharset(charset);
        headReqDTO.setPartnerType(partnerType);
        headReqDTO.setTxnCode(txnCode);
        headReqDTO.setTraceId(traceId);
        headReqDTO.setReqDate(reqDate);
        headReqDTO.setReqTime(reqTime);
        queryReqData.setOriTraceId(oriTraceId);
        queryReqData.setOriPlatformId(oriPlatformId);
        queryReqData.setHead(headReqDTO);

        //签名
        String signJson = queryReqData.doSign(thirdChannelDto.getPayMd5Key());

        logger.info("[ailong before sign msg]:"+signJson);
        String sign = MD5Util.digest(signJson, "UTF-8");
        logger.info("[ailong sign msg]:"+sign);

        String jsonObject = net.sf.json.JSONObject.fromObject(queryReqData).toString();
        StringBuffer method = new StringBuffer();
        method.append("encryptData=").append(jsonObject).append("&");
        method.append("signData=").append(sign).append("&");
        method.append("partnerNo=").append(partnerNo);
        logger.info("pay post params == >  encryptData:"+ jsonObject + "; signData:" + sign);

        try {
            String jsonStr = PostUtils.sendPost(thirdChannelDto.getQueryUrl(), method.toString());
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                checkResult.setRespMsg("通道支付请求请求结果为空");
                return checkResult;
            }
            logger.info("query post result == > "+ jsonStr);

            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);
            String signCheck = respMap.get("signData");
            QueryRespData respData = JSONObject.parseObject(respMap.get("encryptData"), QueryRespData.class);
            if("000000".equals(respData.getHead().getRespCode())) {
                //验签
                signJson = respData.doSign(thirdChannelDto.getPayMd5Key());

                logger.info("[ailong before sign msg]:" + signJson);
                sign = MD5Util.digest(signJson, "UTF-8");
                logger.info("[ailong sign msg]:" + sign);
                if (StringUtils.isNotBlank(signCheck) && signCheck.equals(sign)) {
                    String temp = respData.getOriTraceId();
                    String sysOrderNo = temp != null ? temp.substring(0, temp.length() - 1) : null;

                    if (sysOrderNo != null && sysOrderNo.equals(shopPay.getSysPayOrderNo())) {
                        checkResult.setAmount(respData.getAmount());
                        checkResult.setRespMsg(respData.getHead().getRespMsg());
                        checkResult.setStatus("true");

                        logger.info("[ainong query order] 查询成功  orderNo:" + sysOrderNo + "; 实际支付金额：" + respData.getAmount());
                    } else {
                        logger.info("[ainong query order] 查询结果异常  单号不一致");
                        checkResult.setRespMsg("查询结果异常");
                        return checkResult;
                    }
                } else {
                    logger.info("[ainong query order] 回调签名错误");
                    checkResult.setRespMsg("通道回调签名错误");
                    return checkResult;
                }
            }else{
                logger.info("[ainong query order] " + respData.getHead().getRespMsg());
                checkResult.setRespMsg(respData.getHead().getRespMsg());
                return checkResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道查询请求异常】-------");
            checkResult.setRespMsg("通道查询请求异常");
        }
        return checkResult;
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
