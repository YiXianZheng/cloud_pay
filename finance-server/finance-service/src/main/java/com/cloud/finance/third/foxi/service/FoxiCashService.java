package com.cloud.finance.third.foxi.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.*;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.MD5Util;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: ${description}
 * @author: zyx
 * @create: 2019-04-12 17:45
 **/
@Service("FoxiCashService")
public class FoxiCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(FoxiCashService.class);

    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private MerchantUserProvider provider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("[fx cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Map<String, String> params = new HashMap<>();
        params.put("merCode", thirdChannelDto.getMerchantId());
        params.put("branchId", thirdChannelDto.getAppId());
        params.put("cashAmt", String.valueOf((int) ((shopRecharge.getRechargeMoney() - shopRecharge.getRechargeRateMoney()) * 100)));
        params.put("orderId", shopRecharge.getRechargeNo() + "0");
        params.put("phoneNo", "12345634212");
        params.put("settType", "T0");
        params.put("cashType", thirdChannelDto.getAppKey());
        params.put("cardId", "4521451451212121");
        params.put("time", DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18));
        params.put("openBankName", shopRecharge.getBankSubbranch());
        // 获取联行号
        ApiResponse bankBin = provider.getBankBin(shopRecharge.getBankNo());
        if (!bankBin.getCode().equals(ResponseCode.Base.SUCCESS.getCode() + "")) {
            cashRespData.setMsg("获取银行联行号异常");
            return cashRespData;
        }
        Object object = bankBin.getData();
        logger.info("银行卡信息：" + object);
        String json = JSONObject.toJSONString(object);
        Map<Object, Object> map = MapUtils.json2Map(json);
        logger.info("银行卡信息map：" + map);
        String openBankNo = map.get("bankBin").toString();
        if (StringUtil.isEmpty(openBankNo)) {
            cashRespData.setMsg("银行联行号不能为空");
            return cashRespData;
        }
        logger.info("联行号：" + openBankNo);
        params.put("openBankNo", openBankNo);   // 联行号
        params.put("accountNo", shopRecharge.getBankNo());
        params.put("accountName", shopRecharge.getBankAccount());
        params.put("returnUrl", "http://www.baidu.com");

        try {
            // 创建签名源字符串
            logger.info("fx cash params：" + params);
            String signStr = ASCIISortUtil.buildSign(params, "=", "&" + thirdChannelDto.getPayMd5Key());
            logger.info("fx cash sign before：" + signStr);
            // 签名
            String sign = MD5Util.md5(signStr);
            logger.info("fx cash sign result：" + sign);
            params.put("signature", sign);
            String jsonStr = JSONObject.toJSONString(params);
            logger.info("json数据：" + jsonStr);
            // 发送http请求
            String respStr = PostUtils.postWithJson(thirdChannelDto.getPayUrl(), jsonStr);
            if (respStr == null) {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRechargeService.rechargeFail(shopRecharge);

                cashRespData.setStatus("false");
                cashRespData.setMsg("代付结果为空");
                return cashRespData;
            }
            logger.info("fx cash post result str: " + respStr);
            // 请求结果处理
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            String status = respMap.get("respCode").toString();
            String msg = respMap.get("respMsg").toString();
            logger.info("fx cash post result code: " + status + "  result msg: " + msg);
            // 判断代付是否成功
            if (status.equals("F5")) {
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelRespMsg(msg);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);
                logger.info("代付申请成功");
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg(msg);
            } else {
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setRechargeStatus(4);
                shopRechargeService.rechargeFail(shopRecharge);
                shopRecharge.setThirdChannelRespMsg(msg);
                logger.error("代付申请失败");
                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(msg);
            }
        } catch (Exception e) {
            logger.error("代付申请异常");
            e.printStackTrace();
            shopRecharge.setRechargeStatus(4);
            shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
            shopRechargeService.rechargeFail(shopRecharge);
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
