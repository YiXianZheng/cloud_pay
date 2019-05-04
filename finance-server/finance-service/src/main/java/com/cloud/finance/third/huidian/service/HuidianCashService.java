package com.cloud.finance.third.huidian.service;

import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @description: ${description}
 * @author: zyx
 * @create: 2019-04-27 13:16
 **/
@Service("HuidianCashService")
public class HuidianCashService implements BaseCashService {

    private Logger logger = LoggerFactory.getLogger(HuidianCashService.class);

    @Override
    public CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto) {
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
