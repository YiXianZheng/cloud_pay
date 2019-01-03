package com.cloud.finance.third.ainong.utils;

public class HexUtil {
    public HexUtil() {
    }

    public static byte[] hexstr2Bytes(String hexstr) {
        String upper = hexstr.toUpperCase();
        int length = upper.length() / 2;
        byte[] ret = new byte[length];

        for(int i = 0; i < length; ++i) {
            byte high = (byte)"0123456789ABCDEF".indexOf(upper.charAt(2 * i));
            byte low = (byte)"0123456789ABCDEF".indexOf(upper.charAt(2 * i + 1));
            ret[i] = (byte)((high << 4) + low);
        }

        return ret;
    }

    public static String bytes2Hexstr(byte[] bytes) {
        String ret = "";

        for(int i = 0; i < bytes.length; ++i) {
            String hex = Integer.toHexString(bytes[i] & 255);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }

            ret = ret + hex.toUpperCase();
        }

        return ret;
    }
}