package com.cloud.finance.third.hankou.utils;

import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.utils.finance.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class HKUtil {
    private static Logger logger = LoggerFactory.getLogger(HKUtil.class);

    public static String getRandomString(int length){
        //1.  定义一个字符串（A-Z，a-z，0-9）即62个数字字母；
        String str = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
        //2.  由Random生成随机数
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        //3.  长度为几就循环几次
        for(int i = 0; i < length; ++i){
            //从62个的数字或字母中选择
            int number = random.nextInt(62);
            //将产生的数字通过length次承载到sb中
            sb.append(str.charAt(number));
        }
        //将承载的字符转换成字符串
        return sb.toString();
    }

    /**
     * 对数据进行ASCII码排序并进行md5加密
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static String generateMd5Sign(final Map<String, String> data, String key) throws Exception {

        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);//集合中的key排序

        StringBuilder sb = new StringBuilder();

        for (String aKeyArray : keyArray) {
            if (StringUtil.isNotEmpty(aKeyArray))
                sb.append(aKeyArray).append("=").append(data.get(aKeyArray).trim()).append("&");
        }
        sb.append("key=").append(key);
        logger.info("sign str: " + String.valueOf(sb));
        return MD5.MD5Encode(String.valueOf(sb)).toUpperCase();
    }
}
