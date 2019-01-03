package com.cloud.agent.dao;

import com.cloud.agent.po.AgentPayChannel;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代理用户支付通道配置的dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface AgentPayChannelDao extends BaseMybatisDao<AgentPayChannel, String> {

    /**
     * 通过代理ID获取代理可用的通道集合  ,1,2,3,
     * @param agentUser
     * @return
     */
    String getChannelsToStr(@Param("agentUser") String agentUser);

    /**
     * 获取代理某一通道费率
     * @param agentUser
     * @param channelCode
     * @return
     */
    AgentPayChannel channelRate(@Param("agentUser")String agentUser, @Param("channelCode")String channelCode);

    /**
     * 通过代理ID获取通道费率集合
     * @param agentUser
     * @param usable
     * @return
     */
    List<AgentPayChannel> channelRates(@Param("agentUser")String agentUser, @Param("usable")Integer usable);
}
