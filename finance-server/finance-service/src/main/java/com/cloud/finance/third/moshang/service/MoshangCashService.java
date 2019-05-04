package com.cloud.finance.third.moshang.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.GetUtils;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.jinxin.utils.PayUtil;
import com.cloud.finance.third.moshang.utils.MD5;
import com.cloud.finance.third.moshang.utils.MSUtils;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("MoshangCashService")
public class MoshangCashService implements BaseCashService {
    private static Logger logger = LoggerFactory.getLogger(MoshangCashService.class);

    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {
        logger.info("...[moshang cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");
        shopRecharge.setThirdChannelId(thirdChannelDto.getId());

        //请求参数
        String merchantid = thirdChannelDto.getMerchantId();//商户id
        String out_trade_no = shopRecharge.getRechargeNo();
        String bank_amount = SafeComputeUtils.numberFormate(SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney()));
        logger.info("【代付请求】 bank code :" + shopRecharge.getBankCode());
        ApiResponse apiResponse = sysBankProvider.toChannelCode(shopRecharge.getBankCode(), thirdChannelDto.getId());
        logger.info("【代付请求】 bank response :" + apiResponse);
        if(!(ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())){

            cashRespData.setMsg("【代付请求失败】不支持的银行");
            logger.error("【代付请求失败】不支持的银行");
            return cashRespData;
        }
        String bank_site_name = shopRecharge.getBankSubbranch();
        String bank_account_name = shopRecharge.getBankAccount();
        String bank_account_no = shopRecharge.getBankNo();

        Map<String, String> params = new HashMap<>();
        params.put("merchantid", merchantid);
        params.put("out_trade_no", out_trade_no);
        params.put("bank_amount", bank_amount);
        params.put("bank_name", apiResponse.getData().toString());
        params.put("bank_site_name", bank_site_name);
        params.put("bank_account_name", bank_account_name);
        params.put("bank_account_no", bank_account_no);

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", thirdChannelDto.getPayMd5Key());
        logger.info("[moshang before sign msg]:"+ stringSignTemp +"; key:" +thirdChannelDto.getPayMd5Key());
        String signStr = MD5.MD5Encode(stringSignTemp).toUpperCase();
        logger.info("[moshang sign msg]:"+signStr);

        params.put("sign", signStr);
        logger.info("pay post params == > "+ PayUtil.getSignParam(params));

        try {
            String jsonStr = GetUtils.sendGetMethodForCharset(thirdChannelDto.getPayUrl(), params,"GB2312");
            boolean isJson = MSUtils.isJson(jsonStr);
            if (!isJson) {
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
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

            if("SUCCESS".equalsIgnoreCase(respMap.get("returncode").toString())){
                logger.info("【通道代付受理成功】----");

                shopRecharge.setRechargeStatus(1);
                shopRecharge.setCompleteTime(new Date());
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelOrderNo(respMap.get("ordernum").toString());
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeSuccess(shopRecharge);

                logger.info("【通道代付成功】---- " + respMap.get("returnmsg"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");

            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(jsonStr);
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【代付失败】----> 代付结果状态错误：" + respMap.get("returncode") + "--->" + respMap.get("returnmsg"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("returnmsg").toString());
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
        logger.info("...[jinxin alipay cash query] query cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        /*** 请求参数 ***/
        String merchantid = thirdChannelDto.getMerchantId();//商户id
        String out_trade_no = shopRecharge.getRechargeNo();
        String bank_amount = SafeComputeUtils.numberFormate(SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney()));

        Map<String, String> params = new HashMap<>();
        params.put("merchantid", merchantid);
        params.put("out_trade_no", out_trade_no);
        params.put("bank_amount", bank_amount);

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[moshang before sign msg]:"+ stringSignTemp +"; key:" +thirdChannelDto.getPayMd5Key());
        String signStr = MD5.MD5Encode(stringSignTemp).toUpperCase();
        logger.info("[moshang sign msg]:"+signStr);

        params.put("sign", signStr);
        logger.info("pay post params == > "+ PayUtil.getSignParam(params));

        try {
            String jsonStr = GetUtils.sendGetMethodForCharset(thirdChannelDto.getQueryUrl(), params,"GB2312");
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【通道代付请求请求结果为空】");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash query post result == > "+ jsonObject.toJSONString());

            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if(shopRecharge.getRechargeNo().equalsIgnoreCase(respMap.get("ordernum"))){
                logger.info("【通道代付查询成功】----");

                if("1".equals(respMap.get("returncode"))) {
                    shopRecharge.setRechargeStatus(1);
                    shopRecharge.setCompleteTime(new Date());
                    shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                    shopRecharge.setThirdChannelOrderNo(respMap.get("ordernum"));
                    shopRecharge.setThirdChannelRespMsg(jsonStr);
                    shopRechargeService.rechargeSuccess(shopRecharge);

                    logger.info("【通道代付成功】---- " + respMap.get("returnmsg"));
                    cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                    cashRespData.setMsg("代付成功[" + respMap.get("returnmsg") + "]");
                }else if("0".equals(respMap.get("returncode")) || "3".equals(respMap.get("returncode"))){
                    logger.info("【通道查询成功】---- " + respMap.get("returnmsg"));
                    cashRespData.setStatus(CashRespData.STATUS_DOING);
                    cashRespData.setMsg("代付查询成功[" + respMap.get("returnmsg") + "]");
                }else{
                    shopRecharge.setRechargeStatus(4);
                    shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                    shopRecharge.setThirdChannelOrderNo(respMap.get("ordernum"));
                    shopRecharge.setThirdChannelRespMsg(jsonStr);
                    shopRechargeService.rechargeSuccess(shopRecharge);

                    logger.info("【通道查询成功】---- " + respMap.get("returnmsg"));
                    cashRespData.setStatus(CashRespData.STATUS_ERROR);
                    cashRespData.setMsg("代付查询成功[" + respMap.get("returnmsg") + "]");
                }

            }else{
                logger.info("【通道代付查询请求失败】------ ");

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg("查询订单不一致");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
        }
        return cashRespData;
    }

    @Override
    public CashRespData adminApplyCash(CashReqData cashReqData, ThirdChannelDto thirdChannelDto) {
        logger.info("...[moshang admin cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        //请求参数
        String merchantid = thirdChannelDto.getMerchantId();//商户id
        String out_trade_no = "S" + DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13) +"0";
        String bank_amount = SafeComputeUtils.numberFormate(cashReqData.getAmount());
        String bank_name = "";
        logger.info("【代付请求】 bank code :" + cashReqData.getBankCode());
        ApiResponse apiResponse = sysBankProvider.toChannelCode(cashReqData.getBankCode(), thirdChannelDto.getId());
        logger.info("【代付请求】 bank response :" + apiResponse);
        if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())){
            bank_name = apiResponse.getData().toString();
        }else{
            logger.error("【代付请求失败】不支持的银行");
            cashRespData.setMsg("【代付请求失败】不支持的银行");
            return cashRespData;
        }
        String bank_site_name = cashReqData.getSubbranch();
        String bank_account_name = cashReqData.getBankAccount();
        String bank_account_no = cashReqData.getBankNo();

        Map<String, String> params = new HashMap<>();
        if(StringUtils.isNotBlank(merchantid)) {
            params.put("merchantid", merchantid);
        }
        if(StringUtils.isNotBlank(out_trade_no)) {
            params.put("out_trade_no", out_trade_no);
        }
        if(StringUtils.isNotBlank(bank_amount)) {
            params.put("bank_amount", bank_amount);
        }
        if(StringUtils.isNotBlank(bank_name)) {
            params.put("bank_name", bank_name);
        }
        if(StringUtils.isNotBlank(bank_site_name)) {
            params.put("bank_site_name", bank_site_name);
        }else{
            logger.error("【moshang admin cash failed】开户银行支行不能为空");
            cashRespData.setStatus("开户银行支行不能为空");

            return cashRespData;
        }
        if(StringUtils.isNotBlank(bank_account_name)) {
            params.put("bank_account_name", bank_account_name);
        }
        if(StringUtils.isNotBlank(bank_account_no)) {
            params.put("bank_account_no", bank_account_no);
        }

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", thirdChannelDto.getPayMd5Key());
        logger.info("[moshang admin before sign msg]:"+ stringSignTemp +"; key:" +thirdChannelDto.getPayMd5Key());
        String signStr = MD5.MD5Encode(stringSignTemp).toUpperCase();
        logger.info("[moshang admin sign msg]:"+signStr);

        params.put("sign", signStr);
        logger.info("pay post params == > "+ PayUtil.getSignParam(params));

        try {
            String jsonStr = GetUtils.sendGetMethodForCharset(thirdChannelDto.getPayUrl(), params,"GB2312");
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【通道代付请求请求结果为空】");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash post result == > "+ jsonObject.toJSONString());

            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("SUCCESS".equalsIgnoreCase(respMap.get("returncode"))){
                logger.info("【通道代付受理成功】----");

                logger.info("【通道代付成功】---- " + respMap.get("returnmsg"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");

            }else{
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("returncode") + "--->" + respMap.get("returnmsg"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("returnmsg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
        }
        return cashRespData;
    }


}
