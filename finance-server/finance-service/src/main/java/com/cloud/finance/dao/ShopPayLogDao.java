package com.cloud.finance.dao;

import com.cloud.finance.common.dto.FinanceOverviewDto;
import com.cloud.finance.po.ShopPayLog;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * ShopPay dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface ShopPayLogDao extends BaseMybatisDao<ShopPayLog, String> {

    /**
     * 获取平台历史的统计数据
     * @param beginTime
     * @param endTime
     * @return
     */
    List<FinanceOverviewDto> getOverViewInfo(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    /**
     * 获取所有代理历史的统计数据
     * @param beginTime
     * @param endTime
     * @param agentCode
     * @return
     */
    List<FinanceOverviewDto> getAgentOverViewInfo(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime, @Param("agentCode") String agentCode);

    /**
     * 获取所有商户历史的统计数据
     * @param beginTime
     * @param endTime
     * @param merchantCode
     * @return
     */
    List<FinanceOverviewDto> getMerchantOverViewInfo(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime, @Param("merchantCode") String merchantCode);

    /**
     * 通过商户号和商户订单号获取订单信息
     * @param merchantCode
     * @param merchantOrderNo
     * @return
     */
    ShopPayLog getByMerchantCodeAndOrderNo(@Param("merchantCode") String merchantCode, @Param("merchantOrderNo") String merchantOrderNo);
}
