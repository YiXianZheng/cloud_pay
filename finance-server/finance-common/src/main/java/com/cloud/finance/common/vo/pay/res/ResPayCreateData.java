package com.cloud.finance.common.vo.pay.res;

import com.cloud.sysconf.common.utils.finance.MD5Util;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求交易查询API需要提交的数据
 */
@Data
public class ResPayCreateData {

    //每个字段具体的意思请查看API文档

	private String sign = "";
	private boolean success =true;
	private String message= "";
	private String code= "";
	private String merOrderId= "";
	private String finalOrderId= "";
	private String ptOrderId= "";
	private String orderStatus= "";
	private String orderMessage= "";
	private String transAmt= "";
	private String merId= "";


	public ResPayCreateData(boolean success, String message,String code,String merOrderId,String finalOrderId,String ptOrderId,String orderStatus,String orderMessage,String transAmt,String merId,String md5Key) throws Exception {
		this.success = success;
		this.message = message;
		this.code = code;
		this.merOrderId = merOrderId;
		this.finalOrderId = finalOrderId;
		this.ptOrderId = ptOrderId;
		this.orderStatus = orderStatus;
		this.orderMessage = orderMessage;
		this.transAmt = transAmt;
		this.merId = merId;
		this.sign = MD5Util.getSign(toMap(), md5Key);
	}

	private Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Object obj;
			try {
				obj = field.get(this);
				if (obj != null) {
					if(!field.getName().equals("sign")){
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
		return "QueryOrderReqData [merId=" + merId + ", merOrderId=" + merOrderId + ", sign=" + sign + "]";
	}

}
