package com.cloud.finance.third.jinxin.utils;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class PayUtil {

    public static String generateOrderId(){
        String keyup_prefix=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String keyup_append=String.valueOf(new Random().nextInt(899999)+100000);
        String pay_orderid=keyup_prefix+keyup_append;//订单号
        return pay_orderid;
    }

    public static String generateTime(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }


    public static String getSignParam(Map<String,String> params){
        StringBuilder buf = new StringBuilder((params.size() +1) * 10);
        buildPayParams(buf,params,false);
        String result = buf.toString();
        return result;
    }

    /**
     * @author
     * @param payParams
     * @return
     */
    public static void buildPayParams(StringBuilder sb,Map<String, String> payParams,boolean encoding){
        List<String> keys = new ArrayList<String>(payParams.keySet());
        Collections.sort(keys);
        for(String key : keys){
            sb.append(key).append("=");
            if(encoding){
                try {
                    sb.append(URLEncoder.encode(payParams.get(key), "UTF-8"));
                } catch (Throwable e) {
                    sb.append(payParams.get(key));
                }
            }else{
                sb.append(payParams.get(key));
            }
            sb.append("&");
        }
        sb.setLength(sb.length() - 1);
    }
}
