package com.cloud.finance.third.xinbao.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.TreeMap;

public class SignUtil {

    private static Logger logger = LoggerFactory.getLogger(SignUtil.class);

    /**
     * 生成待签字符串
     *
     * @param data 原始map数据
     * @return 待签字符串
     */
    public static String buildWaitingForSign(Map<String, String> data) {
        if (data == null || data.size() == 0) {
            throw new IllegalArgumentException("请求数据不能为空");
        }
        String waitingForSign = null;
        Map<String, String> sortedMap = new TreeMap<>(data);
        // 如果sign参数存在,去除sign参数,不参与签名
        if (sortedMap.containsKey("sign")) {
            sortedMap.remove("sign");
        }
        StringBuilder stringToSign = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
            if (entry.getValue() != null) {
                stringToSign.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }
        stringToSign.deleteCharAt(stringToSign.length() - 1);
        waitingForSign = stringToSign.toString();
        logger.debug("代签名字符串:{}", waitingForSign);
        return waitingForSign;
    }

    /**
     * MD5摘要签名
     *
     * @param waitToSignStr
     * @param key
     * @return
     */
    public static String signMD5(String waitToSignStr, String key) {
        // MD5摘要签名计算
        String signature = DigestUtils.md5Hex(waitToSignStr + key);
        return signature;
    }

    /**
     * 验证MD5签名
     *
     * @param waitToSignStr 待签字符串
     * @param key           商户安全码
     * @param verifySign    待验证签名
     * @return 验签结果。 true: 成功, false: 失败
     */
    public static boolean verifyMD5(String waitToSignStr, String key, String verifySign) {
        // MD5摘要签名计算
        String signature = DigestUtils.md5Hex(waitToSignStr + key);
        return verifySign.equals(signature);
    }
}
