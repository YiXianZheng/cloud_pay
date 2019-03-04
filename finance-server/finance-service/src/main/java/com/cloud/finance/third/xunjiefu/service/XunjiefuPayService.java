package com.cloud.finance.third.xunjiefu.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.enums.SysPaymentTypeEnum;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.*;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.xunjiefu.utils.XJFSignUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
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
import java.util.Map;
import java.util.TreeMap;

/**
 * @Auther Toney
 * @Date 2018/11/01 14:12
 * @Description:
 */
@Service("XunjiefuPayService")
public class XunjiefuPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(XunjiefuPayService.class);

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
        // TODO Auto-generated method stub

        return createPayUrl(thirdChannelDto, shopPayDto);
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return createPayUrl(thirdChannelDto, shopPayDto);
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        logger.info("[xunjiefu gate direct params]:channelId:"+thirdChannelDto.getId()+ ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        String actionRespCode = SysPayResultConstants.SUCCESS_MAKE_ORDER+"";
        String actionRespMessage = "生成跳转地址成功";
        if(shopPayDto.getBankCode() != null && !shopPayDto.getBankCode().equals(shopPayDto.getChannelTypeCode())){
            logger.info("【支付请求】 bank code :" + shopPayDto.getBankCode());
            ApiResponse apiResponse = sysBankProvider.toChannelCode(shopPayDto.getBankCode(), thirdChannelDto.getId());
            logger.info("【支付请求】 bank response :" + apiResponse);
            if(!(ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())){
                logger.error("【支付请求失败】不支持的银行");
                actionRespCode = SysPayResultConstants.ERROR_THIRD_BANK +"";
                actionRespMessage = "生成跳转地址失败[不支持的银行]";
            }
        }

        String actionRespUrl = getBasePayUrl() + "/d8/xjf_" + shopPayDto.getSysPayOrderNo() + ".html";
        String channelPayOrderNo = shopPayDto.getSysPayOrderNo();
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        if (actionRespCode.equals(SysPayResultConstants.SUCCESS_MAKE_ORDER+"")) {
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setResultCode(actionRespCode);
            payCreateResult.setResultMessage(actionRespMessage);
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setChannelOrderNo(channelPayOrderNo);
            payCreateResult.setPayUrl(actionRespUrl);

        } else {
            payCreateResult.setResultCode(actionRespCode);
            payCreateResult.setResultMessage(actionRespMessage);
            payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
            payCreateResult.setChannelOrderNo(channelPayOrderNo);
            payCreateResult.setPayUrl(actionRespUrl);
        }
        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createQuickJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;

    }

    @Override
    public MidPayCreateResult createSytAllIn(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCheckResult checkOrderResult(ThirdChannelDto thirdChannelDto, ShopPay shopPay) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createSysPayOrderId(String channelId, String assId, String assPayOrderNo) {
        String tm = System.currentTimeMillis() + "";// 获取系统毫秒时间戳
        // 4位+13位+3位随机数
        String sysPayOrderNo = channelId + tm + StringUtil.getRandom(3);
        return sysPayOrderNo;
    }

    @Override
    public ChannelAccountData queryAccount(ThirdChannelDto thirdChannelDto) {
        // TODO Auto-generated method stub
        return null;
    }

    private MidPayCreateResult createPayUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        logger.info("xjf create qrcode pay params ===> orderId: " + shopPayDto.getSysPayOrderNo() + " channelId: " + thirdChannelDto.getId());
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setStatus("false");
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
        String payType = shopPayDto.getChannelTypeCode();
        logger.info("[pay channel]: " + payType);
        Map<String, String> params = new TreeMap<>();
        String productId = "";
        switch (payType) {
            case "wx_qrcode":
                productId = "0101";
                break;
            case "qq_qrcode":
                productId = "0102";
                break;
            case "gate_qrcode":
                productId = "0104";
                break;
            case "jd_qrcode":
                productId = "0105";
                break;
            default:
                productId = "0103";
                break;
        }
        params.put("version", 	"1.0.0");
        params.put("transType", "SALES");
        params.put("productId",	productId);
        params.put("merNo", 	thirdChannelDto.getMerchantId());
        params.put("orderDate", DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_11));
        params.put("orderNo", 	shopPayDto.getSysPayOrderNo());
        params.put("returnUrl", "http://youaddress.com");
        params.put("notifyUrl", getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl());
        params.put("transAmt", 	SafeComputeUtils.numberFormate2(SafeComputeUtils.multiply(shopPayDto.getMerchantPayMoney(), 100D)));
        params.put("commodityName", "test");
        params.put("commodityDetail", "test");
        params.put("salesType", "0");

        //签名
        String stringSignTemp = ASCIISortUtil.buildSign(params, "=", "");
        logger.info("[xunjiefu before sign msg]:" + stringSignTemp);

        String signStr = XJFSignUtil.doSign(stringSignTemp, thirdChannelDto.getAppKey());
        logger.info("[xunjiefu sign msg]: " + signStr);

        params.put("signature", signStr);
        logger.info("xjf request params: " + params);
        String respStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), params);
        if (StringUtil.isEmpty(respStr)) {
            payCreateResult.setResultMessage("支付请求结果为空");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            return payCreateResult;
        }
        logger.info("请求结果：" + respStr);
        Map<Object, Object> respMap = MapUtils.json2Map(respStr);
        logger.info("结果转换：" + respMap);
        String respCode = respMap.get("respCode").toString();
        String respDesc = respMap.get("respDesc").toString();

        if ("0000".equals(respCode) || "P000".equals(respCode)) {
            payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
            payCreateResult.setStatus("true");
            payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER + "");
            payCreateResult.setResultMessage("成功生成支付链接");
            payCreateResult.setPayUrl(respMap.get("payQRCodeUrl").toString());
            logger.info("【通道支付请求成功】------- 成功生成支付链接");
        } else {
            payCreateResult.setStatus("false");
            payCreateResult.setResultCode(SysPayResultConstants.ERROR_PAY_CHANNEL_UNUSABLE + "");
            payCreateResult.setResultMessage("生成跳转地址失败 ===>" + respDesc);
            logger.error("【通道支付请求失败】------- 状态码：" + respCode + " 错误消息：" + respDesc);
        }
        return payCreateResult;
    }
}
