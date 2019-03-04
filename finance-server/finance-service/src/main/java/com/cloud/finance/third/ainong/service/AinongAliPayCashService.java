package com.cloud.finance.third.ainong.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.enums.RechargeStatusEnum;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.ainong.utils.Base64Util;
import com.cloud.finance.third.ainong.utils.MD5Util;
import com.cloud.finance.third.ainong.vo.*;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("AinongAliPayCashService")
public class AinongAliPayCashService implements BaseCashService {
    private static Logger logger = LoggerFactory.getLogger(AinongAliPayCashService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("...[ainong alipay cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Date time = new Date();
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
        String txnCode = "102002";
        //交易跟踪号
        String traceId = shopRecharge.getRechargeNo();
        //请求日期  格式为yyyyMMdd
        String reqDate = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_11);
        //请求时间  格式为yyyyMMddHHmmss
        String reqTime = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_18);

        //交易请求参数
        //交易金额  单位元
        String txnAmt = SafeComputeUtils.numberFormate2(SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney()));
        //银行卡号
        String accountNo = shopRecharge.getBankNo();
        //姓名
        String accountName = shopRecharge.getBankAccount();
        //总行
        String bankName;
        logger.info("【代付请求】 bank code : " + shopRecharge.getBankCode());
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        logger.info("【代付请求】 bank response : " + apiResponse);
        if((ResponseCode.Base.SUCCESS.getCode() + "").equals(apiResponse.getCode())){
            bankName = apiResponse.getData().toString();
        }else{
            cashRespData.setMsg("【代付请求失败】不支持的银行");
            logger.error("【代付请求失败】不支持的银行");
            return cashRespData;
        }
        //银行联行号
        String bankBranchNo = shopRecharge.getBankBin();
        //通知地址
        String callBackUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();

        AilongDfReqData reqData = new AilongDfReqData();
        HeadReqData headReqDTO = new HeadReqData();
        headReqDTO.setPartnerNo(partnerNo);
        headReqDTO.setVersion(version);
        headReqDTO.setCharset(charset);
        headReqDTO.setPartnerType(partnerType);
        headReqDTO.setTxnCode(txnCode);
        headReqDTO.setTraceId(traceId);
        headReqDTO.setReqDate(reqDate);
        headReqDTO.setReqTime(reqTime);
        reqData.setAccountName(accountName);
        reqData.setAccountNo(accountNo);
        reqData.setBankName(bankName);
        reqData.setBankBranchNo(bankBranchNo);
        reqData.setTxnAmt(txnAmt);
        reqData.setCallBackUrl(callBackUrl);
        reqData.setPayType(thirdChannelDto.getAppKey());
        reqData.setHead(headReqDTO);

        //签名
        String signJson = reqData.doSign(thirdChannelDto.getPayMd5Key());

        logger.info("[ailong before sign msg]: " + signJson);
        String sign = MD5Util.digest(signJson, "UTF-8");
        logger.info("[ailong sign msg]:"+sign);

        Map<String, String> method = new HashMap<>();
        String jsonString = JSONObject.toJSONString(reqData);
        method.put("encryptData", jsonString);
        method.put("signData", sign);
        method.put("partnerNo", partnerNo);
        logger.info("pay post params == >  encryptData:" + jsonString + "; signData:" + sign);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), method);

            logger.info("cash post result == > "+ jsonStr);
            if(jsonStr == null){
                logger.error("【通道代付请求请求结果为空】");
                cashRespData.setStatus("false");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }

            //获取返回报文
            Map<Object, Object> respMap = MapUtils.json2Map(jsonStr);
            AilongDfRespData respData = JSONObject.parseObject(respMap.get("encryptData").toString(), AilongDfRespData.class);

            if("000000".equals(respData.getHead().getRespCode()) || "000001".equals(respData.getHead().getRespCode())){
                logger.info("【通道代付成功】----");

                if(RechargeStatusEnum.CASH_STATUS_WAIT.getStatus().equals(shopRecharge.getRechargeStatus())){
                    shopRecharge.setRechargeStatus(1);
                    shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                    shopRecharge.setThirdChannelOrderNo(respData.getHead().getPlatformId());
                    shopRecharge.setThirdChannelRespMsg(jsonStr);
                    shopRecharge.setCompleteTime(new Date());
                    shopRechargeService.rechargeSuccess(shopRecharge);

                    logger.info("【通道代付成功】---- " + respData.getHead().getRespMsg());
                    cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                    cashRespData.setMsg("代付成功["+respData.getHead().getRespMsg()+"]");
                }else{
                    logger.info("【通道】----" + respData.getHead().getRespMsg());
                    cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                    cashRespData.setMsg("代付成功["+respData.getHead().getRespMsg()+"]");
                }
            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.error("【通道代付失败】----> 代付结果状态错误：" + respData.getHead().getRespCode() + "--->"
                        + respData.getHead().getRespMsg());

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respData.getHead().getRespMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
        }
        return cashRespData;
    }

    @Override
    public CashRespData queryCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {
        return null;
    }

    @Override
    public CashRespData adminApplyCash(CashReqData cashReqData, ThirdChannelDto thirdChannelDto) {
        logger.info("...[ainong alipay admin cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        Date time = new Date();
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
        String txnCode = "102002";
        //交易跟踪号
        String traceId = "S" + DateUtil.DateToString(time, DateUtil.DATE_PATTERN_13) +"0";
        //请求日期  格式为yyyyMMdd
        String reqDate = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_11);
        //请求时间  格式为yyyyMMddHHmmss
        String reqTime = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_18);

        //交易请求参数
        //交易金额  单位元
        String txnAmt = SafeComputeUtils.numberFormate2(cashReqData.getAmount());
        //银行卡号
        String accountNo = cashReqData.getBankNo();
        //姓名
        String accountName = cashReqData.getBankAccount();
        //总行
        String bankName = "";
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(cashReqData.getBankCode());
        if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())){
            bankName = apiResponse.getData().toString();
        }else{
            logger.error("【admin代付请求失败】不支持的银行");
            cashRespData.setMsg("【代付请求失败】不支持的银行");
            return cashRespData;
        }
        //银行联行号
        String bankBranchNo = cashReqData.getBin();
        //通知地址
        String callBackUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();

        AilongDfReqData reqData = new AilongDfReqData();
        HeadReqData headReqDTO = new HeadReqData();
        headReqDTO.setPartnerNo(partnerNo);
        headReqDTO.setVersion(version);
        headReqDTO.setCharset(charset);
        headReqDTO.setPartnerType(partnerType);
        headReqDTO.setTxnCode(txnCode);
        headReqDTO.setTraceId(traceId);
        headReqDTO.setReqDate(reqDate);
        headReqDTO.setReqTime(reqTime);
        reqData.setAccountName(accountName);
        reqData.setAccountNo(accountNo);
        reqData.setBankName(bankName);
        reqData.setBankBranchNo(bankBranchNo);
        reqData.setTxnAmt(txnAmt);
        reqData.setCallBackUrl(callBackUrl);
        reqData.setHead(headReqDTO);

        //签名
        String signJson = reqData.doSign(thirdChannelDto.getPayMd5Key());

        logger.info("[ailong before sign msg]:"+signJson);
        String sign = MD5Util.digest(signJson, "UTF-8");
        logger.info("[ailong sign msg]:"+sign);

        Map<String, String> method = new HashMap<>();
        String jsonString = JSONObject.toJSONString(reqData);
        method.put("encryptData", jsonString);
        method.put("signData", sign);
        method.put("partnerNo", partnerNo);
        logger.info("pay post params == >  encryptData:"+ jsonString + "; signData:" + sign);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), method);
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【admin通道代付请求请求结果为空】");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("admin cash post result == > "+ jsonObject.toJSONString());

            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);
            AilongDfRespData respData = JSONObject.parseObject(respMap.get("encryptData"), AilongDfRespData.class);
            //同步返回签名
            String respSign = respMap.get("signData");
            //同步返回报文签名
            String respJsonSign = MD5Util.digest(respData.doSign(thirdChannelDto.getPayMd5Key()), "UTF-8");
            if (!respSign.equals(respJsonSign)) {
                logger.error("【admin通道代付请求失败】回调签名错误");
                cashRespData.setMsg("【通道代付请求失败】回调签名错误");
                return cashRespData;
            }

            if("000000".equals(respData.getHead().getRespCode()) || "000001".equals(respData.getHead().getRespCode())){
                logger.info("【admin通道代付成功】---- " + respData.getHead().getRespMsg());
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功["+respData.getHead().getRespMsg()+"]");

            }else{
                logger.info("【admin通道代付失败】----> 代付结果状态错误：" + respData.getHead().getRespCode() + "--->"
                        + respData.getHead().getRespMsg());

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respData.getHead().getRespMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【admin通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
        }
        return cashRespData;
    }

}
