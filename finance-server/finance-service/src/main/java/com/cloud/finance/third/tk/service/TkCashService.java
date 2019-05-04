package com.cloud.finance.third.tk.service;

import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.HttpClientUtil;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.utils.DateUtil;
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

/**
 * @description: tk商务平台代付
 * @author: zyx
 * @create: 2019-04-04 23:04
 **/
@Service("TkCashService")
public class TkCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(TkCashService.class);

    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {
        logger.info("[tk cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Map<String, String> params = new HashMap<>();
        params.put("inputCharset", "UTF-8");
        params.put("merchantId", thirdChannelDto.getMerchantId());
        params.put("payAmount", SafeComputeUtils.numberFormate(shopRecharge.getRechargeMoney() - shopRecharge.getRechargeRateMoney()));
        params.put("transId", shopRecharge.getRechargeNo());
        params.put("cardName", shopRecharge.getBankAccount());
        params.put("cardNumber", shopRecharge.getBankNo());
        params.put("payTime", DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_01));
        String bank;
        ApiResponse apiResponse = sysBankProvider.toChannelCode(shopRecharge.getBankCode(), thirdChannelDto.getId());
        if (apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode() + "")) {
            bank = apiResponse.getData().toString();
        } else {
            cashRespData.setMsg("第三方不支持该银行");
            return cashRespData;
        }
        params.put("bankCode", bank);

        try {
            // 创建签名源字符串
            logger.info("tk cash params：" + params);
            String signStr = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
            logger.info("tk cash sign before：" + signStr);
            // 签名
            String sign = MD5Util.md5(signStr);
            logger.info("tk cash sign result：" + sign);
            params.put("sign", sign);

            // 发送http请求
            String respStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
            if (respStr == null) {
                cashRespData.setStatus("false");
                shopRecharge.setRechargeStatus(4);
                cashRespData.setMsg("代付结果为空");
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRechargeService.rechargeFail(shopRecharge);
                return cashRespData;
            }
            logger.info("tk cash post result str: " + respStr);
            // 请求结果处理
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            String status = respMap.get("retCode").toString();
            String msg = respMap.get("retMsg").toString();
            logger.info("tk cash post result code: " + status + "  result msg: " + msg);
            // 判断代付是否成功
            if (status.equals("0000")) {
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelRespMsg(msg);
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);

                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg(msg);
            } else {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(msg);
                shopRechargeService.rechargeFail(shopRecharge);
                cashRespData.setMsg(msg);
                cashRespData.setStatus(CashRespData.STATUS_ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            shopRecharge.setRechargeStatus(4);
            shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
            shopRechargeService.rechargeFail(shopRecharge);
            cashRespData.setMsg("代付请求异常");
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
