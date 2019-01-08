package com.cloud.merchant.controller;

import com.cloud.merchant.common.enums.MerchantTypeEnum;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/7/31 19:48
 * @Description:
 */
@RestController
@RequestMapping(value = "/merchant/b/")
public class MerchantBaseController extends BaseController {

    /**
     * 获取代理类型集合
     * @return
     */
    @PostMapping(value = "/merchantType")
    public ApiResponse loadAgentType(){
        try{
            List<Map<String, Object>> data = MerchantTypeEnum.loadAgentType();
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
