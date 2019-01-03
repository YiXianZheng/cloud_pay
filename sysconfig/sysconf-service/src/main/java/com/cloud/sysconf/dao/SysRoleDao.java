package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 系统角色的DAO
 * @Auther Toney
 * @Date 2018/7/26 10:56
 * @Description:
 */
public interface SysRoleDao extends BaseMybatisDao<SysRole, String> {

    /**
     * 通过角色ID获取组织结构信息
     * @param roleId
     * @return
     */
    Map<String,Object> loadOrigInfo(@Param("roleId") String roleId);

    /**
     * 通过组织ID获取角色列表
     * @param officeId
     * @return
     */
    List<Map<String,String>> getByOfficeId(@Param("officeId") String officeId);

    /**
     * 通过角色类型获取角色信息
     * @param roleType
     * @return
     */
    SysRole getByRoleType(String roleType);
}
