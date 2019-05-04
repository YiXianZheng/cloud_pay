package com.cloud.apigateway.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cloud.apigateway.service.MerchantApiFilterService;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/8/13 16:27
 * @Description:
 */
@Service
public class MerchantApiFilterServiceImpl implements MerchantApiFilterService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private MerchantUserProvider merchantUserProvider;

    @Override
    public ApiResponse checkApi(HttpServletRequest request) {


        String merchantCode = request.getParameter("assCode");      //商户号
        String paymentType = request.getParameter("paymentType");      //支付方式
        String merchantMoneyStr = request.getParameter("assPayMoney");

        if (StringUtils.isEmpty(merchantMoneyStr)) {
            logger.error("【merchant pay in api filter】 Fail of 订单金额为空");
            return ApiResponse.creatFail(new ResponseCode.COMMON(SysPayResultConstants.ERROR_PAY_AMOUNT_PARAM,"[assPayMoney]订单金额不能为空"));
        }

        logger.info("【merchant pay in api filter】 merchantCode:" + merchantCode + "; paymentType:"+ paymentType + "; money: " + merchantMoneyStr);

        Double merchantPayMoneyYuan = SafeComputeUtils.div(Double.parseDouble(merchantMoneyStr), 100D);

        if(StringUtils.isEmpty(merchantCode)){
            logger.error("【merchant pay in api filter】 Fail of merchantCode null ");
            return ApiResponse.creatFail(new ResponseCode.COMMON(SysPayResultConstants.ERROR_PAY_MERCHANT_ID_NULL,"[assCode]商户编号号不能为空"));
        }
        if(StringUtils.isBlank(paymentType)){
            logger.error("【merchant pay in api filter】 Fail of paymentType null ");
            return ApiResponse.creatFail(new ResponseCode.COMMON(SysPayResultConstants.ERROR_PAY_MENT_TYPE_NULL,"[paymentType]支付类型参数为空"));
        }

        Map<String, String> map = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);

        if(map == null || map.size()==0){
            ApiResponse apiResponse = merchantUserProvider.initMerchantToRedis(merchantCode);
            if(apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())){
                map = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
            }else{
                return apiResponse;
            }
        }

        ApiResponse authResponse = null;
        if(null==map || map.size()==0){
            logger.info("【merchant pay in api filter】 Fail of merchant code ");
            authResponse = ApiResponse.creatFail(new ResponseCode.COMMON(SysPayResultConstants.ERROR_PAY_MERCHANT_NOT_REGISTED,"["+merchantCode+"]商户号不存在"));
            return authResponse;
        }
        if(StringUtils.isBlank(map.get("activePayChannels")) || map.get("activePayChannels").toString().indexOf(paymentType)<0){
            logger.info("【merchant pay in api filter】 Fail of paymentType");
            authResponse =  ApiResponse.creatFail(new ResponseCode.COMMON(SysPayResultConstants.ERROR_PAY_MERCHANT_NOT_REGISTED,"["+merchantCode+"]商户号未开通["+paymentType+"]支付功能"));
            return authResponse;
        }

        if(!"1".equals(map.get("payStatus"))){
            logger.info("【merchant pay in api filter】 Fail of payStatus ");
            authResponse =  ApiResponse.creatFail(new ResponseCode.COMMON(SysPayResultConstants.ERROR_MERCHANT_AUTH, "["+ merchantCode +"]商户暂不接发起支付,如有异议请联系客服"));
            return authResponse;
        }
        //取出商户今日已支付总金额
        String jsonStr = redisClient.Gethget(RedisConfig.MERCHANT_DAILY_PAY_COUNT_DB, merchantCode,
                DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_11));

        double dailyTotal = 0D;
        if(StringUtils.isNotBlank(jsonStr)) {
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);
            if(StringUtils.isNotBlank(respMap.get("totalSuccessMoney")))
                dailyTotal = Double.parseDouble(respMap.get("totalSuccessMoney"));
        }

        String limitStr = "0";
        if(map.get("dailyLimit")!=null && StringUtils.isNotBlank(map.get("dailyLimit"))){
            limitStr = map.get("dailyLimit");
        }
        double dailyLimit = Double.parseDouble(limitStr);
        logger.info("【merchant pay in api filter】 dailyTotal:" + dailyTotal + "; dailyLimit:"+ dailyLimit);
        if(SafeComputeUtils.add(dailyTotal, merchantPayMoneyYuan) > dailyLimit){
            authResponse =  ApiResponse.creatFail(new ResponseCode.COMMON(SysPayResultConstants.ERROR_MERCHANT_AUTH, "["+ merchantCode +"]商户暂不接发起支付[超过每日上限]"));
            logger.info("【merchant pay in api filter】 Fail of dailyLimit ");
            return authResponse;
        }

        return authResponse;
    }
}
