/**
 *
 */
package com.cloud.finance.common.enums;

import java.util.*;


/**
 * 支付方式的枚举类
 */
public enum SysPaymentTypeEnum {

	//微信[扫码\公众号\H5唤醒支付]
	WX_H5_JUMP("wx_h5_wake"),
	WX_QR_CODE("wx_qrcode"),
	WX_SELF_PAY("wx_self_wap"),

	//QQ[扫码\公众号\H5唤醒支付]
	QQ_H5_JUMP("qq_h5_wake"),
	QQ_QR_CODE("qq_qrcode"),
	QQ_SELF_PAY("qq_self_wap"),

	//支付宝[扫码\服务号\H5唤醒支付]
	ALI_H5_JUMP("ali_h5_wake"),
	ALI_QR_CODE("ali_qrcode"),
	ALI_SELF_PAY("ali_self_wap"),

	//京东[扫码\京东号\H5唤醒支付]
	JD_H5_JUMP("jd_h5_wake"),
	JD_QR_CODE("jd_qrcode"),
	JD_SELF_PAY("jd_self_wap"),

	GATE_H5("gate_h5"),					//银联H5
	GATE_QR_CODE("gate_qrcode"),		//银联扫码
	GATE_WEB_SYT("gate_web_syt"),		//银联网关快捷
	GATE_WEB_DIRECT("gate_web_direct"),	//银联网关直连

	//聚合收银台[可自由切换付款方式]
	SYT_ALL_IN("syt_all_in");

	private String value;

	/**
	 *
	 * @param value
	 */
	private SysPaymentTypeEnum(String value) {
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

		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.WX_QR_CODE.value, 		"微信扫码支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.WX_SELF_PAY.value, 	 	"微信公众号支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.WX_H5_JUMP.value,	 	"微信H5唤醒支付");

		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.ALI_QR_CODE.value, 		"支付宝扫码支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.ALI_SELF_PAY.value, 		"支付宝服务号支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.ALI_H5_JUMP.value, 		"支付宝H5唤醒支付");

		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.QQ_QR_CODE.value, 		"QQ扫码支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.QQ_SELF_PAY.value, 		"QQ公众号支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.QQ_H5_JUMP.value, 		"QQH5唤醒支付");

		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.GATE_QR_CODE.value,  	"银联APP扫码");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.GATE_WEB_SYT.value, 		"网关收银台支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.GATE_WEB_DIRECT.value, 	"网关直连支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.GATE_H5.value, 			"银联H5快捷支付");
		
		

		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.JD_QR_CODE.value, 		"京东扫码支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.JD_SELF_PAY.value, 		"京东服务号支付");
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.JD_H5_JUMP.value, 		"京东H5唤醒支付");
		
		SysPaymentTypeEnum.labelMap.put(SysPaymentTypeEnum.SYT_ALL_IN.value, 		"聚合收银台支付");

	}

	public static Map<String, String> getLabelMap() {
		return SysPaymentTypeEnum.labelMap;
	}


	public String getDisplay() {
		return SysPaymentTypeEnum.labelMap.get(this.getValue());
	}


	public static List<Map<String, Object>> loadPaymentType(){
		List<Map<String,Object>> list =new ArrayList<>();

		for (String key : SysPaymentTypeEnum.labelMap.keySet()){
			Map<String, Object> map = new HashMap<>();
			map.put("type", key);
			map.put("value", SysPaymentTypeEnum.labelMap.get(key));
			list.add(map);
		}
		return list;
	}

}
