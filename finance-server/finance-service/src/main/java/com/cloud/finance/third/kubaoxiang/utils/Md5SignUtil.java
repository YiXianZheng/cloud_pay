package com.cloud.finance.third.kubaoxiang.utils;

import com.cloud.sysconf.common.utils.finance.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Md5SignUtil {
    private static Logger logger = LoggerFactory.getLogger(Md5SignUtil.class);

    /**
     * 对数据进行ASCII码排序并进行md5加密
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static String generateMd5Sign(final Map<String, String> data, String key) throws Exception {

        data.put("key", key);
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);//集合中的key排序

        StringBuilder sb = new StringBuilder();

        for (String aKeyArray : keyArray) {
            sb.append(aKeyArray).append("=").append(data.get(aKeyArray).trim()).append( "&" );
        }
        sb = sb.deleteCharAt(sb.length() - 1);
        logger.info("sign str: " + String.valueOf(sb));
        return MD5.MD5Encode(String.valueOf(sb));
    }
}
