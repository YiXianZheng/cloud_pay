package com.cloud.finance.third.jinxin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.jinxin.utils.MD5Util;
import com.cloud.finance.third.jinxin.utils.PayUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
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

@Service("JinxinCashService")
public class JinxinCashService implements BaseCashService {
    private static Logger logger = LoggerFactory.getLogger(JinxinCashService.class);

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
        logger.info("...[jinxin alipay cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        //请求参数
        String mchid = thirdChannelDto.getMerchantId();//商户id
        String out_trade_no = shopRecharge.getRechargeNo();
        String money = SafeComputeUtils.numberFormate(SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney()));
        String bankname = "";
        logger.error("【代付请求】 bank code :" + shopRecharge.getBankCode());
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(shopRecharge.getBankCode());
        logger.error("【代付请求】 bank response :" + apiResponse);
        if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())){
            bankname = apiResponse.getData().toString();
        }else{
            logger.error("【代付请求失败】不支持的银行");
            cashRespData.setMsg("【代付请求失败】不支持的银行");
            return cashRespData;
        }
        String subbranch = shopRecharge.getBankSubbranch();
        String accountname = shopRecharge.getBankAccount();
        String cardnumber = shopRecharge.getBankNo();
        String province = shopRecharge.getProvince();
        String city = shopRecharge.getCity();

        Map<String, String> params = new HashMap<>();
        if(StringUtils.isNotBlank(mchid)) {
            params.put("mchid", mchid);
        }
        if(StringUtils.isNotBlank(out_trade_no)) {
            params.put("out_trade_no", out_trade_no);
        }
        if(StringUtils.isNotBlank(money)) {
            params.put("money", money);
        }
        if(StringUtils.isNotBlank(bankname)) {
            params.put("bankname", bankname);
        }
        if(StringUtils.isNotBlank(subbranch)) {
            params.put("subbranch", subbranch);
        }else{
            logger.error("【jinxin cash failed】开户银行支行不能为空");
            cashRespData.setStatus("开户银行支行不能为空");

            return cashRespData;
        }
        if(StringUtils.isNotBlank(accountname)) {
            params.put("accountname", accountname);
        }
        if(StringUtils.isNotBlank(cardnumber)) {
            params.put("cardnumber", cardnumber);
        }
        if(StringUtils.isNotBlank(province)) {
            params.put("province", province);
        }else{
            logger.error("【jinxin cash failed】开户银行所在省不能为空");
            cashRespData.setStatus("开户银行所在省不能为空");

            return cashRespData;
        }
        if(StringUtils.isNotBlank(city)) {
            params.put("city", city);
        }else{
            logger.error("【jinxin cash failed】开户银行所在城市不能为空");
            cashRespData.setStatus("开户银行所在城市不能为空");

            return cashRespData;
        }

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[jinxin before sign msg]:"+ stringSignTemp +"; key:" +thirdChannelDto.getPayMd5Key());
        String signStr = "";
        try {
            signStr = MD5Util.md5(stringSignTemp);
        }catch (Exception e){
            logger.error("【系统计算签名错误】-----------");
            cashRespData.setStatus("系统计算签名错误");

            return cashRespData;
        }
        logger.info("[jinxin sign msg]:"+signStr);

        params.put("pay_md5sign", signStr);
        logger.info("pay post params == > "+ PayUtil.getSignParam(params));

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

            if("success".equalsIgnoreCase(respMap.get("status"))){
                logger.info("【通道代付受理成功】----");

                shopRecharge.setRechargeStatus(2);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelOrderNo(respMap.get("transaction_id"));
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
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("status") + "--->" + respMap.get("msg"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("msg"));
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
        logger.info("...[jinxin alipay cash query] query cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        /*** 请求参数 ***/
        //商户账号  商户在支付平台的唯一标识
        String mchid = thirdChannelDto.getMerchantId();
        String out_trade_no = shopRecharge.getRechargeNo();

        Map<String, String> params = new HashMap<>();
        params.put("mchid", mchid);
        params.put("out_trade_no", out_trade_no);

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[jinxin before sign msg]:"+ stringSignTemp +"; key:" +thirdChannelDto.getPayMd5Key());
        String signStr = "";
        try {
            signStr = MD5Util.md5(stringSignTemp);
        }catch (Exception e){
            logger.error("【系统计算签名错误】-----------");
            cashRespData.setStatus("系统计算签名错误");

            return cashRespData;
        }
        logger.info("[jinxin sign msg]:"+signStr);

        params.put("pay_md5sign", signStr);
        logger.info("pay post params == > "+ PayUtil.getSignParam(params));

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getQueryUrl(), params);
            JSONObject jsonObject = JSON.parseObject(jsonStr);
            if(jsonObject.size()==0){
                logger.error("【通道代付请求请求结果为空】");
                cashRespData.setMsg("通道代付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash query post result == > "+ jsonObject.toJSONString());

            //获取返回报文
            Map<String, String> respMap = JSONObject.parseObject(jsonStr, HashMap.class);

            if("success".equalsIgnoreCase(respMap.get("status"))){
                logger.info("【通道代付查询成功】----");

                if("1".equals(respMap.get("refCode"))) {
                    shopRecharge.setRechargeStatus(1);
                    shopRecharge.setCompleteTime(new Date());
                    shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                    shopRecharge.setThirdChannelOrderNo(respMap.get("transaction_id"));
                    shopRecharge.setThirdChannelRespMsg(jsonStr);
                    shopRechargeService.rechargeSuccess(shopRecharge);

                    logger.info("【通道代付成功】---- " + respMap.get("respInfo"));
                    cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                    cashRespData.setMsg("代付成功[" + respMap.get("respInfo") + "]");
                }else if("3".equals(respMap.get("refCode")) || "4".equals(respMap.get("refCode"))){
                    logger.info("【通道查询成功】---- " + respMap.get("msg"));
                    cashRespData.setStatus(CashRespData.STATUS_DOING);
                    cashRespData.setMsg("代付查询成功[" + respMap.get("msg") + "]");
                }else{
                    shopRecharge.setRechargeStatus(4);
                    shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                    shopRecharge.setThirdChannelOrderNo(respMap.get("transaction_id"));
                    shopRecharge.setThirdChannelRespMsg(jsonStr);
                    shopRechargeService.rechargeSuccess(shopRecharge);

                    logger.info("【通道查询成功】---- " + respMap.get("msg"));
                    cashRespData.setStatus(CashRespData.STATUS_ERROR);
                    cashRespData.setMsg("代付查询成功[" + respMap.get("msg") + "]");
                }

            }else{
                logger.info("【通道代付查询请求失败】------ ");

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("msg"));
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
        logger.info("...[jinxin alipay admin cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        //请求参数
        String mchid = thirdChannelDto.getMerchantId();//商户id
        String out_trade_no = "S" + DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13) +"0";
        String money = SafeComputeUtils.numberFormate(cashReqData.getAmount());
        String bankname = "";
        logger.error("【代付请求】 bank code :" + cashReqData.getBankCode());
        ApiResponse apiResponse = sysBankProvider.getBankNameByCode(cashReqData.getBankCode());
        logger.error("【代付请求】 bank response :" + apiResponse);
        if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())){
            bankname = apiResponse.getData().toString();
        }else{
            logger.error("【代付请求失败】不支持的银行");
            cashRespData.setMsg("【代付请求失败】不支持的银行");
            return cashRespData;
        }
        String subbranch = cashReqData.getSubbranch();
        String accountname = cashReqData.getBankAccount();
        String cardnumber = cashReqData.getBankNo();
        String province = cashReqData.getProvince();
        String city = cashReqData.getCity();

        Map<String, String> params = new HashMap<>();
        if(StringUtils.isNotBlank(mchid)) {
            params.put("mchid", mchid);
        }
        if(StringUtils.isNotBlank(out_trade_no)) {
            params.put("out_trade_no", out_trade_no);
        }
        if(StringUtils.isNotBlank(money)) {
            params.put("money", money);
        }
        if(StringUtils.isNotBlank(bankname)) {
            params.put("bankname", bankname);
        }
        if(StringUtils.isNotBlank(subbranch)) {
            params.put("subbranch", subbranch);
        }else{
            logger.error("【jinxin cash failed】开户银行支行不能为空");
            cashRespData.setStatus("开户银行支行不能为空");

            return cashRespData;
        }
        if(StringUtils.isNotBlank(accountname)) {
            params.put("accountname", accountname);
        }
        if(StringUtils.isNotBlank(cardnumber)) {
            params.put("cardnumber", cardnumber);
        }
        if(StringUtils.isNotBlank(province)) {
            params.put("province", province);
        }else{
            logger.error("【jinxin cash failed】开户银行所在省不能为空");
            cashRespData.setStatus("开户银行所在省不能为空");

            return cashRespData;
        }
        if(StringUtils.isNotBlank(city)) {
            params.put("city", city);
        }else{
            logger.error("【jinxin cash failed】开户银行所在城市不能为空");
            cashRespData.setStatus("开户银行所在城市不能为空");

            return cashRespData;
        }

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
        logger.info("[jinxin before sign msg]:"+ stringSignTemp +"; key:" +thirdChannelDto.getPayMd5Key());
        String signStr = "";
        try {
            signStr = MD5Util.md5(stringSignTemp);
        }catch (Exception e){
            logger.error("【系统计算签名错误】-----------");
            cashRespData.setStatus("系统计算签名错误");

            return cashRespData;
        }
        logger.info("[jinxin sign msg]:"+signStr);

        params.put("pay_md5sign", signStr);
        logger.info("pay post params == > "+ PayUtil.getSignParam(params));

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

            if("success".equalsIgnoreCase(respMap.get("status"))){
                logger.info("【通道代付受理成功】----");

                logger.info("【通道代付成功】---- " + respMap.get("msg"));
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功[受理成功]");

            }else{
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respMap.get("status") + "--->" + respMap.get("msg"));

                cashRespData.setStatus(CashRespData.STATUS_ERROR);
                cashRespData.setMsg(respMap.get("msg"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
            cashRespData.setMsg("通道代付请求异常");
        }
        return cashRespData;
    }

}
