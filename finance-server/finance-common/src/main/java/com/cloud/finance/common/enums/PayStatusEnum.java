package com.cloud.finance.common.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 平台订单状态枚举类
 */
@Getter
public enum PayStatusEnum {

	PAY_STATUS_WAIT(0, "未付款"),
	PAY_STATUS_ALREADY(1, "已付款"),
	PAY_STATUS_RETURN(2, "已经退款"),
	PAY_STATUS_RETURN_PART(3, "部分退款"),
	PAY_STATUS_DOING(4, "付款中"),
	PAY_STATUS_FROZEN(5, "风控冻结中")
	;

	private Integer status;
	private String value;

	PayStatusEnum(Integer status, String value){
		this.status = status;
		this.value = value;
	}

	public static List<Map<String,Object>> loadPayStatus(){
		List<Map<String,Object>> list =new ArrayList<>();
		for (PayStatusEnum type : PayStatusEnum.values()){
			Map<String, Object> map = new HashMap<>();
			map.put("status", type.getStatus());
			map.put("value", type.getValue());
			list.add(map);
		}
		return list;
	}

	public static String getByStatus(Integer status){
		if(status == null) return "";
		for (PayStatusEnum payStatusEnum : PayStatusEnum.values()){
			if(status == payStatusEnum.getStatus()){
				return payStatusEnum.value;
			}
		}
		return "";
	}
}
