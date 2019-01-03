package com.cloud.finance.third.shtd1.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.shtd1.util.MD5Utils;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("Shtd1CashService")
public class Shtd1CashService implements BaseCashService {
    private static Logger logger = LoggerFactory.getLogger(Shtd1CashService.class);

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

        logger.info("...[shanghai channel 1 alipay cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        /*** 公共参数 ***/
        //商户账号  商户在支付平台的唯一标识
        String userid = thirdChannelDto.getMerchantId();
        //交易命令
        String orderCode = "sq_withdraw";
        /** 代付请求参数 **/
        //下游订单号
        String pay_number = shopRecharge.getRechargeNo();
        //订单金额  以“元”为单位，仅允许两位小数，必须大于零
        String amount = SafeComputeUtils.numberFormate2(SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney()));
        //银行卡号
        String cardNumber = shopRecharge.getBankNo();
        //姓名
        String cardName = shopRecharge.getBankAccount();
        //开户行代码
        String cardNo = "";
        logger.error("【代付请求】 bank code :" + shopRecharge.getBankCode());
        ApiResponse apiResponse = sysBankProvider.toChannelCode(shopRecharge.getBankCode(), thirdChannelDto.getId());
        logger.error("【代付请求】 bank response :" + apiResponse);
        if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())){
            cardNo = apiResponse.getData().toString();
        }else{
            logger.error("【代付请求失败】不支持的银行");
            cashRespData.setMsg("【代付请求失败】不支持的银行");
            return cashRespData;
        }
        String channel_code = "scanPay";
        String cardType = "1";
        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();

        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("notifyUrl", notifyUrl);
        params.put("cardNo", cardNo);
        params.put("cardName", cardName);
        params.put("cardNumber", cardNumber);
        params.put("userid", userid);
        params.put("cardType", cardType);

        //签名
        String sign = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 before sign msg]:"+sign);
        sign = MD5Utils.getKeyedDigest(sign, thirdChannelDto.getPayMd5Key());
        logger.info("[shanghai channel 1 sign msg]:"+sign);

        params.put("sign", sign);
        params.put("orderCode", orderCode);
        params.put("pay_number", pay_number);
        params.put("channel_code", channel_code);

        String baowen = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 request params]:"+baowen);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【通道代付请求请求结果为空】");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash post result == > "+ jsonObject.toJSONString());

            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("0000".equals(respMap.get("respCode")) && "success".equals(respMap.get("status"))){
                logger.info("【通道代付受理成功】----");

                shopRecharge.setRechargeStatus(2);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelOrderNo(respMap.get("orderId"));
                shopRecharge.setThirdChannelRespMsg(jsonObject.toJSONString());
                shopRechargeService.rechargeSuccess(shopRecharge);

                logger.info("【通道代付成功】---- " + respMap.get("respInfo"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功["+respMap.get("respInfo")+"]");

            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("respCode") + "--->" + respMap.get("respInfo"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("respInfo"));
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
        logger.info("...[shanghai channel 1 alipay cash query] query cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        /*** 公共参数 ***/
        //商户账号  商户在支付平台的唯一标识
        String userid = thirdChannelDto.getMerchantId();
        //交易命令
        String orderCode = "sq_withdrawQuery";
        /** 查询请求参数 **/
        //下游订单号
        String pay_number = shopRecharge.getRechargeNo();

        Map<String, String> params = new HashMap<>();
        params.put("userid", userid);

        //签名
        String sign = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 before sign msg]:"+sign);
        sign = MD5Utils.getKeyedDigest(sign, thirdChannelDto.getPayMd5Key());
        logger.info("[shanghai channel 1 sign msg]:"+sign);

        params.put("sign", sign);
        params.put("orderCode", orderCode);
        params.put("pay_number", pay_number);

        String baowen = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 request params]:"+baowen);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【通道代付请求请求结果为空】");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash post result == > "+ jsonObject.toJSONString());

            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("0000".equals(respMap.get("respCode")) && "4".equals(respMap.get("status"))){
                logger.info("【通道代付成功】----");

                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelOrderNo(respMap.get("orderId"));
                shopRecharge.setThirdChannelRespMsg(jsonObject.toJSONString());
                shopRechargeService.rechargeSuccess(shopRecharge);

                logger.info("【通道代付成功】---- " + respMap.get("respInfo"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功["+respMap.get("respInfo")+"]");

            }else if("0000".equals(respMap.get("respCode")) && "3".equals(respMap.get("status"))){
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("respCode") + "--->" + respMap.get("respInfo"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("respInfo"));
            }else{
                logger.info("【通道代付处理中】------ ");

                cashRespData.setStatus(CashRespData.STATUS_DOING);
                cashRespData.setMsg(respMap.get("respInfo"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
        }
        return cashRespData;
    }

    @Override
    public CashRespData adminApplyCash(CashReqData cashReqData, ThirdChannelDto thirdChannelDto) {
        logger.info("...[shanghai channel 1 alipay admin cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");


        /*** 公共参数 ***/
        //商户账号  商户在支付平台的唯一标识
        String userid = thirdChannelDto.getMerchantId();
        //交易命令
        String orderCode = "gm_withdraw";
        /** 代付请求参数 **/
        //下游订单号
        String pay_number = "S" + DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13) +"0";
        //支付结果通知地址
        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
        //订单金额  以“元”为单位，仅允许两位小数，必须大于零
        String amount = SafeComputeUtils.numberFormate(cashReqData.getAmount());
        //银行卡号
        String cardNo = cashReqData.getBankNo();
        //姓名
        String cardName = cashReqData.getBankAccount();
        //总行
        String headquartersBankName = "";
        logger.error("【代付请求】 bank code :" + cashReqData.getBankCode());
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(cashReqData.getBankCode());
        logger.error("【代付请求】 bank response :" + apiResponse);
        if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())){
            headquartersBankName = apiResponse.getData().toString();
        }else{
            logger.error("【代付请求失败】不支持的银行");
            cashRespData.setMsg("【代付请求失败】不支持的银行");
            return cashRespData;
        }
        //01-个人；02-企业
        String customerType = "01";
        //01借记卡  04企业对公银行账户
        String accountTypeCode = "01";
        String channel_code = "scanPay";

        Map<String, String> params = new HashMap<>();
        params.put("amount", amount);
        params.put("notifyUrl", notifyUrl);
        params.put("cardNo", cardNo);
        params.put("cardName", cardName);
        params.put("customerType", customerType);
        params.put("accountTypeCode", accountTypeCode);
        params.put("userid", userid);
        params.put("headquartersBankName", headquartersBankName);

        //签名
        String sign = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 before sign msg]:"+sign);
        sign = MD5Utils.getKeyedDigest(sign, thirdChannelDto.getPayMd5Key());
        logger.info("[shanghai channel 1 sign msg]:"+sign);

        params.put("sign", sign);
        params.put("orderCode", orderCode);
        params.put("pay_number", pay_number);
        params.put("channel_code", channel_code);

        String baowen = MD5Utils.getSignParam(params);
        logger.info("[shanghai channel 1 request params]:"+baowen);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【通道代付请求请求结果为空】");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash post result == > "+ jsonObject.toJSONString());

            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("0000".equals(respMap.get("respCode"))){
                logger.info("【通道代付受理成功】----");

                logger.info("【通道代付成功】---- " + respMap.get("respInfo"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功["+respMap.get("respInfo")+"]");

            }else{
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("respCode") + "--->" + respMap.get("respInfo"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("respInfo"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
        }
        return cashRespData;
    }

}
