package com.cloud.sysconf.controller;

import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysDictDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.service.SysDictService;
import com.cloud.sysconf.service.SysMenuService;
import com.cloud.sysuser.common.DTO.SysUserFormDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 系统字典的Controller
 */
@RestController
@RequestMapping("/sys/dict")
public class SysDictController extends BaseController {

    @Autowired
    private SysDictService sysDictService;

    @RequestMapping("/refreshRedis")
    public ApiResponse refreshRedis(){
        try{
            ReturnVo returnVo = sysDictService.refreshRedis();
            return this.toApiResponse(returnVo);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 分页搜索系统字典 适用于列表分页
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping("/tablePage")
    public ApiResponse tablePageForAgent(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = sysDictService.listForTablePage(pageQuery, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 新增或保存系统字典
     * @param headers
     * @param sysDictDto
     * @return
     */
    @PostMapping("/save")
    public ApiResponse save(@RequestHeader HttpHeaders headers, @RequestBody(required = true) SysDictDto sysDictDto){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        return this.toApiResponse(sysDictService.saveOrUpdate(sysDictDto, headerInfoDto));
    }

    /**
     * 获取系统字典信息
     * @param code
     * @returnk
     */
    @PostMapping("/detail")
    public ApiResponse detail(@RequestParam String code){
        return this.toApiResponse(sysDictService.detail(code));
    }
}
