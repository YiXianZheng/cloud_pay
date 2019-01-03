package com.cloud.finance.third.hc.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.AccountInfoDto;
import com.cloud.finance.common.enums.RechargeStatusEnum;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopAccount;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopAccountService;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.ainong.utils.Base64Util;
import com.cloud.finance.third.ainong.utils.MD5Util;
import com.cloud.finance.third.ainong.vo.AinongAliPayCashRespData;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("HcCashService")
public class HcCashService implements BaseCashService {
    private static Logger logger = LoggerFactory.getLogger(HcCashService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private ShopAccountService shopAccountService;

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("...[hc cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        Map<String, String> params = new HashMap<>();

        String merchantCode = redisClient.Gethget(RedisConfig.THIRD_PAY_CHANNEL, shopRecharge.getThirdChannelId(), "merchantId");
        String payMd5Key = redisClient.Gethget(RedisConfig.THIRD_PAY_CHANNEL, shopRecharge.getThirdChannelId(), "payMd5Key");
        logger.info("...[hc cash] merchant code ["+ merchantCode+"]");

        if(shopRecharge.getRechargeMoney()>0) {
            params.put("amount", SafeComputeUtils.multiply(shopRecharge.getRechargeMoney(), 100D)+"");
        }
        if(StringUtils.isNotBlank(shopRecharge.getBankAccount())) {
            params.put("bankAccount", shopRecharge.getBankAccount());
        }
        if(StringUtils.isNotBlank(shopRecharge.getBankCode())) {
            params.put("bankCode", shopRecharge.getBankCode());
        }
        if(StringUtils.isNotBlank(shopRecharge.getBankNo())) {
            params.put("bankNo", shopRecharge.getBankNo());
        }
        if(StringUtils.isNotBlank(merchantCode)) {
            params.put("merCode", merchantCode);
        }
        if(StringUtils.isNotBlank(payMd5Key)) {
            params.put("key", payMd5Key);
        }
        if(StringUtils.isNotBlank(shopRecharge.getBankSubbranch())) {
            params.put("bankSubbranch", shopRecharge.getBankSubbranch());
        }
        if(StringUtils.isNotBlank(shopRecharge.getBankBin())) {
            params.put("bin", shopRecharge.getBankBin());
        }
        if(StringUtils.isNotBlank(shopRecharge.getProvince())) {
            params.put("province", shopRecharge.getProvince());
        }
        if(StringUtils.isNotBlank(shopRecharge.getCity())) {
            params.put("city", shopRecharge.getCity());
        }

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【代付请求请求结果为空】");
                cashRespData.setMsg("代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash post result == > "+ jsonObject.toJSONString());

            if(CashRespData.STATUS_SUCCESS.equals(jsonObject.get("code")) || CashRespData.STATUS_DOING.equals(jsonObject.get("code"))){
                logger.info("【代付成功】----");

                if(shopRecharge != null && RechargeStatusEnum.CASH_STATUS_WAIT.getStatus() ==shopRecharge.getRechargeStatus()){
                    shopRecharge.setRechargeStatus(1);
                    shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                    shopRecharge.setThirdChannelRespMsg(jsonObject.toJSONString());
                    shopRecharge.setCompleteTime(new Date());
                    shopRechargeService.rechargeSuccess(shopRecharge);

                    logger.info("【代付成功】---- " + jsonObject.get("msg"));
                    cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                    cashRespData.setMsg("代付成功["+jsonObject.get("msg")+"]");
                }else{
                    logger.info("【代付失败】----" + jsonObject.get("msg"));
                    cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                    cashRespData.setMsg("代付成功["+jsonObject.get("msg")+"]");
                }
            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【代付失败】----> 代付结果：" + jsonStr);

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(jsonObject.get("message").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【代付请求异常】-------");
            cashRespData.setMsg("代付请求异常");
        }
        return cashRespData;
    }

    @Override
    public CashRespData queryCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {
        return null;
    }

    @Override
    public CashRespData adminApplyCash(CashReqData cashReqData, ThirdChannelDto thirdChannelDto) {
        return null;
    }

}
