package com.cloud.sysconf.service;

import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.po.SysRoleMenu;

import java.util.Set;

/**
 * 系统角色菜单的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface SysRoleMenuService extends BaseMybatisService<SysRoleMenu, String> {

    /**
     * 保存角色菜单权限
     * @param type      update:修改（需删除原配置）
     * @param menus     菜单集合
     * @param roleId    角色ID
     * @return
     */
    ReturnVo save(String type, Set<String> menus, String roleId);
}
