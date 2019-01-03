package com.cloud.finance.common.utils;

/**
 *
 * 类描述：常量
 *
 * @author lxl
 * @since 1.0, 2016年12月27日
 */
public final class SysCashResultConstants {

	private SysCashResultConstants() {

	}
	
	
	public static final int SUCCESS_DAIFU_RESULT = 70000;//代付商户号不能为

	public static final int ERROR_DAIFU_MERCHANT_ID_NULL = 70001;//代付商户号不能为空
	public static final int ERROR_DAIFU_CASH_ID_NULL = 70002;//代付商户号不能为空

	public static final int ERROR_DAIFU_MONEY_MIN = 70003;//代付商户号不能为空

	public static final int ERROR_DAIFU_BANLANCE_NOT_ENOUGH = 70016;//代付商户号不能为空  ENOUGH

	public static final int ERROR_DAIFU_BANLANCE_MAX = 70017;//代付商户号不能为空  ENOUGH

	public static final int ERROR_DAIFU_TYPE_NOT_RIGHT = 70004;//支付类型不存在
	public static final int ERROR_DAIFU_TYPE_NULL = 70005;//支付类型错误


	public static final int ERROR_DAIFU_BACK_URL_NULL = 70006;//支付类型不存在

	public static final int ERROR_DAIFU_BANK_NULL = 70008;//收款人信息填写不完整

	public static final int ERROR_DAIFU_MERCHANT_NOT_REGISTED = 70009;//代付商户号不能为空

	public static final int ERROR_DAIFU_SIGN_RESULT_NULL = 70010;//MD5签名结果空

	public static final int ERROR_DAIFU_SIGN_RESULT_EXCEPTION = 70011;//MD5签名异常

	public static final int ERROR_DAIFU_SIGN_RESULT_ERROR = 70012;//MD5签名错误

	public static final int ERROR_DAIFU_BANK_EXPTION = 70013;//代付交易银行异常

	public static final int ERROR_DAIFU_TEST_ACCOUNT = 70014;//测试商户不允许代付

	public static final int ERROR_DAIFU_CASH_ID_REPEAT = 70015;//代付订单号重复


	public static final int ERROR_QUERY_DAIFU_MERCHANT_ID_NULL = 30101;//商户ID为空

	public static final int ERROR_QUERY_DAIFU_MERCHANT_NOT_REGIST = 30102;//商户ID错误

	public static final int ERROR_QUERY_DAIFU_MERCHANT_ORDER_ID_NUL = 30103;//商户订单号不能为空


	public static final int SUCCESS_QUERY_ORDER = 10000;//查询成功
	
	
	public static final int ERROR_QUERY_ORDER = 40001;//查询失败 订单不存在
	public static final int SUCCESS_CASH_STOP = 40002;	//代付维护中

	public static final int ERROR_BANK_CARD_NUMBER_DOES_NOT_MATCH = 50001;//商户银行卡绑定时卡号不符合


	public static final int ERROR_CASH_ACCOUNT_ERR = 80001;//代付账户错误

    public static final int ERROR_CASH_AMOUNT_ERR = 80002;//代付金额错误

    public static final int ERROR_CASH_KEY_ERR = 80003;//代付秘钥错误

    public static final int ERROR_CASH_PARAMS_ERR = 80004;//代付参数错误

    public static final int ERROR_CASH_CHECK_ERR = 80005;//判断是否可以代付异常

    public static final int ERROR_CASH_CHANNEL_ERR = 80006;//代付金额错误

    public static final int ERROR_CASH_APPLY_ERR = 80007;//代付申请




}
