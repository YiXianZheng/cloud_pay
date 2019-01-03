package com.cloud.sysconf.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * @Auther Toney
 * @Date 2018/7/26 19:33
 * @Description:
 */
@Data
public class SysLogDto extends BaseDto {

    private String userId;          //用户ID

    private String userName;        //用户名

    private String company;         //公司

    private String department;      //部门

    private String requestUrl;      //请求地址

    private String requestMethod;   //请求方式

    private String requestIp;       //IP地址

    private String classMethod;     //请求方法

    private String requestArgs;     //ARGS

    private String request;         //

    private String response;        //返回值

    private String spendTime;       //请求总耗时

    private String createTime;      //创建时间
}
