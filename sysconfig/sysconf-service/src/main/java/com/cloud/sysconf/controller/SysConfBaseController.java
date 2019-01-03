package com.cloud.sysconf.controller;

import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.enums.RoleTypeEnum;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/8/28 19:48
 * @Description:
 */
@RestController
@RequestMapping(value = "/sys/conf/b")
public class SysConfBaseController extends BaseController {

    /**
     * 获取角色类型集合
     * @return
     */
    @PostMapping(value = "/roleType")
    public ApiResponse roleType(){
        try{
            List<Map<String, Object>> data = RoleTypeEnum.loadRoleType();
            if(data.size()>0){
                return ApiResponse.creatSuccess(data);
            }else{
                return ApiResponse.creatFail(ResponseCode.UNINTENDED_RESULT.RESULT_NULL);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

}
