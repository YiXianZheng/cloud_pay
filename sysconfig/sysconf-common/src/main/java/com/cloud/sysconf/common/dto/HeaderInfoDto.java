package com.cloud.sysconf.common.dto;

import lombok.Data;

/**
 * @Auther Toney
 * @Date 2018/7/31 11:12
 * @Description: 用户传递表中一些基本数据，通常是通过BaseController获取到的一些数据
 */
@Data
public class HeaderInfoDto {

    public static final String AUTH_PLATFORM_SYSTEM = "010000"; //平台管理系统
    public static final String AUTH_AGENT_SYSTEM = "020000";    //代理管理系统
    public static final String AUTH_MERCHANT_SYSTEM = "030000"; //商户管理系统

    private String token;

    private String curUserId;

    private String panId;

    private String auth;        //访问系统

    private String roleId;      //角色ID

    private String agentUser;

    private String merchantUser;

}
