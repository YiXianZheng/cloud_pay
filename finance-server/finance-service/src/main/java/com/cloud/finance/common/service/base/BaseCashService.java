package com.cloud.finance.common.service.base;

import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import org.springframework.stereotype.Service;

@Service
public interface BaseCashService {

    /**
     * 发起代付
     * @param shopRecharge
     * @param thirdChannelDto
     * @return
     */
    CashRespData applyCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto);

    /**
     * 查询代付结果
     * @param shopRecharge
     * @param thirdChannelDto
     * @return
     */
    CashRespData queryCash(ShopRecharge shopRecharge, ThirdChannelDto thirdChannelDto);


    /**
     * 发起代付(系统专用)
     * @param cashReqData
     * @param thirdChannelDto
     * @return
     */
    CashRespData adminApplyCash(CashReqData cashReqData, ThirdChannelDto thirdChannelDto);
}

