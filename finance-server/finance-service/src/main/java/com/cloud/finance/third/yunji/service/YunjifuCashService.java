package com.cloud.finance.third.yunji.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.ainong.utils.Base64Util;
import com.cloud.finance.third.yunji.utils.YJSignUtil;
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

@Service("YunjifuCashService")
public class YunjifuCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(YunjifuCashService.class);

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

        logger.info("[yunji cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Map<String, String> params = new HashMap<>();
        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        String order_id = shopRecharge.getRechargeNo();
        String machno = thirdChannelDto.getMerchantId();
        String money = String.valueOf(shopRecharge.getRechargeMoney());
        String province = shopRecharge.getProvince();
        String bank_city = shopRecharge.getCity();
        ApiResponse response = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        String bank_name = response.getData().toString();
        String account_name = shopRecharge.getBankAccount();
        String id_card_no = "121212121212121212";
        String account_number = shopRecharge.getBankNo();
        String paypassword = thirdChannelDto.getAdminCashPassword();
        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();

        params.put("order_id", order_id);
        params.put("machno", machno);
        params.put("money", money);
        params.put("province", province);
        params.put("bank_city", bank_city);
        params.put("bank_name", bank_name);
        params.put("account_name", account_name);
        params.put("id_card_no", id_card_no);
        params.put("account_number", account_number);
        params.put("paypassword", paypassword);
        params.put("notifyUrl", Base64Util.encode(notifyUrl));

        String signBefore = ASCIISortUtil.buildSign(params, "=", "");
        logger.info("[yunji cash sign before msg]: " + signBefore);

        String signStr = null;
        try {
            signStr = YJSignUtil.doSign(signBefore, thirdChannelDto.getMerchantId());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[yunji cash sign exception]");
        }
        logger.info("[yunji cash sign result]: " + signStr);
        params.put("sign", signStr);

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

            if("0000".equalsIgnoreCase(respMap.get("resultCode"))) {
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);

                logger.info("【通道代付成功】---- " + respMap.get("resultMsg"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");

            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("resultCode") + "--->" + respMap.get("resultMsg"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("resultMsg"));
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
        return null;
    }
}
