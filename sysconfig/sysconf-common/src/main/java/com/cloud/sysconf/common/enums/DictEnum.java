package com.cloud.sysconf.common.enums;

import lombok.Getter;

/**
 * 系统字典表枚举类
 * @Auther Toney
 * @Date 2018/8/8 16:17
 * @Description:
 */
@Getter
public enum DictEnum {

    DAILY_LIMIT("DAILY_LIMIT", "商户默认每日限额"),
    COMMISSION_CHARGE_FIXED("COMMISSION_CHARGE_FIXED", "固定代付手续费的默认值"),
    COMMISSION_CHARGE_PERCENT("COMMISSION_CHARGE_PERCENT", "百分比代付续费的默认值"),
    CASH_MAX("CASH_MAX", "系统最高代付金额"),
    CASH_MIN("CASH_MIN", "系统最低代付金额"),
    NOTIFY_BASE_URL("NOTIFY_BASE_URL", "系统支付回调服务跟地址"),
    NOTIFY_TIMES("NOTIFY_TIMES", "商户异步通知的频率"),
    PAY_BASE_URL("PAY_BASE_URL", "系统支付服务根地址"),
    PAY_MAX("PAY_MAX", "系统最高支付金额"),
    PAY_MIN("PAY_MIN", "系统最低支付金额"),
    CASH_BEGIN_TIME("CASH_BEGIN_TIME", "代付开始时间"),
    CASH_END_TIME("CASH_END_TIME", "代付结束时间")

            ;

    private String code;

    private String value;

    DictEnum(String code, String value){
        this.code = code;
        this.value = value;
    }

}
