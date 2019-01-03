package com.cloud.finance.third.zhonghua.service;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
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
import com.cloud.sysconf.common.utils.MD5Util;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.provider.SysBankProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/9/20 21:12
 * @Description:
 */
@Service("ZhonghuaPayService")
public class ZhonghuaPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(ZhonghuaPayService.class);

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
        logger.info("[zhonghua create H5 params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        String actionRespCode = SysPayResultConstants.SUCCESS_MAKE_ORDER+"";
        String actionRespMessage = "生成跳转地址成功";

        String actionRespUrl = getBasePayUrl() + "/d8/zh_" + shopPayDto.getSysPayOrderNo() + ".html";
        String channelPayOrderNo = shopPayDto.getSysPayOrderNo();
        MidPayCreateResult payCreateResult = new MidPayCreateResult();
        payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
        payCreateResult.setResultCode(actionRespCode);
        payCreateResult.setStatus("true");
        payCreateResult.setResultMessage(actionRespMessage);
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
        payCreateResult.setChannelOrderNo(channelPayOrderNo);
        payCreateResult.setPayUrl(actionRespUrl);

        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return  null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
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
        return null;
    }
}
