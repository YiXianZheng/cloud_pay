package com.cloud.agent.common.dto;

import lombok.Data;

/**
 * @Auther Toney
 * @Date 2018/8/4 11:27
 * @Description:
 */
@Data
public class AgentUserListDto {

    private String id;

    private String agentCode;   //代理编码

    private String agentName;   //代理名称

    private String agentType;   //代理类型

    private String sysUserName;   //系统用户名称

    private String sysUserLoginName;   //系统用户登陆名

    private Integer optStatus;      //操作权限  1：正常   0：冻结

    private Integer authStatus;     //认证状态  1：待审核  2：审核通过  3：审核不通过

    private Integer cashStatus;     //提现状态  1：正常   0：冻结

    private Integer commissionType;    //代付手续费类型（1：固定费用  2：百分比）

    private Double commissionCharge;    //代付手续费

    private String bankCode;        //银行编码

    private String bankName;        //银行名称

    private String bankBranchName;  //支行信息

    private String bankCardHolder;  //银行卡持卡人

    private String bankCardNo;      //银行卡号
}
