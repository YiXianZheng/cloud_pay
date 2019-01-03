package com.cloud.finance.third.anyinfu.service;

import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.hankou.utils.HKUtil;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("AnyinfuCashService")
public class AnyinfuCashService implements BaseCashService {

    private static Logger logger = LoggerFactory.getLogger(AnyinfuCashService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysBankProvider sysBankProvider;

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {

        logger.info("...[anyinfu cash] cash order action...");

        CashRespData cashRespData = new CashRespData();
        cashRespData.setStatus(CashRespData.STATUS_ERROR);
        cashRespData.setMsg("代付异常");

        // 商户号
        String mch_id = thirdChannelDto.getMerchantId();
        // 商户订单号
        String out_trade_out = shopRecharge.getThirdChannelOrderNo();
        // 总金额
        String total_fee = String.valueOf((int) (shopRecharge.getRechargeMoney() * 100));
        // 提现类别
        String withdraw_type = "AVAILABLE";
        // 银行编码
        String bank_code = shopRecharge.getBankCode();
        // 银行联行号
        String bank_no = shopRecharge.getBankBin();
        // 收款人
        String payee = shopRecharge.getBankAccount();
        // 收款人身份证号
        String id_card_no = "23232323232323";
        // 手机号
        String mobile = "15004764771";
        // 随机操作符
        String nonce_str = HKUtil.getRandomString(32);
        // 代付通道编码
        String sp_id = "22";
        // 省份代码
        String province = shopRecharge.getProvince();
        // 城市代码
        String city = shopRecharge.getCity();
        // 银行名称
        String card_bank = shopRecharge.getBankCode();
        // 支行名称
        String card_sub_bank = shopRecharge.getBankSubbranch();
        // 渠道编码
        String channel_type = "ALIPAY_WAP_LB";
        return null;
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
