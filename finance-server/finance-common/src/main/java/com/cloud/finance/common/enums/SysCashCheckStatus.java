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
public enum SysCashCheckStatus {
	//对付对账过程一下字段会用到
	CHANNEL_CHECK_INITED("60402"),
	CHANNEL_CHECK_SUCCESS("60003"),
	CHANNEL_CHECK_FAILED("60004"),
	CHANNEL_CHECK_DEALING("60005"),
	CHANNEL_CHECK_EXCEPTION("60006");
	private String value;
	/**
	 *trank
	 * @param value
	 */
	private SysCashCheckStatus(String value) {
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
		//完成代付请求-核对代付结果
		SysCashCheckStatus.labelMap.put(SysCashCheckStatus.CHANNEL_CHECK_INITED.value,       "核对任务建立[成功接收代付]");
		SysCashCheckStatus.labelMap.put(SysCashCheckStatus.CHANNEL_CHECK_FAILED.value,       "核对代付失败");
		SysCashCheckStatus.labelMap.put(SysCashCheckStatus.CHANNEL_CHECK_SUCCESS.value,	 	 "核对代付成功");
		SysCashCheckStatus.labelMap.put(SysCashCheckStatus.CHANNEL_CHECK_DEALING.value,  	 "核对银行处理中");
		SysCashCheckStatus.labelMap.put(SysCashCheckStatus.CHANNEL_CHECK_EXCEPTION.value,    "核对代付异常"); 
	}

	public static Map<String, String> getLabelMap() {
		return SysCashCheckStatus.labelMap;
	}


}
