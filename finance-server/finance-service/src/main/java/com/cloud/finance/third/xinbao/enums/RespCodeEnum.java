package com.cloud.finance.third.xinbao.enums;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Getter
public enum RespCodeEnum {
    //成功
    EXECUTE_SUCCESS("EXECUTE_SUCCESS", "交易成功"),
    //处理中
    EXECUTE_PROCESSING("EXECUTE_PROCESSING", "交易处理中"),
    //请求超时复查
    TIME_OUT("TIME_OUT", "请求超时"),
    //以下都是失败或错误
    EXECUTE_FAIL("EXECUTE_FAIL", "交易失败"),
    INTERNAL_ERROR("INTERNAL_ERROR", "系统内部错误"),
    SERVICE_NOT_FOUND_ERROR	("SERVICE_NOT_FOUND_ERROR", "服务不存在"),
    PARAMETER_ERROR("PARAMETER_ERROR", "参数错误"),
    PARAM_FORMAT_ERROR("PARAM_FORMAT_ERROR", "参数格式错误"),
    UNAUTHENTICATED("UNAUTHENTICATED", "认证(签名)错误"),
    UNAUTHORIZED("UNAUTHORIZED", "未授权的服务"),
    REQUEST_NO_NOT_UNIQUE("REQUEST_NO_NOT_UNIQUE", "商户请求号不唯一"),
    FIELD_NOT_UNIQUE("FIELD_NOT_UNIQUE", "对象字段重复"),
    REDIRECT_URL_NOT_EXIST("REDIRECT_URL_NOT_EXIST", "重定向服务需设置redirectUrl"),
    PARTNER_NOT_REGISTER("PARTNER_NOT_REGISTER", "合作伙伴没有注册"),
    PARTNER_NOT_PRODUCT("PARTNER_NOT_PRODUCT", "商户没有配置产品"),
    UNSUPPORTED_SECHEME("UNSUPPORTED_SECHEME", "不支持的请求协议"),
    ;

    private String code;

    private String val;

    RespCodeEnum(String code, String val){
        this.code = code;
        this.val = val;
    }

    /**
     * 通过code获取val
     * @param code
     * @return
     */
    public static String getRespValByCode(String code){
        if(StringUtils.isBlank(code)) return "";
        for (RespCodeEnum respCode : RespCodeEnum.values()){
            if(code.equals(respCode.code)){
                return respCode.val;
            }
        }
        return "";
    }
}
