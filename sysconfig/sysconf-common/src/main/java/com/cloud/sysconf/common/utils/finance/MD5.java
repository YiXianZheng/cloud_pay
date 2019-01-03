package com.cloud.sysconf.common.utils.finance;

import java.security.MessageDigest;

public class MD5 {
	
private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
    
   
	public static String MD5Encode(String origin) throws Exception {
		MessageDigest md = MessageDigest.getInstance("MD5");
		return byteArrayToHexString(md.digest(origin.getBytes("utf-8")));
	}
	
	public static String MD5EncodeSigle(String origin) throws Exception {
		MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
        String newstr=base64en.encode(md5.digest(origin.getBytes("utf-8")));
        return newstr;
	}

   
    public static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (byte aB : b) {
            resultSb.append(byteToHexString(aB));
        }
        return resultSb.toString();
    }

    
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
	
}
