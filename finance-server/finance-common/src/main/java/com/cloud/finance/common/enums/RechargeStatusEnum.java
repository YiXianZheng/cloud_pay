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
public enum RechargeStatusEnum {

	CASH_STATUS_WAIT(0, "待审核"),
	CASH_STATUS_SUCCESS(1, "代付成功"),
	CASH_STATUS_DOING(2, "代付处理中"),
	CASH_STATUS_REJECT(3, "已驳回"),
	CASH_STATUS_FAIL(4, "代付失败")
	;

	private Integer status;
	private String value;

	RechargeStatusEnum(Integer status, String value){
		this.status = status;
		this.value = value;
	}

	public static List<Map<String,Object>> loadRechargeStatus(){
		List<Map<String,Object>> list =new ArrayList<>();
		for (RechargeStatusEnum type : RechargeStatusEnum.values()){
			Map<String, Object> map = new HashMap<>();
			map.put("status", type.getStatus());
			map.put("value", type.getValue());
			list.add(map);
		}
		return list;
	}

	public static String getByStatus(Integer status){
		if(status == null) return "";
		for (RechargeStatusEnum payStatusEnum : RechargeStatusEnum.values()){
			if(status == payStatusEnum.getStatus()){
				return payStatusEnum.value;
			}
		}
		return "";
	}
}
