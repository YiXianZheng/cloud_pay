package com.cloud.sysconf.controller;

import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysOfficeDto;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.service.SysOfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 系统组织结构的Controller
 */
@RestController
@RequestMapping("/sys/office")
public class SysOfficeController extends BaseController {

    @Autowired
    private SysOfficeService sysOfficeService;

    /**
     * 获取组织机构树状图
     * @param headers
     * @return
     */
    @RequestMapping("/load")
    public ApiResponse loadMenu(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        return this.toApiResponse(sysOfficeService.getBySysUser(headerInfoDto.getCurUserId()));
    }

    /**
     * 获取角色详情
     * @param id
     * @return
     */
    @RequestMapping("/detail")
    public ApiResponse detail(@RequestParam("id") String id){
        return this.toApiResponse(sysOfficeService.getDetail(id));
    }

    /**
     * 新增或保存组织结构
     * @param headers
     * @param sysOfficeDto
     * @return
     */
    @RequestMapping("/save")
    public ApiResponse save(@RequestHeader HttpHeaders headers, @RequestBody(required = true) SysOfficeDto sysOfficeDto){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        return this.toApiResponse(sysOfficeService.saveOrUpdate(sysOfficeDto, headerInfoDto));
    }
}
