package com.cloud.finance.third.guanjun.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.guanjun.utils.GJSignUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("GuanjunCashService")
public class GuanjunCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(GuanjunCashService.class);

    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider provider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("[guanjun cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());
        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        Map<String, String> params = new HashMap<>();
        params.put("merId", thirdChannelDto.getMerchantId());
        params.put("bizType", "01");
        params.put("txnAmt", String.valueOf((int) ((shopRecharge.getRechargeMoney() - shopRecharge.getRechargeRateMoney()) * 100)));
        params.put("merOrderId", shopRecharge.getRechargeNo());
        params.put("idCardNo", "342423593829142847");
        params.put("bankName", shopRecharge.getBankSubbranch());
        params.put("province", shopRecharge.getProvince());
        params.put("bankCity", shopRecharge.getCity());
        params.put("currency", "CNY");
        params.put("bankNo", "231635390521");
        params.put("subject", "subject");
        params.put("sendIp", "103.230.242.216");
        params.put("body", "body");
        params.put("txnTime", DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18));
        params.put("attach", "attach");
        params.put("acctId", shopRecharge.getBankNo());
        params.put("acctName", shopRecharge.getBankAccount());
        params.put("mobile", "15160599123");
        ApiResponse apiResponse = provider.toChannelCode(shopRecharge.getBankCode(), thirdChannelDto.getId());
        if (!apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode() + "")) {
            cashRespData.setStatus("false");
            cashRespData.setMsg("第三方不支持该银行");
            return cashRespData;
        }
        logger.info("获取银行卡编码：" + apiResponse);
        params.put("bankCode", apiResponse.getData().toString());

        try {
            // 创建签名源字符串
            logger.info("guanjun cash params：" + params);
            // 签名
            String sign = GJSignUtil.signData(params, thirdChannelDto.getPayMd5Key()).toUpperCase();
            logger.info("guanjun cash sign result：" + sign);
            params.put("signature", sign);
            params.put("signMethod", "MD5");
            logger.info("发送数据：" + JSONObject.toJSONString(params));
            // 发送http请求
            String respStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if (respStr == null) {
                logger.error("代付结果为空");
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRechargeService.rechargeFail(shopRecharge);

                cashRespData.setStatus("false");
                cashRespData.setMsg("代付结果为空");
                return cashRespData;
            }
            logger.info("guanjun cash post result str: " + respStr);
            // 请求结果处理
            Map<Object, Object> respMap = MapUtils.json2Map(respStr);

            String status = respMap.get("success").toString();
            logger.info("guanjun cash post result code: " + status);
            // 判断代付是否成功
            if (status.equals("true")) {
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelRespMsg(respStr);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setCompleteTime(new Date());
                logger.info("代付申请成功");
                shopRechargeService.rechargeSuccess(shopRecharge);
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功");
            } else {
                String code = respMap.get("code").toString();
                String msg = respMap.get("message").toString();
                logger.error("失败编码：" + code + ", 失败描述：" + msg);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setRechargeStatus(4);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.error("代付申请失败");
                shopRecharge.setThirdChannelRespMsg(msg);
                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("代付申请异常");
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
