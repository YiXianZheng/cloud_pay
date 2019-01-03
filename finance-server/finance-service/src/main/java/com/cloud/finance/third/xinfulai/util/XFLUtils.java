package com.cloud.finance.third.xinfulai.util;

import com.cloud.finance.third.xinfulai.service.XinfulaiPayService;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * <p>
 * 功能说明：
 * </p>
 * <ul>
 * <li></li>
 * </ul>
 * <p>
 * Created by Chen,Wenbi 2015/6/26.
 * </p>
 * <p>
 * Email Address: <a href=“chenwb@lianlian.com.cn”>chenwb@lianlian.com.cn</a>
 * </p>
 */
public class XFLUtils {

    private static Logger logger = LoggerFactory.getLogger(XinfulaiPayService.class);
    public static String getKeyedDigest(String strSrc, String key) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(strSrc.getBytes("UTF-8"));
            
            String result="";
            byte[] temp;
            temp=md5.digest(key.getBytes("UTF-8"));
            for (int i=0; i<temp.length; i++){
                result+=Integer.toHexString((0x000000ff & temp[i]) | 0xffffff00).substring(6);
            }
            
            return result;
            
        } catch (NoSuchAlgorithmException e) {
            
            e.printStackTrace();
            
        }catch(Exception e)
        {
          e.printStackTrace();
        }
        return null;
    }
    
    public static String getSignParam(Map<String,String> params){
        StringBuilder buf = new StringBuilder((params.size() +1) * 10);
//        buildPayParams(params,false);
        String result = buf.toString();    
        return result;
    }

    public static String encodeBASE64(String value) {
        return encodeBASE64(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String encodeBASE64(byte[] value) {
        return Base64.encodeBase64String(value).replace("\n","");
    }

    public static String signMap(Map<String, String> item, String secretKey) {
        if (item == null || "".equals(secretKey)) return "";
        SortedMap<String, String> map = new TreeMap<>(item);
        List<String> queryList = new ArrayList<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            if (entry.getValue() == null || "signature".equals(key) || "signMethod".equals(key)) continue;
            queryList.add(key + "=" + entry.getValue().toString());
        }
        String uri = StringUtils.join(queryList.iterator(), "&");
        uri = uri + secretKey;
        logger.info("xfl channel pay sign before: " + uri);
        return encodeBASE64(hexMD5(uri));
    }

    private static String getParams(Map<String, String> paramsMap) {
        String result = "";
        for (HashMap.Entry<String, String> entity : paramsMap.entrySet()) {
            result += "&" + entity.getKey() + "=" + entity.getValue();
        }
        return result.substring(1);
    }

    private static byte[] hexMD5(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(value.getBytes("utf-8"));
            byte[] digest = messageDigest.digest();
            return digest;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String urlEncode(String str){
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Throwable e) {
            return str;
        } 
    }

    /**
     * 将指定的字符串用MD5加密 originstr 需要加密的字符串
     * 
     * @param originstr
     * @return
     */

    public static String ecodeByMD5(String originstr)
    {

        String result = null;

        char hexDigits[] =
        {// 用来将字节转换成 16 进制表示的字符

        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

        if (originstr != null)
        {

            try
            {

                // 返回实现指定摘要算法的 MessageDigest 对象

                MessageDigest md = MessageDigest.getInstance("MD5");

                // 使用utf-8编码将originstr字符串编码并保存到source字节数组

                byte[] source = originstr.getBytes("utf-8");

                // 使用指定的 byte 数组更新摘要

                md.update(source);

                // 通过执行诸如填充之类的最终操作完成哈希计算，结果是一个128位的长整数

                byte[] tmp = md.digest();

                // 用16进制数表示需要32位

                char[] str = new char[32];

                for (int i = 0, j = 0; i < 16; i++)
                {

                    // j表示转换结果中对应的字符位置

                    // 从第一个字节开始，对 MD5 的每一个字节

                    // 转换成 16 进制字符

                    byte b = tmp[i];

                    // 取字节中高 4 位的数字转换

                    // 无符号右移运算符>>> ，它总是在左边补0

                    // 0x代表它后面的是十六进制的数字. f转换成十进制就是15

                    str[j++] = hexDigits[b >>> 4 & 0xf];

                    // 取字节中低 4 位的数字转换

                    str[j++] = hexDigits[b & 0xf];

                }

                result = new String(str);// 结果转换成字符串用于返回

            }
            catch (NoSuchAlgorithmException e)
            {
                e.printStackTrace();
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 创建密匙
     * 
     * @param algorithm
     *            加密算法,可用 DES,DESede,Blowfish
     * @return SecretKey 秘密（对称）密钥
     */
    public static SecretKey createSecretKey(String algorithm)
    {
        // 声明KeyGenerator对象
        KeyGenerator keygen;
        // 声明 密钥对象
        SecretKey deskey = null;
        try
        {
            // 返回生成指定算法的秘密密钥的 KeyGenerator 对象
            keygen = KeyGenerator.getInstance(algorithm);
            // 生成一个密钥
            deskey = keygen.generateKey();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        // 返回密匙
        return deskey;
    }
}
