package com.cloud.finance.common.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.enums.PayStatusEnum;
import com.cloud.finance.common.utils.HttpClientUtil;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.pay.mes.MesPayNotifyData;
import com.cloud.finance.po.ShopPay;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.redis.RedisConstants;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 *
 * 类描述：商户支付服务类
 *
 */
@Service
@Transactional
public class MerchantPayService {
	
	private static Logger logger = LoggerFactory.getLogger(MerchantPayService.class);

	@Autowired
	private RedisClient redisClient;
	@Autowired
	private MerchantUserProvider merchantUserProvider;
	

	@Transactional(readOnly = false)
	public boolean notifyAssWithMd5Key(ShopPay shopPay) throws Exception{
		if(shopPay == null) return false;
		logger.info("[notify merchant by orderid]:"+shopPay.getMerchantOrderNo());
		Map<String, String> merchant = redisClient.Gethgetall(RedisConstants.MERCHANT_INFO_DB, shopPay.getMerchantCode());
		if(merchant == null || merchant.size()==0){
			ApiResponse apiResponse = merchantUserProvider.initMerchantToRedis(shopPay.getMerchantCode());
			if(apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())){
                merchant = redisClient.Gethgetall(RedisConstants.MERCHANT_INFO_DB, shopPay.getMerchantCode());
			}else{
				return false;
			}
		}

		String assCode = shopPay.getMerchantCode();
		String assPayOrderNo = shopPay.getMerchantOrderNo();
		String sysPayOrderNo = shopPay.getSysPayOrderNo();
		Double returnDouble=SafeComputeUtils.multiply(shopPay.getMerchantPayMoney(), 100D);
		String assPayMoney = returnDouble.intValue()+"";
		SimpleDateFormat formatter;
	    formatter = new SimpleDateFormat ("yyyyMMddHHmmss");
		String succTime;
		if(null==shopPay.getPayCompleteTime()){
			succTime =formatter.format(new Date());
		}else{
			succTime =formatter.format(shopPay.getPayCompleteTime());
		}

		//商户添加默认回调地址
		//返回地址
		String respCode = shopPay.getPayStatus()+"";
		String respMsg = PayStatusEnum.getByStatus(shopPay.getPayStatus());
		String md5Key = merchant.get("md5Key");
		String notifyUrl = shopPay.getMerchantNotifyUrl();
		String assPayMessage = shopPay.getMerchantPayMessage();
		
		if (StringUtils.isEmpty(assPayMessage)) {
			assPayMessage = sysPayOrderNo;
		}
		String notifyResponseResult;
		if (StringUtils.isNotEmpty(notifyUrl)) {
            MesPayNotifyData res = new MesPayNotifyData(assCode, assPayOrderNo,sysPayOrderNo, assPayMoney, assPayMessage, succTime,respCode, respMsg, md5Key);
            Map<String, String>  notifyMap = res.getReturnParamMap();
            logger.info("notify response sign result : " +res.getSign() + "; notify url : " + notifyUrl);
            notifyResponseResult = HttpClientUtil.post(notifyUrl, notifyMap);
			if (StringUtils.isEmpty(notifyResponseResult) && "success".equalsIgnoreCase(notifyResponseResult)) {
				logger.error("notify response error result,sysPayOrderNo:"+ sysPayOrderNo+",notifyResult:"+notifyResponseResult);
			} else {
				notifyMap.put("notifyUrl", notifyUrl);
                notifyMap.put("notifyTime", new Date().getTime()+"");
				redisClient.lpush(RedisConfig.UN_RESPONSE_NOTIFY, DateUtil.DateToString(new Date(), sysPayOrderNo+"-"+DateUtil.DATE_PATTERN_18),
						JSONObject.toJSONString(notifyMap));
				logger.info("notify response success result 【进入通知队列】");
			}
            logger.info("notify response success result,sysPayOrderNo:"+ sysPayOrderNo + ",notifyResult: success~~~~" );
		}
		return true;
	}

	public String returnAssWithMd5Key(ShopPay shopPay) throws Exception{
		if(shopPay == null) return "http://www.baidu.com";
		logger.info("[notify merchant by orderid]:"+shopPay.getMerchantOrderNo());
		Map<String, String> merchant = redisClient.Gethgetall(RedisConstants.MERCHANT_INFO_DB, shopPay.getMerchantCode());
		if(merchant == null || merchant.size()==0){
			ApiResponse apiResponse = merchantUserProvider.initMerchantToRedis(shopPay.getMerchantCode());
			if(apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())){
                merchant = redisClient.Gethgetall(RedisConstants.MERCHANT_INFO_DB, shopPay.getMerchantCode());
			}else{
				return "http://www.baidu.com";
			}
		}

		String assCode = shopPay.getMerchantCode();
		String assPayOrderNo = shopPay.getMerchantOrderNo();
		String sysPayOrderNo = shopPay.getSysPayOrderNo();
		Double returnDouble=SafeComputeUtils.multiply(shopPay.getMerchantCostMoney(), 100D);
		String assPayMoney = returnDouble.intValue()+"";
		SimpleDateFormat formatter;
	    formatter = new SimpleDateFormat ("yyyyMMddHHmmss");
		String succTime;
		if(null==shopPay.getPayCompleteTime()){
			succTime =formatter.format(new Date());
		}else{
			succTime =formatter.format(shopPay.getPayCompleteTime());
		}
		//返回订单状态
        String respCode = shopPay.getPayStatus()+"";
        String respMsg = PayStatusEnum.getByStatus(shopPay.getPayStatus());
        String md5Key = merchant.get("md5Key");
        String returnUrl = shopPay.getMerchantReturnUrl();
        String assPayMessage = shopPay.getMerchantPayMessage();

		if(StringUtils.isEmpty(returnUrl)){
			//商户添加默认回调地址
			returnUrl = "http://www.baidu.com";
		}
		String resultFinalUrl;

		if(StringUtils.isNotEmpty(returnUrl)){
			
			MesPayNotifyData res = new MesPayNotifyData(assCode,assPayOrderNo,sysPayOrderNo,assPayMoney,assPayMessage,succTime,respCode,respMsg,md5Key);
			String returnParamValue = res.getReturnParamUrlValueEncode();
			String retFinalParamValue = returnParamValue.substring(0, returnParamValue.length()-1);
			resultFinalUrl = returnUrl+"?"+retFinalParamValue;
		}else{
			resultFinalUrl = "http://www.baidu.com";
		}
		return resultFinalUrl;
	}

}
