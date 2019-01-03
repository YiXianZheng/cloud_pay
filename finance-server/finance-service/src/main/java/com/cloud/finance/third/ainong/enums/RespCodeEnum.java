package com.cloud.finance.third.ainong.enums;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Getter
public enum RespCodeEnum {
    CODE_SUCCESS("0", "成功"),
    CODE_PARAMS_ERROR("1000", "参数错误"),
    CODE_SIGN_ERROR("1001", "签名错误"),
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
