package com.cloud.finance.third.jinxin.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    public static String md5(String str) throws NoSuchAlgorithmException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] byteDigest = md.digest();
            int i;
            //字符数组转换成字符串
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < byteDigest.length; offset++) {
                i = byteDigest[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            // 32位加密
            return buf.toString().toUpperCase();
            // 16位的加密
            //return buf.toString().substring(8, 24).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
