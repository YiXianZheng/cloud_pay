package com.cloud.sysconf.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	public static Log logger = LogFactory.getLog(Util.class);

	/**
	 * 获取真实IP
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 快速组装Map
	 * 
	 * @param kvs
	 * @return
	 */
	public static Map<String, Object> getSimpleMap(KeyValue... kvs) {
		Map<String, Object> map = new HashMap<>();
		if (kvs != null) {
			for (KeyValue kv : kvs) {
				map.put(kv.getKey() + "", kv.getValue());
			}
		}
		return map;
	}

	/**
	 * 设置当前会话语言
	 * 
	 * @param language
	 * @return 失败返回false
	 */
	public static boolean setCurrentLanguage(HttpSession session, LanguageEnum language) {
		try {
			session.setAttribute(Constant.CURRENT_LANGUAGE, language);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 获取当前会话语言(如果找不到默认返回中文)
	 * 
	 * @return
	 */
	public static LanguageEnum getCurrentLanguage() {
		try {
			HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
			LanguageEnum language = (LanguageEnum) request.getSession().getAttribute(Constant.CURRENT_LANGUAGE);
			if (language != null) {
				return language;
			}
		} catch (Exception e) {
		}
		return LanguageEnum.zh_CN;
	}

	/**
	 * 填充消息占位符
	 * 
	 * @param msg
	 * @return
	 */
	public static String convertParam(String msg, Map<String, Object> params) {
		Pattern pattern = Pattern.compile("\\$\\{\\w*\\}");
		Matcher matcher = pattern.matcher(msg);
		matcher.reset();
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String str = matcher.group();
			String key = str.substring(2, str.length() - 1);
			if (params == null) {
				logger.warn("参数为空");
				return msg;
			}
			Object value = params.get(key);
			if (value == null) {
				logger.warn("找不到参数[" + key + "]对应的值，请检查参数列表：" + params);
			}
			matcher.appendReplacement(sb, value == null ? "" : value.toString());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}


	/**
	 *  根据请求头判断是手机还是电脑
	 * android : 所有android设备
	 * mac os : iphone ipad
	 * windows phone:Nokia等windows系统的手机
	 */
	public static boolean  isMobileDevice(HttpServletRequest request){
		String requestHeader = request.getHeader("user-agent");
		logger.info("user-agent: " + requestHeader);
		String[] deviceArray = new String[]{"android","mac os","windows phone"};
		if(requestHeader == null)
			return false;
		requestHeader = requestHeader.toLowerCase();
		for(int i=0;i<deviceArray.length;i++){
			if(requestHeader.indexOf(deviceArray[i])>0){
				return true;
			}
		}
		return false;
	}

	public static Map<String, Object> toHashMap(LinkedHashMap<String, Object> linkedHashMap){
		Map<String, Object> map = new HashMap<>();
		Iterator iterator = linkedHashMap.entrySet().iterator();
		while (iterator.hasNext()){
			Map.Entry entry = (Map.Entry) iterator.next();
			map.put(entry.getKey().toString(), entry.getValue());
		}
		return map;
	}

}
