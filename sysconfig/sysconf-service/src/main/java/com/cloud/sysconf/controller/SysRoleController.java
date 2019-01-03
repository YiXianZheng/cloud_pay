package com.cloud.sysconf.controller;

import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysRoleDto;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 系统角色的Controller
 */
@RestController
@RequestMapping("/sys/role")
public class SysRoleController extends BaseController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 分页查询系统角色
     * @param pageQuery
     * @param headers
     * @return
     */
    @RequestMapping("/tablePage")
    public ApiResponse loadMenu(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        return this.toApiResponse(sysRoleService.listForTablePage(pageQuery, headerInfoDto));
    }

    /**
     * 获取角色详情
     * @param id
     * @return
     */
    @RequestMapping("/detail")
    public ApiResponse detail(@RequestParam("id") String id){
        return this.toApiResponse(sysRoleService.getDetail(id));
    }

    /**
     * 获取编辑角色的配置信息
     * @return
     */
    @RequestMapping("/config")
    public ApiResponse config(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        return this.toApiResponse(sysRoleService.getConfig(headerInfoDto));
    }

    /**
     * 新增或保存系统角色
     * @param headers
     * @param sysRoleDto
     * @return
     */
    @RequestMapping("/save")
    public ApiResponse save(@RequestHeader HttpHeaders headers, @RequestBody(required = true) SysRoleDto sysRoleDto){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        return this.toApiResponse(sysRoleService.saveOrUpdate(sysRoleDto, headerInfoDto));
    }

    /**
     * 更新系统用户的角色
     * @param userId
     * @param roleId
     * @return
     */
    @RequestMapping("/saveRoleUser")
    public ApiResponse saveRoleUser(@RequestParam(required = true) String userId, @RequestParam(required = true) String roleId){
        return this.toApiResponse(sysRoleService.saveRoleUser(userId, roleId));
    }

    /**
     * 取消更新系统用户的角色
     * @param userId
     * @param roleId
     * @return
     */
    @RequestMapping("/saveRoleUserCancel")
    public ApiResponse saveRoleUserCancel(@RequestParam(required = true) String userId, @RequestParam(required = true) String roleId){
        return this.toApiResponse(sysRoleService.saveRoleUserCancel(userId, roleId));
    }

    /**
     * 获取组织机构下的角色列表
     * @param officeId
     * @return
     */
    @RequestMapping("/list")
    public ApiResponse roleList(@RequestParam(required = true) String officeId){
        return this.toApiResponse(sysRoleService.roleList(officeId));
    }

    /**
     * 通过角色类型保存默认角色
     * @param userId
     * @param roleType
     * @return
     */
    @RequestMapping("/saveDefaultRoleUser")
    public ApiResponse saveDefaultRoleUser(@RequestParam(required = true) String userId, @RequestParam(required = true) String roleType){
        return this.toApiResponse(sysRoleService.saveDefaultRoleUser(userId, roleType));
    }
}
