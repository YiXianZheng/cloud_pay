package com.cloud.agent.dao;

import com.cloud.agent.po.AgentUser;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 代理用户的dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface AgentUserDao extends BaseMybatisDao<AgentUser, String> {

    /**
     * 更新代理状态
     * @param agentUser
     */
    int updateStatus(AgentUser agentUser);

    /**
     * 逻辑删除代理商
     * @param agentUser
     * @return
     */
    int delAgent(AgentUser agentUser);

    /**
     * 获取可用的代理商  审核通过、未删除
     * @return
     */
    List<AgentUser> getActiveAgent();

    /**
     * 通过代理号获取代理信息
     * @param agentCode
     * @return
     */
    AgentUser getByCode(@Param("agentCode") String agentCode);
}
