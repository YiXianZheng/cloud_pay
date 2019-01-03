package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.SysMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统菜单dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface SysMenuDao extends BaseMybatisDao<SysMenu, String> {

    /**
     * 获取用户的菜单
     * @param rootId
     * @param userId
     * @return
     */
    List<SysMenu> querySysMenu(@Param("rootId") String rootId, @Param("userId") String userId);

    /**
     * 通过角色ID获取菜单列表
     * @param roleId
     * @return
     */
    public List<SysMenu> getByRole(@Param("roleId") String roleId);

}
