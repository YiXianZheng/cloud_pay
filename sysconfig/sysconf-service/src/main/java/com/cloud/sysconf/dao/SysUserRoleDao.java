package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.SysUserRole;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户角色关系DAO
 * @Auther Toney
 * @Date 2018/7/26 10:59
 * @Description:
 */
public interface SysUserRoleDao extends BaseMybatisDao<SysUserRole, String> {

    /**
     * 删除用户角色配置
     * @param userId
     * @param roleId
     */
    void delRoleUser(@Param("userId") String userId, @Param("roleId") String roleId);
}
