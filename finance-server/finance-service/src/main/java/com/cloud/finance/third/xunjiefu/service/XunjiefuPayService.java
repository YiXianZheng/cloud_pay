package com.cloud.finance.third.xunjiefu.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return null;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
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
}
