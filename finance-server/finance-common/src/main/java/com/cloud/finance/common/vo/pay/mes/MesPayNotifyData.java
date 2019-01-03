package com.cloud.finance.common.vo.pay.mes;

import com.cloud.sysconf.common.utils.finance.MD5Util;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * H5扫码支付提交Post数据给到API之后，API会返回JSON格式的数据，这个类用来装这些数据
 */
@Data
public class MesPayNotifyData {

	/*
	 *  应答码 		respCode String(10) 是
		应答描述 		respMsg String(100) 是 对应应答码信息描述
		商户订单号 	orderNo String(32)  商户自定义订单号，唯一标识
		支付链接 	codeUrl String(50) 否 支付链接地址
		随机字符串	randomStr String(32) 是 确保签名不可预测
		签名方式		signType String(10) 是 MD5
		签名			sign String(32) 是
	 */

	private String assCode = "";		//商户编码
	private String assPayOrderNo = "";	//商户订单号
	private String sysPayOrderNo = "";
	private String assPayMoney = "";
	private String assPayMessage = "";
	
	private String succTime = "";
	private String respCode = "";
	private String respMsg = "";
	private String sign = "";
	public static final String[] SIGN_PARAMS = {"assCode","assPayOrderNo","sysPayOrderNo","assPayMoney","assPayMessage","succTime","respCode","respMsg"};

	public MesPayNotifyData(String assCode, String assPayOrderNo,String sysPayOrderNo, String assPayMoney,String assPayMessage,String succTime, String respCode, String respMsg, String md5Key) throws Exception {
		super();
		this.assCode = assCode;
		this.assPayOrderNo = assPayOrderNo;
		this.sysPayOrderNo = sysPayOrderNo;
		this.assPayMoney = assPayMoney;
		this.assPayMessage = assPayMessage;
		this.succTime = succTime;
		this.respCode = respCode;
		this.respMsg = respMsg;
		this.sign = MD5Util.getSign(toMap(), md5Key);
	}


	public String getReturnParamUrlValue(){
		String result = null;
		try {
			result = MD5Util.formatMapToUrl(getReturnParamMap());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public String getReturnParamUrlValueEncode(){
		String result = null;
		try {
			result = MD5Util.formatMapToUrlEncode(getReturnParamMap());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}


	public Map<String, String> getReturnParamMap() {
		Map<String, String> map = new HashMap<String, String>();
		Field[] fields = this.getClass().getDeclaredFields();
		String mapValue = "";
		for (Field field : fields) {
			Object obj;
			String fieldName=field.getName();
			try {
				obj = field.get(this);
				if (obj != null) {
					//SIGN_PARAMS
					if(!fieldName.equals("SIGN_PARAMS")){
						map.put(field.getName(), (String) obj);
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		Field[] fields = this.getClass().getDeclaredFields();
		String mapValue = "";
		for (Field field : fields) {
			Object obj;
			String fieldName=field.getName();
			try {
				obj = field.get(this);
				if (obj != null) {
					if(!fieldName.equals("sign")&&Arrays.asList(SIGN_PARAMS).contains(fieldName)){
						map.put(field.getName(), (String) obj);
					}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

}
