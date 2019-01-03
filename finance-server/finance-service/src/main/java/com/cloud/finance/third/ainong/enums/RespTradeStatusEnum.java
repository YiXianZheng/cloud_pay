package com.cloud.finance.third.ainong.enums;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

@Getter
public enum RespTradeStatusEnum {
    CODE_UNPAY("0", "未支付"),
    CODE_SUCCESS("1", "成功"),
    CODE_PAYING("2", "支付中"),
    CODE_REFUND("3", "退款"),
    CODE_ERROR("4", "失败"),
    ;

    private String code;

    private String val;

    RespTradeStatusEnum(String code, String val){
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
        for (RespTradeStatusEnum respCode : RespTradeStatusEnum.values()){
            if(code.equals(respCode.code)){
                return respCode.val;
            }
        }
        return "";
    }
}
