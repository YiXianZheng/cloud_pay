package com.cloud.merchant.dao;

import com.cloud.merchant.po.MerchantPayChannel;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 商户用户支付通道配置的dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface MerchantPayChannelDao extends BaseMybatisDao<MerchantPayChannel, String> {

    /**
     * 通过商户ID获取商户可用的通道集合  ,1,2,3,
     * @param merchantUser
     * @return
     */
    String getChannelsToStr(@Param("merchantUser") String merchantUser);

    /**
     * 获取商户某一通道费率
     * @param merchantUser
     * @param channelCode
     * @return
     */
    MerchantPayChannel channelRate(@Param("merchantUser") String merchantUser, @Param("channelCode") String channelCode);

    /**
     * 获取商户的可用通道费率集合
     * @param merchantUser
     * @return
     */
    List<MerchantPayChannel> channelRates(@Param("merchantUser") String merchantUser, @Param("usable") Integer usable);

    /**
     * 更新商户通道配置
     * @param merchantUser
     * @param channelCode
     * @param agentRate
     * @param usable
     * @param updateBy
     * @param updateDate
     */
    void updateChannelRate(@Param("merchantUser") String merchantUser, @Param("channelCode") String channelCode,
                           @Param("agentRate") Double agentRate, @Param("usable") Integer usable,
                           @Param("updateBy") String updateBy, @Param("updateDate") Date updateDate);
}
