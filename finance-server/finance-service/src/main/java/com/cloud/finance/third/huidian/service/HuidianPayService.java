package com.cloud.finance.third.huidian.service;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: ${description}
 * @author: zyx
 * @create: 2019-04-27 12:57
 **/
@Service("HuidianPayService")
public class HuidianPayService implements BasePayService {

    private static Logger logger = LoggerFactory.getLogger(HuidianPayService.class);
    @Autowired
    private ShopPayService payService;
    @Autowired
    private RedisClient redisClient;

    private String getBasePayUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_BASE_URL");
    }

    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return createPayUrl(thirdChannelDto, shopPayDto);
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
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

    public MidPayCreateResult createPayUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        String payType = shopPayDto.getChannelTypeCode();
        logger.info("[huidian " + payType + " pay create params] channel: " + thirdChannelDto.getId() + ", sysPayNo: " + shopPayDto.getSysPayOrderNo());
        String actionRespCode = SysPayResultConstants.SUCCESS_MAKE_ORDER + "";
        String actionRespMessage = "生成跳转地址成功";

        String actionRespUrl = getBasePayUrl() + "/d8/huidian_" + shopPayDto.getSysPayOrderNo() + ".html";
        String channelPayOrderNo = shopPayDto.getSysPayOrderNo();
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
        payCreateResult.setResultCode(actionRespCode);
        payCreateResult.setChannelOrderNo(channelPayOrderNo);
        payCreateResult.setResultMessage(actionRespMessage);

        payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
        payCreateResult.setStatus("true");
        payCreateResult.setPayUrl(actionRespUrl);
        return payCreateResult;
    }
}
