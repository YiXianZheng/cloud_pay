package com.cloud.sysconf.controller;

import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.enums.RoleTypeEnum;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.service.SysBankService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping(value = "/sys/bank")
public class SysBankController extends BaseController {

    @Autowired
    private SysBankService sysBankService;

    /**
     * 获取第三方通道的银行编码
     * @return
     */
    @PostMapping(value = "/toChannelCode")
    public ApiResponse toChannelCode(String sysBankCode, String thirdChannelId){
        try{
            String code = sysBankService.getByChannelAndSysCode(sysBankCode, thirdChannelId);
            if(StringUtils.isNotBlank(code)){
                return ApiResponse.creatSuccess("获取通道银行编码成功",code);
            }else{
                return ApiResponse.creatFail(ResponseCode.UNINTENDED_RESULT.RESULT_NULL);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }


    /**
     * 获取第三方通道的银行编码
     * @return
     */
    @PostMapping(value = "/getBankNameByCode")
    public ApiResponse getBankNameByCode(String sysBankCode){
        try{
            String bankName = sysBankService.getBankNameByCode(sysBankCode);
            if(StringUtils.isNotBlank(bankName)){
                return ApiResponse.creatSuccess("获取通道银行名称成功", bankName);
            }else{
                return ApiResponse.creatFail(ResponseCode.UNINTENDED_RESULT.RESULT_NULL);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 获取系统银行列表
     * @return
     */
    @PostMapping(value = "/list")
    public ApiResponse list(String sysBankCode, String thirdChannelId){
        try{
            return ApiResponse.creatSuccess(sysBankService.getSysSelectList());
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

}
