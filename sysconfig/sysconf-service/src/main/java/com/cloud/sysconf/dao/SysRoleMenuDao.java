package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.SysRoleMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色菜单关系DAO
 * @Auther Toney
 * @Date 2018/7/26 10:58
 * @Description:
 */
public interface SysRoleMenuDao extends BaseMybatisDao<SysRoleMenu, String> {

    /**
     * 删除角色的菜单配置
     * @param roleId
     */
    void delByRole(String roleId);
}
