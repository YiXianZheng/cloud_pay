package com.cloud.finance.provider;

import com.cloud.finance.provider.fallback.FinanceProviderFallback;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;


/**
 * 提供供其它服务调用的接口
 * @Auther Toney
 * @Date 2018/7/9 15:23
 * @Description:
 */

//@EnableAspectJAutoProxy(proxyTargetClass = true)  //若需要AOP  此注解可能需要
@FeignClient(name = "finance-service", fallback = FinanceProviderFallback.class, decode404 = true)
public interface FinanceProvider {

    /**
     * 更新财务数据
     * @return
     */
    @RequestMapping(value = "/finance/initOverview", method = RequestMethod.POST)
    ApiResponse initOverview();

    /**
     * 更新通道财务数据
     * @return
     */
    @RequestMapping(value = "/finance/initChannelOverview", method = RequestMethod.POST)
    ApiResponse initChannelOverview();

    /**
     * 更新昨天的订单状态 step 1  -->  step 2
     * @return
     */
    @RequestMapping(value = "/finance/order/dailyUpdate", method = RequestMethod.POST)
    ApiResponse dailyUpdate();

    /**
     * 每月将一个月之前的订单数据移至历史库 每天状态更新之后操作
     *      e.g. 加入今天是八月某日，那么就把六月一号之前的数据移至历史库
     * @return
     */
    @RequestMapping(value = "/finance/order/monthMove", method = RequestMethod.POST)
    ApiResponse monthMove();

    /**
     * 更新昨日交易汇总数据到Redis中
     * @return
     */
    @RequestMapping(value = "/finance/order/dailySummary", method = RequestMethod.POST)
    ApiResponse dailySummary();

    @RequestMapping(value = "/recharge/summaryPaid", method = RequestMethod.POST)
    ApiResponse summaryPaid(@RequestParam String userId, @RequestParam String bankNo, @RequestParam Date today);
}
