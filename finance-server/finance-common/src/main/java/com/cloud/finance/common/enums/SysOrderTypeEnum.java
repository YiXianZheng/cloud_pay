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
public enum SysOrderTypeEnum {

	//1-充值订单,2-风险订单,3-调账冲账

	SYS_ORDER_TYPE_RECHARGE(1, "充值订单"),
	SYS_ORDER_TYPE_RISK(2, "风险订单"),
	SYS_ORDER_TYPE_RECONCILIATION(3, "调账冲账"),
	;

	private Integer type;
	private String name;

	SysOrderTypeEnum(Integer type, String name){
		this.type = type;
		this.name = name;
	}

	public static List<Map<String,Object>> loadOrderType(){
		List<Map<String,Object>> list =new ArrayList<>();
		for (SysOrderTypeEnum type : SysOrderTypeEnum.values()){
			Map<String, Object> map = new HashMap<>();
			map.put("type", type.getType());
			map.put("name", type.getName());
			list.add(map);
		}
		return list;
	}
}
