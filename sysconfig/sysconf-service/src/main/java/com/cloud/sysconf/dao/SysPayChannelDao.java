package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.SysPayChannel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * 支付通道配置的DAO
 * @Auther Toney
 * @Date 2018/7/29 17:41
 * @Description:
 */
public interface SysPayChannelDao extends BaseMybatisDao<SysPayChannel, String> {

    /**
     * 查找支付通道
     * @param usable
     * @param agentUser
     * @return
     */
    List<SysPayChannel> querylist(@Param("usable") Integer usable, @Param("agentUser") String agentUser);
}
