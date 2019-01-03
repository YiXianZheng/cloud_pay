package com.cloud.sysconf.service.impl;

import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.dao.SysRoleMenuDao;
import com.cloud.sysconf.po.SysRoleMenu;
import com.cloud.sysconf.service.SysRoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


/**
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
@Service
public class SysRoleMenuServiceImpl extends BaseMybatisServiceImpl<SysRoleMenu, String, SysRoleMenuDao> implements SysRoleMenuService {

    @Autowired
    private SysRoleMenuDao sysRoleMenuDao;

    @Override
    @Transactional
    public ReturnVo save(String type, Set<String> menus, String roleId){
        if("update".equals(type)){
            sysRoleMenuDao.delByRole(roleId);
        }
        for (String str: menus
                ) {
            SysRoleMenu sysRoleMenu = new SysRoleMenu();
            sysRoleMenu.setMenuId(str);
            sysRoleMenu.setRoleId(roleId);

            sysRoleMenuDao.add(sysRoleMenu);
        }
        return ReturnVo.returnSuccess();
    }
}
