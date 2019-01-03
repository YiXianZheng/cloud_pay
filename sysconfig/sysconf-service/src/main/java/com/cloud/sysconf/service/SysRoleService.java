package com.cloud.sysconf.service;

import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysRoleDto;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.po.SysRole;

/**
 * 系统角色的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface SysRoleService extends BaseMybatisService<SysRole, String> {

    /**
     * 分页获取角色信息   适用于列表型的分页
     * @param pageQuery  分页条件
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     * 新增或保存角色信息
     * @param sysRoleDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo saveOrUpdate(SysRoleDto sysRoleDto, HeaderInfoDto headerInfoDto);

    /**
     * 获取角色详情
     * @param id
     * @return
     */
    ReturnVo getDetail(String id);

    /**
     * 保存用户的角色
     * @param userId
     * @param roleId
     * @return
     */
    ReturnVo saveRoleUser(String userId, String roleId);

    /**
     * 保存用户的角色
     * @param userId
     * @param roleId
     * @return
     */
    ReturnVo saveRoleUserCancel(String userId, String roleId);

    /**
     * 获取编辑角色的配置信息
     * @param headerInfoDto
     * @return
     */
    ReturnVo getConfig(HeaderInfoDto headerInfoDto);

    /**
     * 获取角色列表
     * @param officeId
     * @return
     */
    ReturnVo roleList(String officeId);

    /**
     * 根据角色类型保存默认角色
     * @param userId
     * @param roleType
     * @return
     */
    ReturnVo saveDefaultRoleUser(String userId, String roleType);
}
