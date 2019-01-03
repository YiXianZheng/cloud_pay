package com.cloud.finance.third.ainong.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    private static ThreadLocal threadLocal = new ThreadLocal() {
        protected synchronized Object initialValue() {
            MessageDigest messagedigest = null;

            try {
                messagedigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException var3) {
                System.out.println("初始化失败，MessageDigest不支持MD5Util," + var3);
            }

            return messagedigest;
        }
    };


    public static MessageDigest getMessageDigest() {
        return (MessageDigest) threadLocal.get();
    }

    public static String digest(String s, String charset) {
        try {
            getMessageDigest().update(s.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            System.out.println("交互解密异常" + e);
            throw new RuntimeException(e);
        }
        return HexUtil.bytes2Hexstr(getMessageDigest().digest());
    }


    //md5加密

    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5','6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public static String MD5Encode(String sourceString) {
        String resultString = null;
        try {
            resultString = new String(sourceString);

            MessageDigest md = MessageDigest.getInstance("MD5");

            resultString = getFormattedText(md.digest(resultString.getBytes()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultString.toUpperCase();
    }



    private static String getFormattedText(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);
        for (int j = 0; j < len; j++) {
            buf.append(HEX_DIGITS[(bytes[j] >> 4) & 0x0f]);
            buf.append(HEX_DIGITS[bytes[j] & 0x0f]);
        }
        return buf.toString();
    }

}
