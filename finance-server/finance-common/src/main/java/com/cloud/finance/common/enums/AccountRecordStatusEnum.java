package com.cloud.finance.common.enums;


import lombok.Getter;

/**
 * 账户账变状态枚举类
 */
@Getter
public enum AccountRecordStatusEnum {

    ACCOUNT_RECORD_STATUS_DOING(1, "账变处理中"),
    ACCOUNT_RECORD_STATUS_SUCCESS(2, "账变完成"),
    ACCOUNT_RECORD_STATUS_FAIL(3, "账变失败"),
    ;

    private Integer code;
    private String value;

    AccountRecordStatusEnum(Integer code, String value){
        this.code = code;
        this.value = value;
    }

    public static String getByCode(Integer code){
        if(code == null) return "";
        for (AccountRecordStatusEnum recordType : AccountRecordStatusEnum.values()){
            if(code == recordType.getCode()){
                return recordType.value;
            }
        }
        return "";
    }
}
