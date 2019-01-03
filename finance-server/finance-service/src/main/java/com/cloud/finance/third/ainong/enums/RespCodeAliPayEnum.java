package com.cloud.finance.third.ainong.enums;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Getter
public enum RespCodeAliPayEnum {
    CODE_SUCCESS("000000", "交易成功"),
    CODE_DOING("000001", "交易处理中")
    ;

    private String code;

    private String val;

    RespCodeAliPayEnum(String code, String val){
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
        for (RespCodeAliPayEnum respCode : RespCodeAliPayEnum.values()){
            if(code.equals(respCode.code)){
                return respCode.val;
            }
        }
        return "";
    }
}
