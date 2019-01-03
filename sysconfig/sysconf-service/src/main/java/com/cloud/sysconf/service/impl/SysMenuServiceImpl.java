package com.cloud.sysconf.service.impl;

import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.SysMenuDto;
import com.cloud.sysconf.common.utils.BuildTree;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.dao.SysMenuDao;
import com.cloud.sysconf.po.SysMenu;
import com.cloud.sysconf.service.SysMenuService;
import com.cloud.sysuser.provider.SysUserProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
@Service
public class SysMenuServiceImpl extends BaseMybatisServiceImpl<SysMenu, String, SysMenuDao> implements SysMenuService {

    @Autowired
    private SysUserProvider sysUserProvider;
    @Autowired
    private SysMenuDao sysMenuDao;

    @Override
    public ReturnVo getBySysUser(String rootId, String curUserId) {
        ReturnVo returnVo = new ReturnVo();
        List<SysMenu> list = sysMenuDao.querySysMenu(rootId, curUserId);
        List<SysMenuDto> menus = new ArrayList<>();
        for (SysMenu sysMenu: list) {
            SysMenuDto menuDTO = new SysMenuDto();
            BeanUtils.copyProperties(sysMenu, menuDTO);

            menus.add(menuDTO);
        }
        returnVo.code = ReturnVo.SUCCESS;
        returnVo.object = BuildTree.build(menus);
        return returnVo;
    }

    @Override
    public ReturnVo getByRole(String roleId) {
        try{
            List<SysMenu> list = sysMenuDao.getByRole(roleId);
            List<SysMenuDto> menus = new ArrayList<>();
            for (SysMenu sysMenu: list) {
                SysMenuDto menuDTO = new SysMenuDto();
                BeanUtils.copyProperties(sysMenu, menuDTO);

                menus.add(menuDTO);
            }
            return ReturnVo.returnSuccess(BuildTree.buildMenu(menus));
        }catch (Exception e){
            return ReturnVo.returnError();
        }
    }

}
