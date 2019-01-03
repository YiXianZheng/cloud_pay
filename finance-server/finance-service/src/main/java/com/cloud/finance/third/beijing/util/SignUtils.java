package com.cloud.finance.third.beijing.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class SignUtils {

    private static Logger logger = LoggerFactory.getLogger(SignUtils.class);
    public static String buildForSign(final Map<String, String> data, String key) {

        data.put("key", key);
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);//集合中的key排序

        StringBuilder sb = new StringBuilder();

        for (String aKeyArray : keyArray) {
            if (data.get(aKeyArray).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(aKeyArray).append("=").append(data.get(aKeyArray).trim()).append( "&" );
        }
        sb = sb.deleteCharAt(sb.length() - 1);
        logger.info("sign str: " + String.valueOf(sb));
//        return Md5SignUtil.getMD5(String.valueOf(sb));
        return "";
    }
}
