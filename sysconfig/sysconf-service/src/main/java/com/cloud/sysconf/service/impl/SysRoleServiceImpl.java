package com.cloud.sysconf.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysMenuDto;
import com.cloud.sysconf.common.dto.SysOfficeDto;
import com.cloud.sysconf.common.dto.SysRoleDto;
import com.cloud.sysconf.common.enums.RoleTypeEnum;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.utils.page.PageResult;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.dao.SysRoleDao;
import com.cloud.sysconf.dao.SysUserRoleDao;
import com.cloud.sysconf.po.SysMenu;
import com.cloud.sysconf.po.SysOffice;
import com.cloud.sysconf.po.SysRole;
import com.cloud.sysconf.po.SysUserRole;
import com.cloud.sysconf.service.SysMenuService;
import com.cloud.sysconf.service.SysOfficeService;
import com.cloud.sysconf.service.SysRoleMenuService;
import com.cloud.sysconf.service.SysRoleService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
@Service
public class SysRoleServiceImpl extends BaseMybatisServiceImpl<SysRole, String, SysRoleDao> implements SysRoleService {

    @Autowired
    private SysRoleDao sysRoleDao;
    @Autowired
    private SysOfficeService sysOfficeService;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;

    @Override
    public ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto) {
        try {
            PageResult pageResult = this.queryForTablePage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            List<SysRoleDto> roleList = initRoleInfo(pageResult.getData());

            pageResult.setData(roleList);
            return ReturnVo.returnSuccess(JSONObject.toJSON(pageResult));
        }catch (Exception e){
            return ReturnVo.returnError(ResponseCode.Base.ERROR);
        }
    }

    /**
     * 初始化角色信息
     * @param roleList
     * @return
     */
    private List<SysRoleDto> initRoleInfo(List<SysRole> roleList){
        List<SysRoleDto> roles = new ArrayList<>();
        for (SysRole sysRole : roleList) {
            SysRoleDto sysRoleDto = new SysRoleDto();
            BeanUtils.copyProperties(sysRole, sysRoleDto);

            sysRoleDto.setRoleType(RoleTypeEnum.getRoleNameByCode(sysRoleDto.getRoleType()));
            SysOffice sysOffice = sysOfficeService.getById(sysRole.getOfficeId());
            if(sysOffice != null){
                String temp = "";
                if("0".equals(sysOffice.getParentId())){
                    SysOffice parentOffice = sysOfficeService.getById(sysRole.getOfficeId());
                    temp = parentOffice.getName() + " > ";
                }
                temp += sysOffice.getName();
                sysRoleDto.setOfficeInfo(temp);
            }

            roles.add(sysRoleDto);
        }
        return roles;
    }

    @Override
    @Transactional
    public ReturnVo saveOrUpdate(SysRoleDto sysRoleDto, HeaderInfoDto headerInfoDto) {
        try{
            SysRole sysRole = new SysRole();

            Set<String> checkMenu = new HashSet<>();
            List<String> checkedKeys = sysRoleDto.getCheckedKeys();
            if(checkedKeys != null && checkedKeys.size()>0){
                for (String menuId: checkedKeys
                     ) {
                    getParentId(checkMenu, menuId);
                }
            }

            if(StringUtils.isBlank(sysRole.getId())){
                BeanUtils.copyProperties(sysRoleDto, sysRole);
                sysRole.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                sysRoleDao.add(sysRole);

                sysRoleMenuService.save("add", checkMenu, sysRole.getId());
            }else{
                sysRole = sysRoleDao.getById(sysRoleDto.getId());
                sysRole.setName(sysRoleDto.getName());
                sysRole.setEnname(sysRoleDto.getEnname());
                sysRole.setOfficeId(sysRoleDto.getOfficeId());
                sysRole.setRoleType(sysRoleDto.getRoleType());
                sysRole.setUsable(sysRoleDto.getUsable());

                sysRole.preUpdate(headerInfoDto.getCurUserId());
                sysRoleDao.update(sysRole);

                sysRoleMenuService.save("update", checkMenu, sysRole.getId());
            }

            return ReturnVo.returnSuccess();
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * 获取自己及所有父级的ID
     * @param set
     * @param menuId
     * @return
     */
    private Set<String> getParentId(Set<String> set, String menuId){
        SysMenu sysMenu = sysMenuService.getById(menuId);
        if(sysMenu != null){
            set.add(sysMenu.getId());
        }
        if(sysMenu != null && !"0".equals(sysMenu.getParentId())){
            return getParentId(set, sysMenu.getParentId());
        }
        return set;
    }

    @Override
    public ReturnVo getDetail(String id) {
        try{
            SysRole sysRole = sysRoleDao.getById(id);
            if(sysRole == null || SysRole.DEL_FLAG_ALREADY == sysRole.getDelFlag())
                return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "角色不存在或已删除"));

            SysRoleDto sysRoleDto = new SysRoleDto();
            BeanUtils.copyProperties(sysRole, sysRoleDto);

            //获取所有叶子节点的ID
            List<String> checkedKeys = new ArrayList<>();
            ReturnVo returnVo = sysMenuService.getByRole(id);
            if(ReturnVo.SUCCESS == returnVo.code){
                List<SysMenuDto> menus = (List<SysMenuDto>) returnVo.object;
                for (SysMenuDto sysMenuDto: menus
                     ) {
                    if(sysMenuDto.getChildren().size() == 0){
                        checkedKeys.add(sysMenuDto.getId());
                    }
                }
                sysRoleDto.setCheckedKeys(checkedKeys);
            }else{
                return returnVo;
            }

            return ReturnVo.returnSuccess(sysRoleDto);
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnError();
        }
    }

    @Override
    @Transactional
    public ReturnVo saveRoleUser(String userId, String roleId) {
        sysUserRoleDao.delRoleUser(userId, roleId);

        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setRoleId(roleId);
        sysUserRole.setUserId(userId);
        sysUserRoleDao.add(sysUserRole);

        Map<String, Object> map = sysRoleDao.loadOrigInfo(roleId);
        if(map == null || map.size()==0){
            map.put("role", "");
            map.put("company", "");
            map.put("department", "");
        }
        map.put("userId", userId);
        map.put("roleId", roleId);

        return ReturnVo.returnSuccess(map);
    }

    @Override
    @Transactional
    public ReturnVo saveRoleUserCancel(String userId, String roleId) {
        sysUserRoleDao.delRoleUser(userId, roleId);

        SysUserRole sysUserRole = new SysUserRole();
        sysUserRole.setRoleId(roleId);
        sysUserRole.setUserId(userId);
        sysUserRoleDao.add(sysUserRole);

        return ReturnVo.returnSuccess();
    }

    @Override
    public ReturnVo getConfig(HeaderInfoDto headerInfoDto) {
         ReturnVo returnVo = sysMenuService.getByRole(headerInfoDto.getRoleId());
         if(ReturnVo.SUCCESS != returnVo.code){
             return returnVo;
         }
         List<SysMenuDto> menus = (List<SysMenuDto>) returnVo.object;

         ReturnVo returnVo1 = sysOfficeService.getAllUsable();
         if(ReturnVo.SUCCESS != returnVo1.code){
             return returnVo1;
         }
         List<SysOfficeDto> offices = (List<SysOfficeDto>) returnVo1.object;

         Map<String, Object> config = new HashMap<>();
         config.put("menus", menus);
         config.put("offices", offices);

         return ReturnVo.returnSuccess(config);
    }

    @Override
    public ReturnVo roleList(String officeId) {
        List<Map<String, String>> roles = sysRoleDao.getByOfficeId(officeId);
        return ReturnVo.returnSuccess(roles);
    }

    @Override
    public ReturnVo saveDefaultRoleUser(String userId, String roleType) {
        SysRole role = sysRoleDao.getByRoleType(roleType);
        return this.saveRoleUser(userId, role.getId());

    }
}
