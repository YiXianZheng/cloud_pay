package com.cloud.finance.third.guanjun.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.enums.SysPaymentTypeEnum;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.guanjun.service.utils.GJSignUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("GuanjunPayService")
public class GuanjunPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(GuanjunPayService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;
    @Autowired
    private SysBankProvider sysBankProvider;

    private String getBasePayUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_BASE_URL");
    }

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }

    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        return null;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        logger.info("[guanjun create quick jump params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        return createPayUrl(thirdChannelDto, shopPayDto);
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createQuickJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createSytAllIn(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCheckResult checkOrderResult(ThirdChannelDto thirdChannelDto, ShopPay shopPay) {
        return null;
    }

    @Override
    public String createSysPayOrderId(String channelId, String assId, String assPayOrderNo) {
        return null;
    }

    @Override
    public ChannelAccountData queryAccount(ThirdChannelDto thirdChannelDto) {
        return null;
    }

    private MidPayCreateResult createPayUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("error");
        // 支付方式，默认为微信唤醒
        String payType = shopPayDto.getChannelTypeCode();
        logger.info("【pay type:】 " + payType);

        Map<String, String> params = new HashMap<>();

        // 商户号
        String merId = thirdChannelDto.getMerchantId();
        // 商户订单号
        String merOrderId = shopPayDto.getSysPayOrderNo();
        // 商品标题
        String subject = "电子产品";
        // 业务类型
        String bizType = "1";
        // 交易类型
        String txnType = "00";
        // 交易子类型
        String txnSubType = "01";
        // 交易金额
        String txnAmt = String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100));
        // 交易币种
        String currency = "CNY";

        // 后台通知地址
        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
        // 成功跳转地址
        String returnUrl = "http://www.baidu.com";
        // 商户终端IP
        String sendIp = "127.0.0.1";
        // 订单发送时间  yyyyMMddHHmmss
        String txnTime = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18);

        params.put("merId", merId);
        params.put("merOrderId", merOrderId);
        params.put("subject", subject);

        params.put("txnType", txnType);
        params.put("txnSubType", txnSubType);
        params.put("txnAmt", txnAmt);
        params.put("currency", currency);
        params.put("notifyUrl", notifyUrl);
        params.put("returnUrl", returnUrl);
        params.put("sendIp", sendIp);
        params.put("txnTime", txnTime);

        if (payType.equals(SysPaymentTypeEnum.ALI_H5_JUMP.getValue())) {
            bizType = "3";
            params.put("province", "110000");
            params.put("city", "110100");
            params.put("areaId", "110101");
        } else if (payType.equals(SysPaymentTypeEnum.GATE_H5.getValue())) {
            // 银行ID
            ApiResponse response = sysBankProvider.toChannelCode(shopPayDto.getBankCode(), thirdChannelDto.getId());
            if(!(ResponseCode.Base.SUCCESS+"").equals(response.getCode())) {
                logger.error("【通道不支持的该银行的支付请求】-------系统银行编码："+ shopPayDto.getBankCode());
                payCreateResult.setResultCode(SysPayResultConstants.ERROR_THIRD_BANK+"");

                return payCreateResult;
            }
            if (shopPayDto.getSource() == 1) {
                // 移动端
                bizType = "62";
            } else {
                // pc端
                bizType = "61";
            }
            String bankCode = response.getData().toString();
            // 银行卡类型    1：借记卡  2：贷记卡
            String cardType = "1";
            // 用户类型     1：个人  2：企业
            String userType = "1";
            // 银行账户
            String acctId = "";

            params.put("bankCode", bankCode);
            params.put("cardType", cardType);
            params.put("userType", userType);
            params.put("acctId", acctId);
        }

        params.put("bizType", bizType);
        // 签名方法
        String signMethod = "MD5";
        // 签名信息
        String signature = GJSignUtil.signData(params, thirdChannelDto.getPayMd5Key());

        logger.info("【guanjun channel sign msg:】 " + signature);

        params.put("signMethod", signMethod);
        params.put("signature", signature);
        String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);

        logger.info("请求结果：" + jsonStr);
        if(StringUtils.isEmpty(jsonStr)){
            logger.error("【通道支付请求请求结果为空】");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS + "");

            return payCreateResult;
        }

        Map<String, Object> respMap = JSONObject.parseObject(jsonStr, HashedMap.class);

        logger.info("[guanjun channel pay result map]: " + respMap);
        if("true".equals(respMap.get("success") + "")){
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setStatus("true");
            payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setPayUrl((String) respMap.get("imgUrl"));
            logger.info("【通道支付请求成功】-------成功生成支付链接");
        }else{
            payCreateResult.setStatus("false");
            payCreateResult.setResultMessage("生成跳转地址失败");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            logger.error("【通道支付请求失败】------- " + respMap.get("code"));
        }
        return payCreateResult;
    }
}
