package com.cloud.finance.common.utils;

/**
 *
 * 类描述：常量
 *
 * @author lxl
 * @since 1.0, 2016年12月27日
 */
public final class SysPayResultConstants {

	private SysPayResultConstants() {

	}

	public static final int ERROR_PAY_AMOUNT_PARAM = 20001;//支付金额异常
	public static final int ERROR_PAY_PARAMS_NULL = 20202;	//商户号不能为空
	public static final int ERROR_PAY_MERCHANT_ID_NULL = 20002;//商户号不能为空
	public static final int ERROR_PAY_MERCHANT_NOT_REGISTED = 20003;//商户号未注册

	public static final int ERROR_PAY_MENT_TYPE_NOT_RIGHT = 20004;//支付类型不存在
	public static final int ERROR_PAY_MENT_TYPE_NULL = 20005;//支付类型错误
	public static final int ERROR_BACK_URL_NULL = 20006;//回调通知地址不能为空
	public static final int ERROR_MERCHANT_ORDER_ID_NULL = 20008;//商户订单ID为空

	public static final int ERROR_SIGN_RESULT_NULL = 20009;//MD5签名结果空
	public static final int ERROR_SIGN_RESULT_EXCEPTION = 20010;//MD5签名异常
	public static final int ERROR_SIGN_RESULT_ERROR = 20011;//MD5签名错误

	public static final int ERROR_MERCHANT_ORDER_ID_REPEAT = 20012;//商户订单号重复

	public static final int ERROR_PAY_TYPE_NOT_SUPPORT = 20013;//支付类型暂不支持

	public static final int ERROR_THIRD_BANK = 20014;//银行错误

	public static final int ERROR_PAY_CHANNEL_NULL = 20015; //无可用通道

	public static final int ERROR_PAY_CHANNEL_UNUSABLE = 20016; //通道请求错误

	public static final int SUCCESS_MAKE_ORDER = 10000;//下单成功

	public static final int SUCCESS_DOING = 10001;//交易处理中



	public static final int ERROR_QUERY_MERCHANT_ID_NULL = 30001;//商户ID为空

	public static final int ERROR_QUERY_MERCHANT_NOT_REGIST = 30002;//商户ID错误

	public static final int ERROR_QUERY_MERCHANT_ORDER_ID_NUL = 30003;//商户订单号不能为空


	public static final int SUCCESS_QUERY_ORDER = 10000;//查询成功
	public static final int ERROR_QUERY_ORDER = 40001;//查询失败 订单不存在

	public static final int ERROR_SYS_PARAMS = 40002;//创建支付参数异常

	public static final int CHANNEL_ERROR = 50000;//通道调用异常

	public static final int CHANNEL_REQUEST_ERROR = 50001;//通道支付请求异常

	public static final int ERROR_MERCHANT_AUTH = 20016;//商户API支付权限异常
}
