package com.cloud.finance.service.impl;

import com.cloud.finance.common.dto.FinanceOverviewDto;
import com.cloud.finance.common.dto.RedisFinanceDto;
import com.cloud.finance.dao.ShopPayLogDao;
import com.cloud.finance.po.ShopPayLog;
import com.cloud.finance.service.ShopPayLogService;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.MyBeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @Auther Toney
 * @Date 2018/7/29 17:52
 * @Description:
 */
@Service
public class ShopPayLogServiceImpl extends BaseMybatisServiceImpl<ShopPayLog, String, ShopPayLogDao> implements ShopPayLogService {

    @Autowired
    private ShopPayLogDao shopPayLogDao;
    @Autowired
    private RedisClient redisClient;


    @Override
    public void countHistory() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DATE, 1);// 设为当前月的1号
        calendar.add(Calendar.MONTH, -1);// 减两个月


        Date startTime = DateUtil.getPreviousMonthFirst(calendar.getTime());
        Date endTime = DateUtil.getPreviousMonthEnd(calendar.getTime());

        List<FinanceOverviewDto> overview = shopPayLogDao.getOverViewInfo(startTime, endTime);
        List<FinanceOverviewDto> overview2 = shopPayLogDao.getAgentOverViewInfo(startTime, endTime, null);
        List<FinanceOverviewDto> overview3 = shopPayLogDao.getMerchantOverViewInfo(startTime, endTime, null);

        overview.addAll(overview2);
        overview.addAll(overview3);

        for (FinanceOverviewDto finance: overview
                ) {
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, finance.getUserCode());
            RedisFinanceDto redisFinanceDto = null;
            //已经存在Redis缓存
            if(map != null && map.size()>0){
                redisFinanceDto = RedisFinanceDto.map2Object(map);

                //总利润
                Double totalCharge = finance.getTotalCharge();
                redisFinanceDto.setHistoryTotalCharge(totalCharge);

                //总交易额
                Double totalMoney = finance.getTotalMoney();
                redisFinanceDto.setHistoryTotalMoney(totalMoney);

                //总订单数
                Integer totalOrder = finance.getTotalOrder();
                redisFinanceDto.setHistoryTotalOrder(totalOrder);

                //总成功订单数
                Integer totalSuccessOrder = finance.getTotalSuccessOrder();
                redisFinanceDto.setHistoryTotalSuccessOrder(totalSuccessOrder);

                //总代付笔数
                Integer totalPaid = finance.getTotalPaid();
                redisFinanceDto.setHistoryTotalPaid(totalPaid.intValue());

                //总成功代付笔数
                Integer totalSuccessPaid = finance.getTotalSuccessPaid();
                redisFinanceDto.setHistoryTotalSuccessPaid(totalSuccessPaid);

                //总风控订单
                Integer totalRisk = finance.getTotalRiskControlOrder();
                redisFinanceDto.setHistoryTotalRiskControlOrder(totalRisk.intValue());


            }else{
                redisFinanceDto = new RedisFinanceDto();

                //总利润
                Double totalCharge = finance.getTotalCharge();
                redisFinanceDto.setHistoryTotalCharge(totalCharge);
                redisFinanceDto.setTotalCharge(0D);
                redisFinanceDto.setDailyTotalCharge(0D);

                //总交易额
                Double totalMoney = finance.getTotalMoney();
                redisFinanceDto.setHistoryTotalMoney(totalMoney);
                redisFinanceDto.setTotalMoney(0D);
                redisFinanceDto.setDailyTotalMoney(0D);

                //总订单数
                Integer totalOrder = finance.getTotalOrder();
                redisFinanceDto.setHistoryTotalOrder(totalOrder);
                redisFinanceDto.setTotalOrder(0);
                redisFinanceDto.setDailyTotalOrder(0);

                //总成功订单数
                Integer totalSuccessOrder = finance.getTotalSuccessOrder();
                redisFinanceDto.setHistoryTotalSuccessOrder(totalSuccessOrder);
                redisFinanceDto.setTotalSuccessOrder(0);
                redisFinanceDto.setDailyTotalSuccessOrder(0);

                //总代付笔数
                Integer totalPaid = finance.getTotalPaid();
                redisFinanceDto.setHistoryTotalPaid(totalPaid.intValue());
                redisFinanceDto.setTotalPaid(0);
                redisFinanceDto.setDailyTotalPaid(0);

                //总成功代付笔数
                Integer totalSuccessPaid = finance.getTotalSuccessPaid();
                redisFinanceDto.setHistoryTotalSuccessPaid(totalSuccessPaid);
                redisFinanceDto.setTotalSuccessPaid(0);
                redisFinanceDto.setDailyTotalSuccessPaid(0);

                //总风控订单
                Integer totalRisk = finance.getTotalRiskControlOrder();
                redisFinanceDto.setHistoryTotalRiskControlOrder(totalRisk.intValue());
                redisFinanceDto.setTotalRiskControlOrder(0);
                redisFinanceDto.setDailyTotalRiskControlOrder(0);
            }

            redisClient.SetHsetJedis(RedisConfig.ORDER_COUNT_DB, finance.getUserCode(), MyBeanUtil.transBean2Map2(redisFinanceDto));
        }

    }

    @Override
    public ShopPayLog getByMerchantCodeAndOrderNo(String merchantCode, String merchantOrderNo) {
        return shopPayLogDao.getByMerchantCodeAndOrderNo(merchantCode, merchantOrderNo);
    }
}
