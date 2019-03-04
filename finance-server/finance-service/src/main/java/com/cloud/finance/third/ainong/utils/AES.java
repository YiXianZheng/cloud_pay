/**
* @author cmt 
* @E-mail 29572320@qq.com
* @version Created on: 2017年4月27日 下午3:06:13
* Class description
*/
package com.cloud.finance.third.ainong.utils;

import com.google.common.base.Strings;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author cmt
 * @E-mail:29572320@qq.com
 * @version Create on: 2017年4月27日 下午3:06:13 Class description
 */

public class AES {

	private static final String cipherAlgorithm = "AES/CBC/PKCS5Padding";
//	private static final String cipherAlgorithm = "AES/ECB/PKCS5Padding";
	private static final String keyAlgorithm = "AES";

	/**
	 * AES 加密
	 * 
	 * @param plainText
	 * @param keyText
	 * @return
	 */
	public static byte [] encode(String plainText, String keyText) {

//		byte[] keyBytes = getUTF8Bytes(keyText);
//		// AES密钥长度为128bit、192bit、256bit，默认为128bit
//		if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
//			throw new RuntimeException("AES密钥长度不合法,内容: " + keyText);
//		}
//
//		SecretKeySpec key = new SecretKeySpec(keyBytes, keyAlgorithm);
//		IvParameterSpec iv = new IvParameterSpec(getUTF8Bytes(keyText));
//		Properties properties = new Properties();
//
//		// Encryption with CryptoOutputStream.
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//		try (CryptoOutputStream cos = new CryptoOutputStream(cipherAlgorithm, properties, outputStream, key, iv)) {
//			cos.write(getUTF8Bytes(plainText));
//			cos.flush();
//		} catch (IOException e) {
//			throw new RuntimeException(plainText, e);
//		}
//		//return Arrays.toString(outputStream.toByteArray());
//		//return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
//		return outputStream.toByteArray();
		return AESEncrypt(plainText,keyText);
	}

	
	
	
	public static String decode(byte [] encodeStr, String keyText) {

//		byte[] keyBytes = getUTF8Bytes(keyText);
//
//		if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
//			throw new RuntimeException("AES密钥长度不合法,内容: " + keyText);
//		}
//		SecretKeySpec key = new SecretKeySpec(keyBytes, keyAlgorithm);
//		IvParameterSpec iv = new IvParameterSpec(getUTF8Bytes(keyText));
//		// Decryption with CryptoInputStream.
//		InputStream inputStream = new ByteArrayInputStream(encodeStr);
//		Properties properties = new Properties();
//		try (CryptoInputStream cis = new CryptoInputStream(cipherAlgorithm, properties, inputStream, key, iv)) {
//			byte[] decryptedData = new byte[1024];
//			int decryptedLen = 0;
//			int i;
//			while ((i = cis.read(decryptedData, decryptedLen, decryptedData.length - decryptedLen)) > -1) {
//				decryptedLen += i;
//			}
//			return new String(decryptedData, 0, decryptedLen, StandardCharsets.UTF_8);
//		} catch (IOException e) {
//			throw new  RuntimeException(Arrays.toString(encodeStr), e);
//		}
		return AESDecrypt(encodeStr,keyText);
	}
	
	
	public static String generateRandomKey() {
		return generateRandomKey(16);
	}
	/**
	 * 生成随机密钥
	 * 
	 * @param size
	 *            位数
	 * @return
	 */
	public static String generateRandomKey(int size) {
		StringBuilder key = new StringBuilder();
		String chars = "0123456789ABCDEF";
		for (int i = 0; i < size; i++) {
			int index = (int) (Math.random() * (chars.length() - 1));
			key.append(chars.charAt(index));
		}
		return key.toString();
	}
	
	@Deprecated
	public static byte [] AESEncrypt(String plainText, String keyText) {
		byte[] bytes = AESEncrypt(getUTF8Bytes(plainText), getUTF8Bytes(keyText), keyAlgorithm, cipherAlgorithm,
				keyText);
		return bytes;
		//return Arrays.toString(bytes);
		//return new String(bytes, StandardCharsets.UTF_8);
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
	 * @throws RuntimeException
	 */
	@Deprecated
	public static byte[] AESEncrypt(byte[] plainBytes, byte[] keyBytes, String keyAlgorithm, String cipherAlgorithm,
			String IV) {
		try {
			// AES密钥长度为128bit、192bit、256bit，默认为128bit
			if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
				throw new RuntimeException("AES密钥长度不合法");
			}

			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
			if (Strings.emptyToNull(IV) != null) {
				IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
				cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			}

			byte[] encryptedBytes = cipher.doFinal(plainBytes);

			return encryptedBytes;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(String.format("没有[%s]此类加密算法", cipherAlgorithm),e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(String.format("没有[%s]此类填充模式", cipherAlgorithm),e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("无效密钥",e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException("无效密钥参数",e);
		} catch (BadPaddingException e) {
			throw new RuntimeException("错误填充模式",e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("加密块大小不合法",e);
		}
	}

	
	@Deprecated
	private static String  AESDecrypt(byte [] encodeStr, String keyText) {
		byte[] bytes = AESDecrypt(encodeStr, getUTF8Bytes(keyText), keyAlgorithm, cipherAlgorithm,
				keyText);
		//return bytes;
		return new String(bytes, StandardCharsets.UTF_8);
		//return Arrays.toString(bytes);
		
		
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
	 * @throws RuntimeException
	 */
	@Deprecated
	private static byte[] AESDecrypt(byte[] encryptedBytes, byte[] keyBytes, String keyAlgorithm,
			String cipherAlgorithm, String IV) {
		try {
			// AES密钥长度为128bit、192bit、256bit，默认为128bit
			if (keyBytes.length % 8 != 0 || keyBytes.length < 16 || keyBytes.length > 32) {
				throw new RuntimeException("AES密钥长度不合法");
			}

			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			SecretKey secretKey = new SecretKeySpec(keyBytes, keyAlgorithm);
			if (IV != null && Strings.emptyToNull(IV) != null) {
				IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
				cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
			} else {
				cipher.init(Cipher.DECRYPT_MODE, secretKey);
			}

			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

			return decryptedBytes;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(String.format("没有[%s]此类加密算法", cipherAlgorithm),e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(String.format("没有[%s]此类填充模式", cipherAlgorithm),e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("无效密钥",e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException("无效密钥参数",e);
		} catch (BadPaddingException e) {
			throw new RuntimeException("错误填充模式",e);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException("解密块大小不合法",e);
		}
	}


	private static byte[] getUTF8Bytes(String input) {
		return input.getBytes(StandardCharsets.UTF_8);
	}


}
