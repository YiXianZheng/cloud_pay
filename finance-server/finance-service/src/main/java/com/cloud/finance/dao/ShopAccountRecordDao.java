package com.cloud.finance.dao;

import com.cloud.finance.common.dto.ChannelSummaryDto;
import com.cloud.finance.po.ShopAccountRecord;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 交易流水 dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface ShopAccountRecordDao extends BaseMybatisDao<ShopAccountRecord, String> {

    /**
     * 通过关联订单号或者代付单号获取账变记录
     * @param unionOrderNo
     * @return
     */
    ShopAccountRecord getByUnionOrderNo(@Param("unionOrderNo") String unionOrderNo);

    /**
     * 更新账变记录的状态
     * @param id
     * @param status
     */
    void updateStatus(@Param("id") String id, @Param("status") Integer status);

    /**
     * 获取商户通道某个时间段内的统计数据
     * @param sysUserId
     * @param beginTime
     * @param endTime
     * @return
     */
    List<ChannelSummaryDto> channelSummary(@Param("sysUserId") String sysUserId, @Param("beginTime") Date beginTime,
                                           @Param("endTime") Date endTime);

    /**
     * 将指定日期之前的账户流水数据移至历史库
     *          shop_account_record  -->  shop_account_record_log
     * @param beginTime
     */
    void monthMove(@Param("beginTime") Date beginTime);

    /**
     * 更新 step  1 -> 2
     */
    void updateStep();

    /**
     * 更新交易流水第三方通道ID
     * @param id
     * @param channelId
     */
    void updateChannelId(@Param("id") String id, @Param("channelId") String channelId);
}

