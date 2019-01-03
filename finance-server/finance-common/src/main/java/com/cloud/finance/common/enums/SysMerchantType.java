/**
 *
 */
package com.cloud.finance.common.enums;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author Henry
 * @CreateTime : 2015-9-28 下午11:46:07
 * @Description : XXX
 */
public enum SysMerchantType {
	COMMON_PAY_CASH("1"),
	RECHARGE_PROXY_ASS("2"),
	CASH_FENRUN_ASS("3"),
	DEBUG_MONEY_ASS("4"),
	DEBUG_API_ASS("5");
	
	private String value;
	/**
	 *trank
	 * @param value
	 */
	private SysMerchantType(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

	private static Map<String, String> labelMap = new LinkedHashMap<String, String>();

	static {
		SysMerchantType.labelMap.put(SysMerchantType.COMMON_PAY_CASH.value,    "D0交易代付");
		SysMerchantType.labelMap.put(SysMerchantType.RECHARGE_PROXY_ASS.value, "D0充值代付");
		SysMerchantType.labelMap.put(SysMerchantType.CASH_FENRUN_ASS.value,    "分润结算商户");
		SysMerchantType.labelMap.put(SysMerchantType.DEBUG_MONEY_ASS.value,    "调账充值商户");
		SysMerchantType.labelMap.put(SysMerchantType.DEBUG_API_ASS.value,      "调试测试商户");

	}

	public static Map<String, String> getLabelMap() {
		return SysMerchantType.labelMap;
	}

}
