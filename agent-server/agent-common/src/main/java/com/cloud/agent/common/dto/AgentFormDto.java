package com.cloud.agent.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;

/**
 * @Auther Toney
 * @Date 2018/7/29 17:00
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class AgentFormDto extends BaseDto {

    private String id;

    private String agentName;       //代理名称

    private String agentType;       //类型

    private String loginName;       //登陆名（管理员姓名）

    private String bankName;        //开户行

    private String bankBranchName;  //分行信息

    private String bankCardNo;      //银行卡号

    private String bankCardHolder;  //持卡人

    private Integer commissionType;    //代付手续费类型（1：固定费用  2：百分比）

    private Double commissionCharge;    //代付手续费

    private ArrayList<PayChannelRateDto> channelRates; //接口费率集合json

}
