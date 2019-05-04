package com.cloud.finance.third.zhaocai.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.HttpClientUtil;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
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
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 招财代付
 * @author: zyx
 * @create: 2019-04-03 11:54
 **/
@Service("ZhaocaiCashService")
public class ZhaocaiCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(ZhaocaiCashService.class);

    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;
    @Autowired
    private MerchantUserProvider provider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("[zhaocai cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Map<String, String> params = new HashMap<>();
        params.put("appid", thirdChannelDto.getMerchantId());
        params.put("ac", "apply");
        params.put("version", "1.0.7");
        params.put("realname", shopRecharge.getBankAccount());
        params.put("time", DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18));
        params.put("amount", SafeComputeUtils.numberFormate2(shopRecharge.getRechargeMoney() - shopRecharge.getRechargeRateMoney()));
        params.put("bankcard", shopRecharge.getBankNo());
        params.put("type", "2");
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
        String cnaps = map.get("bankBin").toString();
        if (StringUtil.isEmpty(cnaps)) {
            cashRespData.setMsg("银行联行号不能为空");
            return cashRespData;
        }
        logger.info("联行号：" + cnaps);
        params.put("cnaps", cnaps);   // 联行号
        String bank;
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        if (apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode() + "")) {
            bank = apiResponse.getData().toString();
        } else {
            cashRespData.setMsg("银行不支持");
            return cashRespData;
        }
        params.put("bankname", bank);
        ApiResponse response = sysBankProvider.toChannelCode(shopRecharge.getBankCode(), thirdChannelDto.getId());
        String bankcode;
        if (response.getCode().equals(ResponseCode.Base.SUCCESS.getCode() + "")) {
            bankcode = response.getData().toString();
        } else {
            cashRespData.setMsg("第三方不支持该银行");
            return cashRespData;
        }
        params.put("bankcode", bankcode);   // 银行编码

        try {
            // 创建签名源字符串
            logger.info("zhaocai cash params：" + params);
            String signStr = ASCIISortUtil.buildSign(params, "=", thirdChannelDto.getPayMd5Key());
            logger.info("zhaocai cash sign before：" + signStr);
            // 签名
            String sign = MD5Util.md5(signStr);
            logger.info("zhaocai cash sign result：" + sign);
            params.put("sg", sign);

            // 发送http请求
            String respStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
            if (respStr == null) {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRechargeService.rechargeFail(shopRecharge);

                cashRespData.setMsg("代付结果为空");
                cashRespData.setStatus("false");
                return cashRespData;
            }
            logger.info("zhaocai cash post result str: " + respStr);
            // 请求结果处理
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            String status = respMap.get("code").toString();
            String msg = respMap.get("msg").toString();
            logger.info("zhaocai cash post result code: " + status + "  result msg: " + msg);
            // 判断代付是否成功
            if (status.equals("1")) {
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelRespMsg(msg);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);

                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg(msg);
            } else {
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setRechargeStatus(4);
                shopRechargeService.rechargeFail(shopRecharge);
                shopRecharge.setThirdChannelRespMsg(msg);

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(msg);
            }
        } catch (IOException e) {
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
