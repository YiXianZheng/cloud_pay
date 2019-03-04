package com.cloud.finance.third.cbd.service;

import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.HttpClientUtil;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.utils.MD5Util;
import com.cloud.sysconf.common.utils.ResponseCode;
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

@Service("CbdCashService")
public class CbdCashService implements BaseCashService {
    private static Logger logger = LoggerFactory.getLogger(CbdCashService.class);

    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("[hk cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Map<String, String> params = new HashMap<>();
        params.put("merid", thirdChannelDto.getMerchantId());
        params.put("payid", thirdChannelDto.getAppId());
        params.put("payje", String.valueOf((int) (shopRecharge.getRechargeMoney() * 100)));
        params.put("orderid", shopRecharge.getRechargeNo());
        params.put("cname", shopRecharge.getBankAccount());
        params.put("cardno", shopRecharge.getBankNo());
        String bank;
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        if (apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode() + "")) {
            bank = apiResponse.getData().toString();
        } else {
            cashRespData.setMsg("银行不支持");
            return cashRespData;
        }
        params.put("bank", bank);
        params.put("cerdid", "123123131232132132");
        params.put("phoneno", "13800000000");

        try {
            // 创建签名源字符串
            logger.info("cbd cash params：" + params);
            String signStr = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
            logger.info("cbd cash sign before：" + signStr);
            // 签名
            String sign = MD5Util.md5(signStr).toUpperCase();
            logger.info("cbd cash sign result：" + sign);
            params.put("signdata", sign);

            // 发送http请求
            String respStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
            if (respStr == null) {
                cashRespData.setMsg("代付结果为空");
                cashRespData.setStatus("false");
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setRechargeStatus(4);
                shopRechargeService.rechargeFail(shopRecharge);
                return cashRespData;
            }
            logger.info("cbd cash post result str: " + respStr);
            // 请求结果处理
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            String status = respMap.get("retCode").toString();
            String msg = respMap.get("retMsg").toString();
            logger.info("cbd cash post result code: " + status + "  result msg: " + msg);
            // 判断代付是否成功
            if (status.equals("1")) {
                shopRecharge.setRechargeStatus(1);

                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setCompleteTime(new Date());
                shopRecharge.setThirdChannelRespMsg(respStr);
                shopRechargeService.rechargeSuccess(shopRecharge);

                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg(msg);
            } else {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRechargeService.rechargeFail(shopRecharge);
                shopRecharge.setThirdChannelRespMsg(respStr);

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
            shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
            shopRecharge.setRechargeStatus(4);
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
