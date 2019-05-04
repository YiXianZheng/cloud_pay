package com.cloud.finance.third.miaofu.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.*;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.jinxin.utils.PayUtil;
import com.cloud.finance.third.moshang.utils.MD5;
import com.cloud.finance.third.moshang.utils.MSUtils;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 秒付微信扫码代付
 * @author: zyx
 * @create: 2019-04-20 16:45
 **/
@Service("MiaofuCashService")
public class MiaofuCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(MiaofuCashService.class);

    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {
        logger.info("...[miaofu cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        //请求参数
        String bank_amount = SafeComputeUtils.numberFormate(SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney()));
        logger.info("【代付请求】 bank code :" + shopRecharge.getBankCode());
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        logger.info("【代付请求】 bank response :" + apiResponse);
        if(!(ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())){

            cashRespData.setMsg("【代付请求失败】不支持的银行");
            logger.error("【代付请求失败】不支持的银行");
            return cashRespData;
        }

        Map<String, String> params = new HashMap<>();
        params.put("mchid", thirdChannelDto.getMerchantId());
        params.put("out_trade_no", shopRecharge.getRechargeNo());
        params.put("money", bank_amount);
        params.put("bankname", apiResponse.getData().toString());
        params.put("subbranch", shopRecharge.getBankSubbranch());
        params.put("accountname", shopRecharge.getBankAccount());
        params.put("cardnumber", shopRecharge.getBankNo());
        params.put("province", shopRecharge.getProvince());
        params.put("city", shopRecharge.getCity());

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[miaofu before sign msg]:"+ stringSignTemp +"; key:" +thirdChannelDto.getPayMd5Key());
        String signStr = MD5.MD5Encode(stringSignTemp).toUpperCase();
        logger.info("[miaofu sign msg]:"+signStr);

        params.put("pay_md5sign", signStr);

        String json = JSONObject.toJSONString(params);
        logger.info("cash json data == > " + json);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            boolean isJson = MSUtils.isJson(jsonStr);
            if (!isJson) {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.error("【代付失败】----> 代付失败原因：" + jsonStr);

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(jsonStr);
                return cashRespData;
            }
            logger.info("代付请求结果：" + jsonStr);
            if(StringUtil.isEmpty(jsonStr)){
                logger.error("【代付请求请求结果为空】");
                cashRespData.setMsg("代付请求请求结果为空");
                return cashRespData;
            }

            //获取返回报文
            Map<Object, Object> respMap = MapUtils.json2Map(jsonStr);

            if("success".equalsIgnoreCase(respMap.get("status").toString())){
                logger.info("【通道代付受理成功】----");

                shopRecharge.setRechargeStatus(1);
                shopRecharge.setCompleteTime(new Date());
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelOrderNo(respMap.get("transaction_id").toString());
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeSuccess(shopRecharge);

                logger.info("【通道代付成功】---- " + respMap.get("msg"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");

            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【代付失败】----> 代付结果状态错误：" + respMap.get("status") + "--->" + respMap.get("msg"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("msg").toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【代付请求异常】-------");
            cashRespData.setMsg("代付请求异常");
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
