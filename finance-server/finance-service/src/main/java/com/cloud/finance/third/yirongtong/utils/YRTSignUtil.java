package com.cloud.finance.third.yirongtong.utils;

import com.cloud.finance.third.moshang.utils.MD5;

public class YRTSignUtil {

    public static String callbackMd5Sign(String partner, String orderNumber, String orderStatus, String payMoney, String md5Key) {

        StringBuffer sb = new StringBuffer();
        sb.append("partner=").append(partner)
                .append("&ordernumber=").append(orderNumber)
                .append("&orderstatus=").append(orderStatus)
                .append("&paymoney=").append(payMoney)
                .append(md5Key);

        return MD5.MD5Encode(String.valueOf(sb));
    }
}
