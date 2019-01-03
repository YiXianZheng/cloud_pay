package com.cloud.sysconf.controller;

import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统菜单的Controller
 */
@RestController
@RequestMapping("/sys/menu")
public class SysMenuController extends BaseController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 获取登陆用户的菜单
     * @param headers
     * @return
     */
    @RequestMapping("/load")
    public ApiResponse loadMenu(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        try{
            ReturnVo returnVo = sysMenuService.getBySysUser(headerInfoDto.getAuth(), headerInfoDto.getCurUserId());
            return this.toApiResponse(returnVo);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }
}
