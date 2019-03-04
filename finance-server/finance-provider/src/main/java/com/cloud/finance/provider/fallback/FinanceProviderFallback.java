package com.cloud.finance.provider.fallback;

import com.cloud.finance.provider.FinanceProvider;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Auther Toney
 * @Date 2018/7/17 14:53
 * @Description:
 */
@Component
public class FinanceProviderFallback implements FinanceProvider {

    private final Logger log = LoggerFactory.getLogger(FinanceProviderFallback.class);

    @Override
    public ApiResponse initOverview() {
        log.error("========= >> /finance/initOverview 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse initChannelOverview() {
        log.error("========= >> /finance/initChannelOverview 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse dailyUpdate() {
        log.error("========= >> /finance/order/dailyUpdate 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse monthMove() {
        log.error("========= >> /finance/order/monthMove 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse dailySummary() {
        log.error("========= >> /finance/order/dailySummary 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse summaryPaid(String userId, String bankNo, String today) {
        log.error("========= >> /recharge/summaryPaid 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }
}
