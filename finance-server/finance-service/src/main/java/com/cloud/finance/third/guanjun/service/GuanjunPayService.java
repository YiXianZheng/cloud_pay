package com.cloud.finance.third.guanjun.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.guanjun.utils.GJSignUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
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
        payCreateResult.setStatus("false");
        // 支付方式，默认为微信唤醒
        String payType = shopPayDto.getChannelTypeCode();
        logger.info("【pay type:】 " + payType);

        Map<String, String> params = new HashMap<>();

        // 商户号
        String merId = thirdChannelDto.getMerchantId();
        // 商户订单号
        String merOrderId = shopPayDto.getSysPayOrderNo();
        // 交易金额 单位为分
        String txnAmt = String.valueOf((int) (shopPayDto.getMerchantPayMoney() * 100));
        // 后台通知地址
        String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
        // 订单发送时间  yyyyMMddHHmmss
        String txnTime = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18);

        params.put("merId", merId);
        params.put("merOrderId", merOrderId);
        params.put("subject", "H5支付测试");
        params.put("body", "H5支付测试描述");
        params.put("bizType", "3");  // 1 微信H5交易 2 QQ H5交易 3 支付宝H5交易 4 京东H5交易
        params.put("txnType", "00");
        params.put("txnSubType", "01");
        params.put("txnAmt", txnAmt);
        params.put("currency", "CNY");
        params.put("notifyUrl", notifyUrl);
        params.put("returnUrl", "http://www.baidu.com"); // 此字段无功能，为必填系统扩展字段
        params.put("sendIp", "103.230.242.216");
        params.put("txnTime", txnTime);
        params.put("province", "福建");
        params.put("city", "厦门");
        params.put("areaId", "厦门");
        params.put("attach", "attach");

        // 签名方法
        String signMethod = "MD5";
        // 签名信息
        String signature = GJSignUtil.signData(params, thirdChannelDto.getPayMd5Key()).toUpperCase();

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
            payCreateResult.setResultCode(respMap.get("code").toString());
            payCreateResult.setResultMessage("生成跳转地址失败");
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            logger.error("【通道支付请求失败】------- " + respMap.get("code"));
        }
        return payCreateResult;
    }
}
