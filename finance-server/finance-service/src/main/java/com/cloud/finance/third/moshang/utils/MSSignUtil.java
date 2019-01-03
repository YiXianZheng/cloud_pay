package com.cloud.finance.third.moshang.utils;

public class MSSignUtil {
    public static String aiyangpayBankMd5Sign(String merchantid,String type,String value,
                                              String orderid,String callbackurl,String md5key){
        StringBuffer sendsb = new StringBuffer();
        sendsb.append("merchantid="+merchantid);
        sendsb.append("&type="+type);
        sendsb.append("&value="+value);
        sendsb.append("&orderid="+orderid);
        sendsb.append("&callbackurl="+callbackurl);
        return MD5.MD5Encode(sendsb + md5key);
    }
    public static String obaopayBankMd5Sign(String merchantid,String orderid,String opstate,
                                            String ovalue,String sysorderid,String md5key){
        StringBuffer sendsb = new StringBuffer();
        sendsb.append("merchantid="+merchantid);
        sendsb.append("&orderid="+orderid);
        sendsb.append("&opstate="+opstate);
        sendsb.append("&ovalue="+ovalue);
        sendsb.append("&sysorderid="+sysorderid);
        return MD5.MD5Encode(sendsb + md5key);
    }
}
