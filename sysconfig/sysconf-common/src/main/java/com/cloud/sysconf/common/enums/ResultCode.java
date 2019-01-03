package com.cloud.sysconf.common.enums;

import lombok.Data;
import lombok.Getter;

/**
 * @Auther Toney
 * @Date 2018/7/13 17:34
 * @Description:
 */
@Getter
public enum ResultCode {

    REQUIRE_ERR("101", "请求异常")
    ;

    private String code;

    private String value;

    ResultCode(String code, String value){
        this.code = code;
        this.value = value;
    }

}
