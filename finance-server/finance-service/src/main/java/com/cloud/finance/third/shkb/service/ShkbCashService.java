package com.cloud.finance.third.shkb.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.hankou.utils.HKUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("ShkbCashService")
public class ShkbCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(ShkbCashService.class);

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

        logger.info("[shkb cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Map<String, String> params = new HashMap<>();
        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        String order_id = shopRecharge.getRechargeNo();
        String machno = thirdChannelDto.getMerchantId();
        String money = String.valueOf(shopRecharge.getRechargeMoney() - shopRecharge.getRechargeRateMoney());
        String province = shopRecharge.getProvince();
        String bank_city = shopRecharge.getCity();
        ApiResponse response = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        String bank_name = response.getData().toString();
        String account_name = shopRecharge.getBankAccount();
        String account_number = shopRecharge.getBankNo();

        params.put("out_trade_no", order_id);
        params.put("mchid", machno);
        params.put("money", money);
        params.put("province", province);
        params.put("city", bank_city);
        params.put("bankname", bank_name);
        params.put("accountname", account_name);
        params.put("cardnumber", account_number.trim());
        params.put("subbranch", shopRecharge.getBankSubbranch());

        String signBefore = ASCIISortUtil.buildSign(params, "=", "");
        logger.info("[shkb cash sign before msg]: " + signBefore);

        String signStr = null;
        try {
            signStr = HKUtil.generateMd5Sign(params, thirdChannelDto.getPayMd5Key());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[shkb cash sign exception]");
        }
        logger.info("[shkb cash sign result]: " + signStr);
        params.put("pay_md5sign", signStr);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            logger.info("[shkb cash resp str]" + jsonStr);
            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);
            logger.info("[shkb cash resp str]" + respMap);
            if (respMap == null) {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                cashRespData.setStatus("代付失败【请求结果为空】");
                return cashRespData;
            }

            if("success".equals(respMap.get("status"))) {
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);
                logger.info("【通道代付成功】---- " + respMap.get("msg"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");
            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【通道代付失败】：" + respMap.get("msg"));

                cashRespData.setMsg("代付失败");
            }
        } catch (Exception e) {
            logger.error("【通道代付请求异常】-------");
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
