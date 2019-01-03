package com.cloud.agent.common.enums;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理类型
 * @Auther Toney
 * @Date 2018/7/29 16:52
 * @Description:
 */
@Getter
public enum AgentTypeEnum {

    SHAREHOLDERS_AGENT("shareholders_agent", "股东代理"),
    CHANNEL_AGENT("channel_agent", "通道代理"),
    MERCHANTS_AGENT("merchants_agent", "商户代理")
    ;

    private String code;

    private String name;

    AgentTypeEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    public static List<Map<String,Object>> loadAgentType(){
        List<Map<String,Object>> list =new ArrayList<>();
        for (AgentTypeEnum type : AgentTypeEnum.values()){
            Map<String,Object> map = new HashMap<>();
            map.put("code", type.getCode());
            map.put("value", type.getName());
            list.add(map);
        }
        return list;
    }

    /**
     * 通过code获取name
     * @param code
     * @return
     */
    public static String getAgentNameByCode(String code){
        if(StringUtils.isBlank(code)) return "";
        for (AgentTypeEnum type : AgentTypeEnum.values()){
            if(code.equals(type.code)){
                return type.name;
            }
        }
        return "";
    }
}
