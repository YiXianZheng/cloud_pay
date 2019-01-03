package com.cloud.finance.third.yirongtong.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

public class H5RequestData {

    public static String text(Map<String, String> map, String key) {
        StringBuffer sb = new StringBuffer();
        sb.append("version=").append(map.get("version"))
                .append("&method=").append(map.get("method"))
                .append("&partner=").append(map.get("partner"))
                .append("&banktype=").append(map.get("banktype"))
                .append("&paymoney=").append(map.get("paymoney"))
                .append("&ordernumber=").append(map.get("ordernumber"))
                .append("&callbackurl=").append(map.get("callbackurl"))
                .append(key);
        return sb.toString();
    }
}
