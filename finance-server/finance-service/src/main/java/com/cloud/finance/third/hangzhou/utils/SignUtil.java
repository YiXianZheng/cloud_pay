package com.cloud.finance.third.hangzhou.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class SignUtil {
    private static Logger logger = LoggerFactory.getLogger(SignUtil.class);

    /**
     * 生成 GJSignUtil
     * @param data 待处理数据
     * @param key 密钥
     * @return 加密结果
     * @throws Exception
     */
    private static String HMACSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] array = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 生成签名。
     *
     * @param data 待签名数据
     * @param key 公钥
     * @return 签名
     */
    public static String generateSignature(final Map<String, String> data, String key) throws Exception {
        Set<String> keySet = data.keySet();
        String[] keyArray = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keyArray);//集合中的key排序

        StringBuilder sb = new StringBuilder();

        for (String aKeyArray : keyArray) {
            if (data.get(aKeyArray).trim().length() > 0) // 参数值为空，则不参与签名
                sb.append(aKeyArray).append("=").append(data.get(aKeyArray).trim()).append("&");
        }

        sb.append("key=").append(key);
        logger.info("sign str: " + String.valueOf(sb));
        return HMACSHA256(sb.toString(), key);
    }

    /**
     * 判断签名是否正确，必须包含sign字段，否则返回false。
     * @param data Map类型数据
     * @param key API密钥
     * @return 签名是否正确
     * @throws Exception
     */
    public static boolean isSignatureValid(Map<String, String> data,String key) throws Exception {
        String sign = data.get("sign");
        data.remove("sign");
        boolean flag = generateSignature(data, key).equals(sign);
        data.put("sign", sign);
        return flag;
    }
}
