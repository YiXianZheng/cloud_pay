package com.cloud.finance.common.vo.pay.mid;


import lombok.Data;

/**
 * 请求支付结果和实际交易金额
 */
@Data
public class MidPayCheckResult {

	// 每个字段具体的意思请查看API文档

	private String status = "";
	private String respCode = "";
	private String respMsg = "";
	
	private String sysOrderId = "";
	private String assOrderId = "";
	private String payType = "";
	private String amount = "";
	private String createOrderTime = "";
	private String payOrderTime = "";

}
