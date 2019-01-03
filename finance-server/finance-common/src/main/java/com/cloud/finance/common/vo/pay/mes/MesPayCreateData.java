package com.cloud.finance.common.vo.pay.mes;

import lombok.Data;

/**
 * HC返回给商户的数据
 */
@Data
public class MesPayCreateData {

	private String success;

	private String message;

	private String code;

	private String payUrl;

	private String assPayOrderNo;

	private String sysPayOrderNo;

	private String money;

}
