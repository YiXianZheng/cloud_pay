package com.cloud.finance.dao;

import com.cloud.finance.common.dto.FinanceOverviewDto;
import com.cloud.finance.po.ShopPay;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ShopPay dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface ShopPayDao extends BaseMybatisDao<ShopPay, String> {

    /**
     * 判断商户订单是够已经存在
     * @param merchantCode
     * @param merchantOrderNo
     * @return
     */
    int checkExist(@Param("merchantCode") String merchantCode, @Param("merchantOrderNo") String merchantOrderNo);

    /**
     * 获取平台概览数据
     * @return
     */
    Map<String, String> getOverViewInfo();

    /**
     * 每个小时报表数据
     * @param hour
     * @param today
     * @return
     */
    Map<String, String> getByHour(@Param("hour") Integer hour, @Param("today") Date today);

    /**
     * 每天的报表数据
     * @param today
     * @return
     */
    Map<String, String> getByDay(@Param("today") Date today);

    /**
     * 每个月的报表数据
     * @param month
     * @param today
     * @return
     */
    Map<String, String> getByMonth(@Param("month") Integer month, @Param("today") Date today);

    /**
     * 获取商户排行
     * @param beginTime
     * @param endTime
     * @param limit
     * @return
     */
    List<Map<String, String>> getMerchantRanking(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime,
                                                 @Param("limit") Integer limit);

    /**
     * 将指定日期之前的订单移至历史库
     *          shop_pay  -->  shop_pay_log
     * @param beginTime
     */
    void monthMove(@Param("beginTime") Date beginTime);

    /**
     * 获取平台某段时间内的统计数据
     * @param beginTime
     * @param endTime
     * @return
     */
    List<FinanceOverviewDto> getOverViewInfo(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime);

    /**
     * 获取所有代理某段时间内的统计数据
     * @param beginTime
     * @param endTime
     * @param agentCode
     * @return
     */
    List<FinanceOverviewDto> getAgentOverViewInfo(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime,
                                                  @Param("agentCode") String agentCode);

    /**
     * 获取所有商户某段时间内的统计数据
     * @param beginTime
     * @param endTime
     * @param merchantCode
     * @return
     */
    List<FinanceOverviewDto> getMerchantOverViewInfo(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime,
                                                     @Param("merchantCode") String merchantCode);

    /**
     * 通过平台订单号获取订单详情
     * @param sysPayOrderNo
     * @return
     */
    ShopPay getBySysOrderNo(@Param("sysPayOrderNo") String sysPayOrderNo);

    /**
     * 更新订单信息
     * @param shopPay
     */
    void updateOrderStatus(ShopPay shopPay);

    /**
     * 更新订单step
     */
    void updateStep();

    /**
     * 更新通道信息
     * @param shopPay
     */
    void updateThirdInfo(ShopPay shopPay);

    /**
     * 通过商户号和商户订单号获取订单信息
     * @param merchantCode
     * @param merchantOrderNo
     * @return
     */
    ShopPay getByMerchantCodeAndOrderNo(@Param("merchantCode") String merchantCode, @Param("merchantOrderNo") String merchantOrderNo);

    /**
     * 汇总某一天的数据
     * @param time
     * @return
     */
    List getDailySummary(@Param("time") Date time);

    /**
     * 汇总商户通道某一天的数据
     * @param time
     * @return
     */
    List getChannelDailySummary(@Param("time") Date time);

    /**
     * 汇总某一天的实时数据
     * @param time
     * @param merchantUser
     * @param agentUser
     * @return
     */
    List getHourSummary(@Param("time") Date time, @Param("merchantUser") String merchantUser, @Param("agentUser") String agentUser);

    /**
     * 更新订单状态
     * @param id
     * @param payStatus
     * @param updateBy
     * @param updateDate
     */
    void updateStatus(@Param("id") String id, @Param("payStatus") Integer payStatus, @Param("updateBy") String updateBy,
                      @Param("updateDate") Date updateDate);

    /**
     * 获取所有商户某段时间内的统计数据
     * @param beginTime
     * @param endTime
     * @param merchantCode
     * @return
     */
    List<FinanceOverviewDto> getMerchantOverChannelInfo(@Param("beginTime") Date beginTime, @Param("endTime") Date endTime,
                                                        @Param("thirdChannelId") String thirdChannelId, @Param("merchantCode") String merchantCode);

}
