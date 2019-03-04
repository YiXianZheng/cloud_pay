package com.cloud.finance.third.hankou.service;

import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.HttpClientUtil;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.ainong.service.AinongCashService;
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

@Service("HankouCashService")
public class HankouCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(AinongCashService.class);

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

        // 参数拼接
        Map<String, String> params = new HashMap<>();
        params.put("mchid", thirdChannelDto.getMerchantId());
        params.put("out_trade_no", shopRecharge.getRechargeNo());
        String money = SafeComputeUtils.numberFormate2(shopRecharge.getRechargeMoney());
        logger.info("代付金额：" + money);
        params.put("money", money);
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        String bankName;
        if (apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode() + "")) {
            bankName = apiResponse.getData().toString();
        } else {
            cashRespData.setMsg("银行不支持");
            return cashRespData;
        }
        params.put("bankname", bankName);
        params.put("subbranch", shopRecharge.getBankSubbranch());
        params.put("accountname", shopRecharge.getBankAccount());
        params.put("cardnumber", shopRecharge.getBankNo());
        params.put("province", shopRecharge.getProvince());
        params.put("city", shopRecharge.getCity());

        try {
            // 创建签名源字符串
            logger.info("hk cash params：" + params);
            String signStr = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
            logger.info("hk cash sign before：" + signStr);
            // 签名
            String sign = MD5Util.md5(signStr).toUpperCase();
            logger.info("hk cash sign result：" + sign);
            params.put("pay_md5sign", sign);

            // 发送http请求
            String respStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
            if (respStr == null) {
                cashRespData.setMsg("代付结果为空");
                cashRespData.setStatus("false");
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRechargeService.rechargeFail(shopRecharge);
                return cashRespData;
            }
            logger.info("hk cash post result str: " + respStr);
            // 请求结果处理
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            String status = respMap.get("status").toString();
            String msg = respMap.get("msg").toString();
            logger.info("hk cash post result code: " + status + "  result msg: " + msg);
            // 判断代付是否成功     成功：success  失败：error
            if (status.equals("success")) {
                shopRecharge.setRechargeStatus(1);

                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(respStr);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);

                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg(msg);
            } else {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(respStr);
                shopRechargeService.rechargeFail(shopRecharge);

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
