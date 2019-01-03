package com.cloud.sysconf.common.utils.finance;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MD5Util {
	private static Logger logger = LoggerFactory.getLogger(MD5Util.class);

	
	private static final String SIGN_KEY = "sign";

	
	private static final String SECRET_KEY = "key";

	
	public static String getSign(Object o, String md5Key) throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		Class cls = o.getClass();
		Field[] fields = cls.getDeclaredFields();
		for (Field f : fields) {
			System.out.println(f.getName());
			if (f.getName().equals(SIGN_KEY)) {
				continue;
			}
			f.setAccessible(true);
			if (f.get(o) != null && !"".equals(f.get(o))) {
				list.add(f.getName() + "=" + f.get(o) + "&");
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		sb.append(SECRET_KEY).append("=").append(md5Key);
		String str2Sign = sb.toString();
		System.out.println("Sign Before MD5:" + str2Sign);
		String result = MD5.MD5Encode(str2Sign).toUpperCase();
		System.out.println("Sign Result:" + result);
		return result;
	}

	public static String getSignTongLian(Map<String, Object> map, String md5Key)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getKey().equals(SIGN_KEY)) {
				continue;
			}
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add(entry.getKey() + "=" + entry.getValue()+"&" );
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		sb.append(SECRET_KEY).append("=").append(md5Key);
		String str2Sign = sb.toString();
		logger.info("Common Sign Before MD5:" + str2Sign);
		String result = MD5.MD5Encode(str2Sign).toUpperCase();
		logger.info("Common Sign Result:" + result);
		return result;
	}

	public static String getSignChangYing(Map<String, Object> map, String md5Key)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getKey().equals(SIGN_KEY)) {
				continue;
			}
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add(entry.getKey() + "=" + entry.getValue()+"&" );
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		sb.append(SECRET_KEY).append("=").append(md5Key);
		String str2Sign = sb.toString();
		logger.info("Common Sign Before MD5:" + str2Sign);
		String result = MD5.MD5Encode(str2Sign).toUpperCase();
		logger.info("Common Sign Result:" + result);
		return result;
	}

	public static String getSignShangYunKe(Map<String, Object> map, String md5Key)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getKey().equals("hmac")) {
				continue;
			}
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add(entry.getKey() + "=" + entry.getValue()+"&" );
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		sb.append(SECRET_KEY).append("=").append(md5Key);
		String str2Sign = sb.toString();
		logger.info("Common Sign Before MD5:" + str2Sign);
		String result = MD5.MD5Encode(str2Sign).toUpperCase();
		logger.info("Common Sign Result:" + result);
		return result;
	}

	/**
	 *
	 * @param map
	 * @param md5Key
	 * @throws Exception
	 */
	public static String getSignMiBei(Map<String, String> map, String md5Key)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getKey().equals(SIGN_KEY)) {
				continue;
			}
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add(entry.getKey() + "=" + entry.getValue() +"&");
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		sb.append(SECRET_KEY).append("=").append(md5Key);
		String str2Sign = sb.toString();
		logger.info("MiBei Common Sign Before MD5:" + str2Sign);
		String result = MD5.MD5Encode(str2Sign).toUpperCase();
		logger.info("MiBei Common Sign Final Result:" + result);
		return result;
	}

	/**
	 *
	 * @param map
	 * @param md5Key
	 * @throws Exception
	 */
	public static String getSign(Map<String, Object> map, String md5Key)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getKey().equals(SIGN_KEY)) {
				continue;
			}
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add("&"+entry.getKey() + "=" + entry.getValue() );
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
//		sb.append(SECRET_KEY).append("=").append(md5Key);
		sb.append(md5Key);
		String str2Sign = sb.toString();
		logger.info("Common Sign Before MD5:" + str2Sign);
		String result = MD5.MD5Encode(str2Sign).toUpperCase();
		logger.info("Common Sign Result:" + result);
		return result;
	}

	public static String getSignJson(Map<String, Object> map, String md5Key)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getKey().equals(SIGN_KEY)) {
				continue;
			}
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add("&"+entry.getKey() + "=" + entry.getValue() );
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
//		sb.append(SECRET_KEY).append("=").append(md5Key);
		sb.append(md5Key);
		String str2Sign = sb.toString();
		logger.info("Common Sign Before MD5:" + str2Sign);
		String result = MD5.MD5Encode(str2Sign).toUpperCase();
		logger.info("Common Sign Result:" + result);
		return result;
	}

	public static String getSignWlb(String source)
			throws Exception {
		String str2Sign = source;
		logger.info("Wlb Sign Before MD5:" + str2Sign);
		String result = MD5.MD5Encode(str2Sign);
		logger.info("Wlb Sign Result:" + result);
		return result;
	}
	public static String formatMapToUrl(Map<String, String> map)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add(entry.getKey() + "=" + entry.getValue()+"&" );
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		return sb.toString();
	}

	public static String formatMapToUrlEncode(Map<String, String> map)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add(entry.getKey() + "=" + URLEncoder.encode((String) entry.getValue(),"utf-8")+"&" );
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		return sb.toString();
	}



	/**
	 * ����ǩ��
	 *
	 * @param jsonObject
	 *            Ҫ����ǩ���json���
	 * @param md5Key
	 *            ��Կ
	 * @return ǩ��
	 * @throws Exception
	 */
	public static String getSign(JSONObject jsonObject, String md5Key)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
			if (entry.getKey().equals(SIGN_KEY)) {
				continue;
			}
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add(entry.getKey() + "=" + entry.getValue() + "&");
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		sb.append(SECRET_KEY).append("=").append(md5Key);
		String str2Sign = sb.toString();
		System.out.println("Sign Before MD5:" + str2Sign);
		String result = MD5.MD5Encode(str2Sign).toUpperCase();
		System.out.println("Sign Result:" + result);
		return result;
	}

	/**
	 * ��֤ǩ��
	 *
	 * @param map
	 *            Ҫ����ǩ���map���
	 * @param md5Key
	 *            ��Կ
	 * @param sign
	 *            ǩ��
	 * @return
	 * @throws Exception
	 */
	public static boolean verifySign(Map<String, Object> map, String md5Key,
			String sign) throws Exception {
		String md5Text = getSign(map, md5Key);
		return md5Text.equalsIgnoreCase(sign);
	}

	/**
	 * ��֤ǩ��
	 *
	 * @param jsonObject
	 *            Ҫ����ǩ���json���
	 * @param md5Key
	 *            ��Կ
	 * @param sign
	 *            ǩ��
	 * @return
	 * @throws Exception
	 */
	public static boolean verifySign(JSONObject jsonObject, String md5Key,
			String sign) throws Exception {
		String md5Text = getSign(jsonObject, md5Key);
		return md5Text.equalsIgnoreCase(sign);
	}

	/**
	 * ��֤ǩ��
	 *
	 * @param o
	 *            Ҫ����ǩ�����ݶ���
	 * @param md5Key
	 *            ��Կ
	 * @param sign
	 *            ǩ��
	 * @return
	 * @throws Exception
	 */
	public static boolean verifySign(Object o, String md5Key, String sign)
			throws Exception {
		String md5Text = getSign(o, md5Key);
		return md5Text.equalsIgnoreCase(sign);
	}
	public static String formatMapToUrlObject(Map<String, Object> map)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add(entry.getKey() + "=" + entry.getValue()+"&" );
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		return sb.toString();
	}
	public static String formatMapToUrlEncodeObject(Map<String, Object> map)
			throws Exception {
		ArrayList<String> list = new ArrayList<String>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (entry.getValue() != null && !"".equals(entry.getValue())) {
				list.add(entry.getKey() + "=" + URLEncoder.encode((String) entry.getValue(),"utf-8")+"&" );
			}
		}
		int size = list.size();
		String[] arrayToSort = list.toArray(new String[size]);
		Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; i++) {
			sb.append(arrayToSort[i]);
		}
		return sb.toString();
	}
}
