package com.cloud.merchant.common.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 
 * @author Henry
 * @CreateTime 2015-2-13 下午8:17:04
 * @Description XXX
 */
public class EncodeUtils {

	private static final String DEFAULT_URL_ENCODING = "UTF-8";

	public static String md5Encode(String input) {
		if (input == null) {
			return null;
		}
		return DigestUtils.md5Hex(input);
	}

	public static String hexEncode(byte[] input) {
		return Hex.encodeHexString(input);
	}

	public static byte[] hexDecode(String input) {
		try {
			return Hex.decodeHex(input.toCharArray());
		} catch (DecoderException e) {
			throw new IllegalStateException("Hex Decoder exception", e);
		}
	}

	public static String base64Encode(byte[] input) {
		return new String(Base64.encodeBase64(input));
	}

	public static String base64UrlSafeEncode(byte[] input) {
		return Base64.encodeBase64URLSafeString(input);
	}

	public static byte[] base64Decode(String input) {
		return Base64.decodeBase64(input);
	}

	public static String urlEncode(String input) {
		try {
			return URLEncoder.encode(input, DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}

	public static String urlDecode(String input) {
		try {
			return URLDecoder.decode(input, DEFAULT_URL_ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Unsupported Encoding Exception", e);
		}
	}

}
