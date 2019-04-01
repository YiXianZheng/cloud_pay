package com.cloud.finance.third.ys.service;

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

/**
 * @description: Ys代付
 * @author: zyx
 * @create: 2019-03-16 23:37
 **/
@Service("YsCashService")
public class YsCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(YsCashService.class);

    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("[ys cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());

        String sysBankCode = shopRecharge.getBankCode();
        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Map<String, String> params = new HashMap<>();
        params.put("userid", thirdChannelDto.getMerchantId());
        params.put("amount", String.valueOf(shopRecharge.getRechargeMoney() - shopRecharge.getRechargeRateMoney()));
        params.put("notifyUrl", "http://www.baidu.com");

        String bank;
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(sysBankCode);
        if (apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode() + "")) {
            bank = apiResponse.getData().toString();
        } else {
            cashRespData.setMsg("银行不支持");
            return cashRespData;
        }

        String orderCode = null;
        if ("wx_ali_cash".equals(thirdChannelDto.getAppId())) {
            // 微信、支付宝代付
            params.put("customerType", "01");
            params.put("accountTypeCode", "01");
            params.put("cardNo", shopRecharge.getBankNo());
            // 银行卡账户名（持卡人）
            params.put("cardName", shopRecharge.getBankAccount());
            // 获取第三方银行编码
            ApiResponse bankCodeResp = sysBankProvider.toChannelCode(sysBankCode, thirdChannelDto.getId());
            params.put("bankCode", bankCodeResp.getData().toString());

            orderCode = "xf_withdraw";
        } else if ("union_cash".equals(thirdChannelDto.getAppId())) {
            // 银联扫码代付
            params.put("payeeBankAccount", shopRecharge.getBankNo());
            params.put("payeeName", shopRecharge.getBankAccount());
            params.put("payeeBankName", bank);

            orderCode = "ys_withdraw";
        }

        try {
            // 创建签名源字符串
            logger.info("ys cash params：" + params);
            String signStr = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
            logger.info("ys cash sign before：" + signStr);
            // 签名
            String sign = MD5Util.md5(signStr);
            logger.info("ys cash sign result：" + sign);
            params.put("sign", sign);
            params.put("orderCode", orderCode);
            params.put("channel_code", "scanPay");
            params.put("pay_number", shopRecharge.getRechargeNo());

            String baowen = ASCIISortUtil.buildSign(params, "=", null);
            logger.info("上送报文: " + baowen);

            // 发送http请求
            String respStr = HttpClientUtil.post(thirdChannelDto.getPayUrl(), params);
            if (respStr == null) {
                cashRespData.setStatus("false");
                cashRespData.setMsg("代付结果为空");
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRechargeService.rechargeFail(shopRecharge);
                return cashRespData;
            }
            logger.info("ys cash post result str: " + respStr);
            // 请求结果处理
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            String status = respMap.get("respCode").toString();
            String msg = respMap.get("respInfo").toString();
            logger.info("ys cash post result code: " + status + "  result msg: " + msg);
            // 判断代付是否成功
            if (status.equals("0000")) {
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setCompleteTime(new Date());
                shopRecharge.setThirdChannelRespMsg(respStr);
                shopRechargeService.rechargeSuccess(shopRecharge);

                cashRespData.setMsg(msg);
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
            } else {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRechargeService.rechargeFail(shopRecharge);
                shopRecharge.setThirdChannelRespMsg(respStr);

                cashRespData.setMsg(msg);
                cashRespData.setStatus(CashRespData.STATUS_ERROR);
            }
        } catch (IOException e) {
            shopRecharge.setRechargeStatus(4);
            e.printStackTrace();
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
