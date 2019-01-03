package com.cloud.sysconf.service;

import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.po.SysMenu;

/**
 * 系统菜单的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface SysMenuService extends BaseMybatisService<SysMenu, String> {

    /**
     * 获取用户的菜单
     * @param rootId 根结点ID
     * @param curUserId
     * @return
     */
    ReturnVo getBySysUser(String rootId, String curUserId);

    /**
     * 通过角色ID获取菜单
     * @param roleId
     * @return
     */
    ReturnVo getByRole(String roleId);

}
