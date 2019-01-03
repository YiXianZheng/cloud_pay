package com.cloud.sysconf.common.enums;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色类型枚举类
 * @Auther Toney
 * @Date 2018/7/26 10:24
 * @Description:
 */
@Getter
public enum RoleTypeEnum {

    ROLE_ROOT_ADMIN("root_admin", "系统管理员"),
    ROLE_GENERAL_MANAGER("general_manager", "总经理"),
    ROLE_OPERATIONS("operations", "系统运维"),
    ROLE_CUSTOMER_SERVICE("customer_service", "系统客服"),
    ROLE_SHAREHOLDERS_AGENT("shareholders_agent", "股东代理"),
    ROLE_CHANNEL_AGENT("channel_agent", "通道代理"),
    ROLE_MERCHANTS_AGENT("merchants_agent", "商户代理"),
    ROLE_DEFAULT_AGENT("default_agent", "默认代理"),
    ROLE_DEFAULT_MERCHANT("default_merchant", "默认商户")
    ;

    private String code;

    private String name;

    RoleTypeEnum(String code, String name){
        this.code = code;
        this.name = name;
    }

    public static List<Map<String,Object>> loadRoleType(){
        List<Map<String,Object>> list =new ArrayList<>();
        for (RoleTypeEnum type : RoleTypeEnum.values()){
            if(!"root_admin".equals(type.getCode()) && !"default_agent".equals(type.getCode())
                    && !"default_merchant".equals(type.getCode())) {
                Map<String, Object> map = new HashMap<>();
                map.put("code", type.getCode());
                map.put("value", type.getName());
                list.add(map);
            }
        }
        return list;
    }

    /**
     * 通过code获取name
     * @param code
     * @return
     */
    public static String getRoleNameByCode(String code){
        if(StringUtils.isBlank(code)) return "";
        for (RoleTypeEnum type : RoleTypeEnum.values()){
            if(code.equals(type.code)){
                return type.name;
            }
        }
        return "";
    }
}
