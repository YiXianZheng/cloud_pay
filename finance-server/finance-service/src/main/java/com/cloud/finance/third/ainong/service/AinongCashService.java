package com.cloud.finance.third.ainong.service;

import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.ainong.enums.RespCodeEnum;
import com.cloud.finance.third.ainong.utils.MD5Util;
import com.cloud.finance.third.ainong.vo.AccountQueryData;
import com.cloud.finance.third.ainong.vo.AinongCashRespData;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
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

@Service("AinongCashService")
public class AinongCashService implements BaseCashService {
    private static Logger logger = LoggerFactory.getLogger(AinongCashService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {
        logger.info("[ainong cash params]:channelId:" + thirdChannelDto.getId() + ", rechargeNo:" + shopRecharge.getRechargeNo());
        Map<String, String> params = new HashMap<>();

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        /*** 公共参数 ***/
        //appid
        String appid = thirdChannelDto.getAppId();
        //商户账号  商户在支付平台的唯一标识
        String mch_id = thirdChannelDto.getMerchantId();
        //随机字符串
        String nonce_str = new Date().getTime() + "";
        //签名方式
        String sign_type = "MD5";
        //签名
        String sign = "";
        //请求时间戳
        String timestamp = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18);

        /*** 请求参数 ***/
        //商户订单号
        String out_trade_no = shopRecharge.getRechargeNo();
        //商品名称
        String title = shopRecharge.getRechargeNo();
        //收款类型
        String payee_type = "1";
        //收款账户类型
        String bankcard_type = "1";
        //收款方姓名
        String payee_name = shopRecharge.getBankAccount();
        //总金额
        String total_fee = SafeComputeUtils.numberFormate(SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney()));
        //银行ID
        ApiResponse response = sysBankProvider.toChannelCode(shopRecharge.getBankCode(), thirdChannelDto.getId());
        if(!(ResponseCode.Base.SUCCESS+"").equals(response.getCode())) {
            logger.error("【通道不支持的该银行的代付请求】-------系统银行编码："+ shopRecharge.getBankCode());
            cashRespData.setMsg("不支持的该银行的代付请求");

            return cashRespData;
        }
        String bank_id = response.getData().toString();
        //银行卡号
        String bankcard_account = shopRecharge.getBankNo();

        if(StringUtils.isNotBlank(appid)) {
            params.put("appid", appid);
        }
        if(StringUtils.isNotBlank(mch_id)) {
            params.put("mch_id", mch_id);
        }
        if(StringUtils.isNotBlank(nonce_str)) {
            params.put("nonce_str", nonce_str);
        }
        if(StringUtils.isNotBlank(sign_type)) {
            params.put("sign_type", sign_type);
        }
        if(StringUtils.isNotBlank(timestamp)) {
            params.put("timestamp", timestamp);
        }
        if(StringUtils.isNotBlank(out_trade_no)) {
            params.put("out_trade_no", out_trade_no);
        }
        if(StringUtils.isNotBlank(title)) {
            params.put("title", title);
        }
        if(StringUtils.isNotBlank(payee_type)) {
            params.put("payee_type", payee_type);
        }
        if(StringUtils.isNotBlank(bankcard_type)) {
            params.put("bankcard_type", bankcard_type);
        }
        if(StringUtils.isNotBlank(payee_name)) {
            params.put("payee_name", payee_name);
        }
        if(StringUtils.isNotBlank(total_fee)) {
            params.put("total_fee", total_fee);
        }
        if(StringUtils.isNotBlank(bank_id)) {
            params.put("bank_id", bank_id);
        }
        if(StringUtils.isNotBlank(bankcard_account)) {
            params.put("bankcard_account", bankcard_account);
        }

        logger.info("[before MD5 sign] -> " + ASCIISortUtil.buildSign(params, "=", "&key="+thirdChannelDto.getPayMd5Key()));
        sign = MD5Util.MD5Encode(ASCIISortUtil.buildSign(params, "=", "&key="+thirdChannelDto.getPayMd5Key()));
        params.put("sign", sign);

        logger.info("[ainong sign msg]:signMsg:"+sign);

        try {
            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
            if(StringUtils.isEmpty(jsonStr)){
                logger.error("【通道支付请求请求结果为空】");
                cashRespData.setMsg("通道支付请求请求结果为空");
                return cashRespData;
            }
            logger.info("cash post result == > "+ jsonStr);

            net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(jsonStr);
            AinongCashRespData ainongCashRespData = (AinongCashRespData) net.sf.json.JSONObject.toBean(jsonObject, AinongCashRespData.class);

            if(AinongCashRespData.TRADE_STATUS_SUCCESS == ainongCashRespData.getTrade_status()){
                logger.info("【通道代付成功】");
                cashRespData.setStatus(CashRespData.STATUS_SUCCESS);
                cashRespData.setMsg("代付成功");
            }else if(AinongCashRespData.TRADE_STATUS_DOING == ainongCashRespData.getTrade_status()){
                cashRespData.setStatus(CashRespData.STATUS_DOING);
                cashRespData.setMsg("代付处理中");
            }else{
                logger.info("【通道代付失败】----> 查询结果状态错误：" + ainongCashRespData.getTrade_status());
                cashRespData.setMsg("通道代付失败");
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
        return null;
    }

    @Override
    public CashRespData adminApplyCash(CashReqData cashReqData, ThirdChannelDto thirdChannelDto) {
        return null;
    }

}
