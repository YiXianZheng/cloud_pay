package com.cloud.finance.common.utils;

import com.cloud.sysconf.common.utils.MapToXMLString;
import com.cloud.sysconf.common.utils.StringUtil;

import java.util.*;

public class ASCIISortUtil {

    /**
     * 签名字符串
     * @param map   参数
     * @param union key val 连接符
     * @param key
     * @return
     */
    public static String buildSign(Map<String, String> map,String union, String key) {

        List<Map.Entry<String, String>> items = new ArrayList<Map.Entry<String, String>>(map.entrySet());
        // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
        Collections.sort(items, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return (o1.getKey()).compareTo(o2.getKey());
            }
        });

        StringBuilder sb = new StringBuilder();
        int flg = 0;
        //取出排序后的参数，逐一连接起来
        for (Map.Entry<String, String> item : items) {
            if (StringUtil.isNotEmpty(item.getValue())) {
                sb.append(item.getKey());
                sb.append(union);
                sb.append(item.getValue());
                if(flg < items.size()-1)
                    sb.append("&");
            }

            flg ++;
        }
        char last = sb.charAt(sb.length() - 1);
        if (last == '&') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(key);

        //返回最终排序后的结果，这里key不参与排序中，具体看接口规约
        return sb.toString();
    }

    /**
     * 签名字符串  返回XML
     * @param map   参数
     * @return
     */
    public static String buildXmlSign(Map<String, String> map) {

        List<Map.Entry<String, String>> items = new ArrayList<Map.Entry<String, String>>(map.entrySet());
        // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）
        Collections.sort(items, new Comparator<Map.Entry<String, String>>() {
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });

        Map<Object, Object> resMap = new HashMap<>();
        for (Map.Entry<String, String> item : items) {
            resMap.put(item.getKey(), item.getValue());
        }
        return MapToXMLString.converter(resMap);
    }
}
