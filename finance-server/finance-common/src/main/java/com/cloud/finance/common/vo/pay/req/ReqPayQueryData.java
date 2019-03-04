package com.cloud.finance.common.vo.pay.req;

import com.cloud.sysconf.common.utils.finance.MD5Util;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求交易查询API需要提交的数据
 */
@Data
public class ReqPayQueryData {

    //每个字段具体的意思请查看API文档

	private String sign = "";
	private String assCode = "";
	private String assPayOrderNo = "";


	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getAssCode() {
		return assCode;
	}

	public void setAssCode(String assCode) {
		this.assCode = assCode;
	}

	public String getAssPayOrderNo() {
		return assPayOrderNo;
	}

	public void setAssPayOrderNo(String assPayOrderNo) {
		this.assPayOrderNo = assPayOrderNo;
	}

	public ReqPayQueryData(String assCode, String assPayOrderNo,String md5Key) throws Exception {
		this.assCode = assCode;
		this.assPayOrderNo = assPayOrderNo;
		this.sign = MD5Util.getSign(toMap(), md5Key);
	}

	private Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		Field[] fields = this.getClass().getDeclaredFields();
		StringBuilder mapValue = new StringBuilder();
		for (Field field : fields) {
			Object obj;
			try {
				obj = field.get(this);
				if (obj != null) {
					if(!field.getName().equals("sign")){
						mapValue.append(field.getName()).append("-").append(obj);
						map.put(field.getName(), obj);
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
	@Override
	public String toString() {
		return "GjQueryOrderReqData [assCode=" + assCode + ", assPayOrderNo=" + assPayOrderNo + ", sign=" + sign + "]";
	}

}
