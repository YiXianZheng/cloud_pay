package com.cloud.finance.third.smzf.utils;

import cfca.sadk.org.bouncycastle.asn1.ASN1Sequence;
import cfca.sadk.org.bouncycastle.asn1.pkcs.RSAPrivateKeyStructure;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.core.io.ClassPathResource;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.*;
import java.util.*;


public class SMUtil {

    public static String signByPri(String context, String filePath) {

        ClassPathResource resource = new ClassPathResource(filePath);
        String regex = "-.*";
        try {
            InputStream certStream = resource.getInputStream();
            byte[] privateKeyContent = IOUtils.toByteArray(certStream);
            certStream.read(privateKeyContent);
            certStream.close();

            String privateKeyStr = new String(privateKeyContent)
                    .replaceAll(regex, "");

            privateKeyContent =
                    Base64.getDecoder().decode(privateKeyStr.replace("\n", ""));

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyContent);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            java.security.Signature signature =
                    java.security.Signature.getInstance("SHA1WithRSA");

            signature.initSign(privateKey);
            signature.update(context.getBytes(Charset.forName("UTF8")));

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String signByPub(String context) {

        String filePath = "cert/sm/pay_pub_key.pem";
        ClassPathResource resource = new ClassPathResource(filePath);
        try {
            InputStream certStream = resource.getInputStream();
            byte[] publicKeyContent = IOUtils.toByteArray(certStream);
            certStream.read(publicKeyContent);
            certStream.close();

            String publicKeyStr = new String(publicKeyContent)
                    .replaceAll("-.*", "");

            byte[] buffer = Base64.getDecoder().decode(publicKeyStr.replace("\n", ""));

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);


            java.security.Signature signature = java.security.Signature.getInstance("SHA1WithRSA");
            signature.initVerify(publicKey);
            signature.update(context.getBytes(Charset.forName("UTF8")));

            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * RSA加密
     *
     * @param plainBytes      明文字节数组
     * @param publicKey       公钥
     * @param keyLength       密钥bit长度
     * @param reserveSize     padding填充字节数，预留11字节
     * @param cipherAlgorithm 加解密算法，一般为RSA/ECB/PKCS1Padding
     * @return 加密后字节数组，不经base64编码
     * @throws Exception
     */
    public static byte[] RSAEncrypt(byte[] plainBytes, PublicKey publicKey, int keyLength, int reserveSize, String cipherAlgorithm)
            throws Exception {
        int keyByteSize = keyLength / 8; // 密钥字节数
        int encryptBlockSize = keyByteSize - reserveSize; // 加密块大小=密钥字节数-padding填充字节数
        int nBlock = plainBytes.length / encryptBlockSize;// 计算分段加密的block数，向上取整
        if ((plainBytes.length % encryptBlockSize) != 0) { // 余数非0，block数再加1
            nBlock += 1;
        }

        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            // 输出buffer，大小为nBlock个keyByteSize
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * keyByteSize);
            // 分段加密
            for (int offset = 0; offset < plainBytes.length; offset += encryptBlockSize) {
                int inputLen = plainBytes.length - offset;
                if (inputLen > encryptBlockSize) {
                    inputLen = encryptBlockSize;
                }

                // 得到分段加密结果
                byte[] encryptedBlock = cipher.doFinal(plainBytes, offset, inputLen);
                // 追加结果到输出buffer中
                outbuf.write(encryptedBlock);
            }

            outbuf.flush();
            outbuf.close();
            return outbuf.toByteArray();
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(String.format("没有[%s]此类加密算法", cipherAlgorithm));
        } catch (NoSuchPaddingException e) {
            throw new Exception(String.format("没有[%s]此类填充模式", cipherAlgorithm));
        } catch (InvalidKeyException e) {
            throw new Exception("无效密钥");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("加密块大小不合法");
        } catch (BadPaddingException e) {
            throw new Exception("错误填充模式");
        } catch (IOException e) {
            throw new Exception("字节输出流异常");
        }
    }

    /**
     * 获取RSA公钥对象
     *
     * @return RSA公钥对象
     * @throws Exception
     */
    public static PublicKey getRSAPublicKeyByFileSuffix() throws Exception {

        // 公钥路径
        String filePath = "cert/sm/pay_pub_key.pem";
        // 密钥算法
        String keyAlgorithm = "RSA";
        ClassPathResource resource = new ClassPathResource(filePath);
        InputStream in = null;

        try {
            in = resource.getInputStream();
            PublicKey pubKey;

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String readLine = null;
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                } else {
                    sb.append(readLine);
                    sb.append('\r');
                }
            }

            X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(org.apache.commons.codec.binary.Base64.decodeBase64(sb.toString()));
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            pubKey = keyFactory.generatePublic(pubX509);

            return pubKey;
        } catch (FileNotFoundException e) {
            throw new Exception("公钥路径文件不存在");
        } catch (IOException e) {
            throw new Exception("读取公钥异常");
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(String.format("生成密钥工厂时没有[%s]此类算法", keyAlgorithm));
        } catch (InvalidKeySpecException e) {
            throw new Exception("生成公钥对象异常");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * AES加密
     *
     * @param plainBytes
     *            明文字节数组
     * @param keyBytes
     *            密钥字节数组
     * @param keyAlgorithm
     *            密钥算法
     * @param cipherAlgorithm
     *            加解密算法
     * @param IV
     *            随机向量
     * @return 加密后字节数组，不经base64编码
     * @throws Exception
     */
    public static byte[] AESEncrypt(byte[] plainBytes, byte[] keyBytes, String keyAlgorithm, String cipherAlgorithm, String IV)
            throws Exception {
        try {
            // AES密钥长度为128bit、192bit、256bit，默认为128bit
            if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
                throw new Exception("AES密钥长度不合法");
            }

            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
            if (StringUtils.trimToNull(IV) != null) {
                IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            }

            byte[] encryptedBytes = cipher.doFinal(plainBytes);

            return encryptedBytes;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(String.format("没有[%s]此类加密算法", cipherAlgorithm));
        } catch (NoSuchPaddingException e) {
            throw new Exception(String.format("没有[%s]此类填充模式", cipherAlgorithm));
        } catch (InvalidKeyException e) {
            throw new Exception("无效密钥");
        } catch (InvalidAlgorithmParameterException e) {
            throw new Exception("无效密钥参数");
        } catch (BadPaddingException e) {
            throw new Exception("错误填充模式");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("加密块大小不合法");
        }
    }

    /**
     * 获取RSA私钥对象
     *
     * @param fileSuffix
     *            RSA私钥名称，决定编码类型
     * @param password
     *            RSA私钥保护密钥
     * @param keyAlgorithm
     *            密钥算法
     * @return RSA私钥对象
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
    public static PrivateKey getRSAPrivateKeyByFileSuffix(String fileSuffix, String password, String keyAlgorithm)
            throws Exception {
        String keyType = "";
        if ("keystore".equalsIgnoreCase(fileSuffix)) {
            keyType = "JKS";
        } else if ("pfx".equalsIgnoreCase(fileSuffix) || "p12".equalsIgnoreCase(fileSuffix)) {
            keyType = "PKCS12";
        } else if ("jck".equalsIgnoreCase(fileSuffix)) {
            keyType = "JCEKS";
        } else if ("pem".equalsIgnoreCase(fileSuffix) || "pkcs8".equalsIgnoreCase(fileSuffix)) {
            keyType = "PKCS8";
        } else if ("pkcs1".equalsIgnoreCase(fileSuffix)) {
            keyType = "PKCS1";
        } else if ("yljf".equalsIgnoreCase(fileSuffix)) {
            keyType = "yljf";
        } else if ("ldys".equalsIgnoreCase(fileSuffix)) {
            keyType = "ldys";
        } else{
            keyType = "JKS";
        }
        String filePath = "cert/sm/pay_prv_key.pem";
        ClassPathResource resource = new ClassPathResource(filePath);
        InputStream in = null;
        try {
            in = resource.getInputStream();
            PrivateKey priKey = null;
            if ("JKS".equals(keyType) || "PKCS12".equals(keyType) || "JCEKS".equals(keyType)) {
                KeyStore ks = KeyStore.getInstance(keyType);
                if (password != null) {
                    char[] cPasswd = password.toCharArray();
                    ks.load(in, cPasswd);
                    Enumeration<String> aliasenum = ks.aliases();
                    String keyAlias = null;
                    while (aliasenum.hasMoreElements()) {
                        keyAlias = (String) aliasenum.nextElement();
                        priKey = (PrivateKey) ks.getKey(keyAlias, cPasswd);
                        if (priKey != null)
                            break;
                    }
                }
            }else if("yljf".equals(keyType)){
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String s = br.readLine();
                PKCS8EncodedKeySpec priPKCS8=new PKCS8EncodedKeySpec(hexStrToBytes(s));
                KeyFactory keyf=KeyFactory.getInstance("RSA");
                PrivateKey myprikey=keyf.generatePrivate(priPKCS8);
                return myprikey;
            }else if("ldys".equals(keyType)){
                byte[] b = new byte[20480];
                in.read(b);
                PKCS8EncodedKeySpec priPKCS8=new PKCS8EncodedKeySpec(b);
                KeyFactory keyf=KeyFactory.getInstance("RSA");
                PrivateKey myprikey=keyf.generatePrivate(priPKCS8);
                return myprikey;
            }else {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String readLine = null;
                while ((readLine = br.readLine()) != null) {
                    if (readLine.charAt(0) == '-') {
                        continue;
                    } else {
                        sb.append(readLine);
                        sb.append('\r');
                    }
                }
                if ("PKCS8".equals(keyType)) {
                    PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(org.apache.commons.codec.binary.Base64.decodeBase64(sb.toString()));
                    KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
                    priKey = keyFactory.generatePrivate(priPKCS8);
                } else if ("PKCS1".equals(keyType)) {
//					RSAPrivateKeyStructure asn1PrivKey = new RSAPrivateKeyStructure((ASN1Sequence) ASN1Sequence.fromByteArray(sb.toString().getBytes()));
                    RSAPrivateKeyStructure asn1PrivKey = new RSAPrivateKeyStructure((ASN1Sequence) ASN1Sequence.fromByteArray(org.apache.commons.codec.binary.Base64.decodeBase64(sb.toString())));
                    KeySpec rsaPrivKeySpec = new RSAPrivateKeySpec(asn1PrivKey.getModulus(), asn1PrivKey.getPrivateExponent());
                    KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
                    priKey = keyFactory.generatePrivate(rsaPrivKeySpec);
                }
            }

            return priKey;
        } catch (FileNotFoundException e) {
            throw new Exception("私钥路径文件不存在");
        } catch (KeyStoreException e) {
            throw new Exception("获取KeyStore对象异常");
        } catch (IOException e) {
            throw new Exception("读取私钥异常");
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("生成私钥对象异常");
        } catch (CertificateException e) {
            throw new Exception("加载私钥密码异常");
        } catch (UnrecoverableKeyException e) {
            throw new Exception("生成私钥对象异常");
        } catch (InvalidKeySpecException e) {
            throw new Exception("生成私钥对象异常");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
            }
        }
    }

    private static final byte[] hexStrToBytes(String s) {
        byte[] bytes;
        bytes = new byte[s.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * 		i + 2), 16);
        }
        return bytes;
    }

    /**
     * RSA解密
     *
     * @param encryptedBytes
     *            加密后字节数组
     * @param privateKey
     *            私钥
     * @param keyLength
     *            密钥bit长度
     * @param reserveSize
     *            padding填充字节数，预留11字节
     * @param cipherAlgorithm
     *            加解密算法，一般为RSA/ECB/PKCS1Padding
     * @return 解密后字节数组，不经base64编码
     * @throws Exception
     */
    public static byte[] RSADecrypt(byte[] encryptedBytes, PrivateKey privateKey, int keyLength, int reserveSize, String cipherAlgorithm)
            throws Exception {
        int keyByteSize = keyLength / 8; // 密钥字节数
        int decryptBlockSize = keyByteSize - reserveSize; // 解密块大小=密钥字节数-padding填充字节数
        int nBlock = encryptedBytes.length / keyByteSize;// 计算分段解密的block数，理论上能整除

        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            // 输出buffer，大小为nBlock个decryptBlockSize
            ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * decryptBlockSize);
            // 分段解密
            for (int offset = 0; offset < encryptedBytes.length; offset += keyByteSize) {
                // block大小: decryptBlock 或 剩余字节数
                int inputLen = encryptedBytes.length - offset;
                if (inputLen > keyByteSize) {
                    inputLen = keyByteSize;
                }

                // 得到分段解密结果
                byte[] decryptedBlock = cipher.doFinal(encryptedBytes, offset, inputLen);
                // 追加结果到输出buffer中
                outbuf.write(decryptedBlock);
            }

            outbuf.flush();
            outbuf.close();
            return outbuf.toByteArray();
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(String.format("没有[%s]此类解密算法", cipherAlgorithm));
        } catch (NoSuchPaddingException e) {
            throw new Exception(String.format("没有[%s]此类填充模式", cipherAlgorithm));
        } catch (InvalidKeyException e) {
            throw new Exception("无效密钥");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("解密块大小不合法");
        } catch (BadPaddingException e) {
            throw new Exception("错误填充模式");
        } catch (IOException e) {
            throw new Exception("字节输出流异常");
        }
    }

    /**
     * AES解密
     *
     * @param encryptedBytes
     *            密文字节数组，不经base64编码
     * @param keyBytes
     *            密钥字节数组
     * @param keyAlgorithm
     *            密钥算法
     * @param cipherAlgorithm
     *            加解密算法
     * @param IV
     *            随机向量
     * @return 解密后字节数组
     * @throws Exception
     */
    public static byte[] AESDecrypt(byte[] encryptedBytes, byte[] keyBytes, String keyAlgorithm, String cipherAlgorithm, String IV)
            throws Exception {
        try {
            // AES密钥长度为128bit、192bit、256bit，默认为128bit
            if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
                throw new Exception("AES密钥长度不合法");
            }

            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
            if (IV != null && StringUtils.trimToNull(IV) != null) {
                IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
                cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return decryptedBytes;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(String.format("没有[%s]此类加密算法", cipherAlgorithm));
        } catch (NoSuchPaddingException e) {
            throw new Exception(String.format("没有[%s]此类填充模式", cipherAlgorithm));
        } catch (InvalidKeyException e) {
            throw new Exception("无效密钥");
        } catch (InvalidAlgorithmParameterException e) {
            throw new Exception("无效密钥参数");
        } catch (BadPaddingException e) {
            throw new Exception("错误填充模式");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("解密块大小不合法");
        }
    }

    /**
     * 验证数字签名函数入口
     *
     * @param plainBytes
     *            待验签明文字节数组
     * @param signBytes
     *            待验签签名后字节数组
     * @param publicKey
     *            验签使用公钥
     * @param signAlgorithm
     *            签名算法
     * @return 验签是否通过
     * @throws Exception
     */
    public static boolean verifyDigitalSign(byte[] plainBytes, byte[] signBytes, PublicKey publicKey, String signAlgorithm) throws Exception {
        boolean isValid = false;
        try {
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initVerify(publicKey);
            signature.update(plainBytes);
            isValid = signature.verify(signBytes);
            return isValid;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(String.format("验证数字签名时没有[%s]此类算法", signAlgorithm));
        } catch (InvalidKeyException e) {
            throw new Exception("验证数字签名时公钥无效");
        } catch (SignatureException e) {
            throw new Exception("验证数字签名时出现异常");
        }
    }

    /**
     * 数字签名函数入口
     *
     * @param plainBytes
     *            待签名明文字节数组
     * @param privateKey
     *            签名使用私钥
     * @param signAlgorithm
     *            签名算法
     * @return 签名后的字节数组
     * @throws Exception
     */
    public static byte[] digitalSign(byte[] plainBytes, PrivateKey privateKey, String signAlgorithm) throws Exception {
        try {
            Signature signature = Signature.getInstance(signAlgorithm);
            signature.initSign(privateKey);
            signature.update(plainBytes);
            byte[] signBytes = signature.sign();

            return signBytes;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception(String.format("数字签名时没有[%s]此类算法", signAlgorithm));
        } catch (InvalidKeyException e) {
            throw new Exception("数字签名时私钥无效");
        } catch (SignatureException e) {
            throw new Exception("数字签名时出现异常");
        }
    }

    /**
     * @description 将xml字符串转换成map
     * @param xml
     *          xml字符串
     * @return Map
     */
    public static Map<String, String> readStringXmlOut(String xml) {
        Map<String, String> map = new HashMap<>();
        Document doc;
        try {
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            Iterator iter = rootElt.elementIterator("head"); // 获取根节点下的子节点head
            // 遍历子节点
            while (iter.hasNext()) {
                Element recordEle = (Element) iter.next();
                String respType = recordEle.elementTextTrim("respType"); // 拿到head节点下的子节点title值
                String respCode = recordEle.elementTextTrim("respCode"); // 拿到head节点下的子节点title值
                String respMsg = recordEle.elementTextTrim("respMsg"); // 拿到head节点下的子节点title值
                map.put("respType", respType);
                map.put("respCode", respCode);
                map.put("respMsg", respMsg);
            }
            /*Iterator iterss = rootElt.elementIterator("body"); ///获取根节点下的子节点body
            // 遍历body节点
            while (iterss.hasNext()) {
                Element recordEless = (Element) iterss.next();
                String result = recordEless.elementTextTrim("result"); // 拿到body节点下的子节点result值
                System.out.println("result:" + result);
                Iterator itersElIterator = recordEless.elementIterator("form"); // 获取子节点body下的子节点form
                // 遍历Header节点下的Response节点
                while (itersElIterator.hasNext()) {
                    Element itemEle = (Element) itersElIterator.next();
                    String banlce = itemEle.elementTextTrim("banlce"); // 拿到body下的子节点form下的字节点banlce的值
                    String subID = itemEle.elementTextTrim("subID");

                    map.put("result", result);
                    map.put("banlce", banlce);
                    map.put("subID", subID);
                }
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
