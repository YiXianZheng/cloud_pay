package com.cloud.finance.common.vo.pay.mid;

import lombok.Data;

/**
 * 请求H5扫码支付API需要提交的数据
 */
@Data
public class MidPayCreateResult {

	// 每个字段具体的意思请查看API文档

	private String status = "";
	private String resultCode= "";
	private String resultMessage= "";
	private String sysOrderNo = "";
	private String channelOrderNo = "";
	private String payUrl = "";

}
