package com.cloud.finance.third.caocao.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.HttpClientUtil;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.MD5Util;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("CaocaoCashService")
public class CaocaoCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(CaocaoCashService.class);

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

        logger.info("[caocao cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Map<String, String> params = new HashMap<>();
        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        String mchOrderNo = shopRecharge.getRechargeNo();
        String mchId = thirdChannelDto.getMerchantId();
        String settAmount = String.valueOf(shopRecharge.getRechargeMoney());
        String bankNetName = shopRecharge.getBankSubbranch();
        ApiResponse response = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        String bankName = response.getData().toString();
        String accountName = shopRecharge.getBankAccount();
        String accountNo = shopRecharge.getBankNo();
        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();

        params.put("mchOrderNo", mchOrderNo);
        params.put("mchId", mchId);
        params.put("appId", thirdChannelDto.getAppId());
        params.put("settAmount", settAmount);
        params.put("bankNetName", bankNetName);
        params.put("bankName", bankName);
        params.put("accountName", accountName);
        params.put("accountNo", accountNo.trim());
        params.put("notifyUrl", notifyUrl);

        String signBefore = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[caocao cash sign before msg]: " + signBefore);

        String signStr = MD5Util.md5(signBefore).toUpperCase();

        logger.info("[caocao cash sign result]: " + signStr);
        params.put("sign", signStr);

        try {
            String jsonStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
            logger.info("cash post result == > " + jsonStr);
            HashMap respMap = JSONObject.parseObject(jsonStr, HashMap.class);
            logger.info("cash post respMap == > " + respMap);

            if("SUCCESS".equalsIgnoreCase((String) respMap.get("retCode"))) {
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);

                logger.info("【通道代付成功】---- " + respMap.get("retMsg"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");
            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("errCode") + "--->" + respMap.get("retMsg"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg((String) respMap.get("resultMsg"));
            }
        } catch (Exception e) {
            logger.error("【通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
            return cashRespData;
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
