package com.cloud.finance.common.enums;


import lombok.Getter;

/**
 * 账户账变类型枚举类
 */
@Getter
public enum AccountRecordTypeEnum {

    ACCOUNT_RECORD_TYPE_PAY(1, "支付入账"),
    ACCOUNT_RECORD_TYPE_RECHARGE(2, "代付出账"),
    ACCOUNT_RECORD_TYPE_FROZEN(3, "风控冻结"),
    ACCOUNT_RECORD_TYPE_UNFROZEN(4, "解除风控冻结")
    ;

    private Integer code;
    private String value;

    AccountRecordTypeEnum(Integer code, String value){
        this.code = code;
        this.value = value;
    }

    public static String getByCode(Integer code){
        if(code == null) return "";
        for (AccountRecordTypeEnum recordType : AccountRecordTypeEnum.values()){
            if(code == recordType.getCode()){
                return recordType.value;
            }
        }
        return "";
    }
}
