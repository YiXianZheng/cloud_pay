package com.cloud.finance.controller;

import com.cloud.finance.common.enums.PayStatusEnum;
import com.cloud.finance.common.enums.RechargeStatusEnum;
import com.cloud.finance.common.enums.SysOrderTypeEnum;
import com.cloud.finance.common.enums.SysPaymentTypeEnum;
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
@RequestMapping(value = "/finance/b")
public class FinanceBaseController extends BaseController {

    /**
     * 获取订单状态集合
     * @return
     */
    @PostMapping(value = "/payStatus")
    public ApiResponse payStatus(){
        try{
            List<Map<String, Object>> data = PayStatusEnum.loadPayStatus();
            if(data.size()>0){
                return ApiResponse.creatSuccess(data);
            }else{
                return ApiResponse.creatFail(ResponseCode.UNINTENDED_RESULT.RESULT_NULL);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 获取订单类型集合
     * @return
     */
    @PostMapping(value = "/orderType")
    public ApiResponse orderType(){
        try{
            List<Map<String, Object>> data = SysOrderTypeEnum.loadOrderType();
            if(data.size()>0){
                return ApiResponse.creatSuccess(data);
            }else{
                return ApiResponse.creatFail(ResponseCode.UNINTENDED_RESULT.RESULT_NULL);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 获取支付类型集合
     * @return
     */
    @PostMapping(value = "/paymentType")
    public ApiResponse paymentType(){
        try{
            List<Map<String, Object>> data = SysPaymentTypeEnum.loadPaymentType();
            if(data.size()>0){
                return ApiResponse.creatSuccess(data);
            }else{
                return ApiResponse.creatFail(ResponseCode.UNINTENDED_RESULT.RESULT_NULL);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 获取代付订单状态集合
     * @return
     */
    @PostMapping(value = "/rechargeStatus")
    public ApiResponse rechargeStatus(){
        try{
            List<Map<String, Object>> data = RechargeStatusEnum.loadRechargeStatus();
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
