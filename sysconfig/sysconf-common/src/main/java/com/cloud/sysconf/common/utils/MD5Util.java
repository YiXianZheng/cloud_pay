package com.cloud.sysconf.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

public class MD5Util {
    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is unsupported", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MessageDigest不支持MD5Util", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }


    /**
     * md5签名
     *
     * 按参数名称升序，将参数值进行连接 签名
     * @param params
     * @return
     */
    public static String sign(TreeMap<String, String> params,String securityKey) {
        StringBuilder paramValues = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            paramValues.append("&");
            paramValues.append(entry.getKey());
            paramValues.append("=");
            paramValues.append(entry.getValue());
        }
        paramValues.append(securityKey);
        System.out.println(md5(paramValues.toString()));
        System.out.println("----------"+paramValues.toString());
        return md5(paramValues.toString());
    }



}
