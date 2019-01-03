package com.cloud.sysconf.common.utils;

public class Constant {

	/** 平台用户id **/
	public static final long PLATFORM_USER_ID = 1;

	/** 存放当前会话语言 key **/
	public static final String CURRENT_LANGUAGE = "CURRENT-LANGUAGE";

	/** 存放当前会话微信登录openId **/
	public static final String WEIXIN_OPENID = "WEIXIN-OPENID";

	/**
	 * 平台标志工号
	 */
	public static final String SYS_ACCOUNT_NO = "000000";


	/**
	 *  redis 缓存代理商编码起始值  如 12000000，那么下一个代理商的编码就是 12000000 + REDIS_AGENT_COUNT + 1
	 */
	public static final String REDIS_AGENT_BASE_INCRE = "redis_agent_base_incre";

	/**
	 *  redis 缓存代理商编码起始值 的 默认值
	 */
	public static final String REDIS_AGENT_DEFAULT_INCRE = "1000000";

	/**
	 *  redis 缓存代理商编码
	 */
	public static final String REDIS_AGENT_COUNT = "redis_agent_count";

	/**
	 *	redis 缓存系统字典的key
	 */
	public static final String REDIS_SYS_DICT = "sys_dict";

	/**
	 *	redis 缓存财务通道累计交易额的key
	 */
	public static final String REDIS_FINANCE_CHANNEL_RATE = "finance_channel_rate";

}
