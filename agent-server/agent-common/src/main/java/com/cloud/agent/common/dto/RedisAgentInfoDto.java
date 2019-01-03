package com.cloud.agent.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * @Auther Toney
 * @Date 2018/8/6 10:46
 * @Description:  用于Redis代理信息持久化
 */
@Data
public class RedisAgentInfoDto extends BaseDto {

    private String id;

    private String agentCode;   //商户号

    private String agentName;   //商户名称

    private String agentType;   //商户类型

    private String sysUserId;

    private String sysUserName;   //系统用户名称

    private String sysUserLoginName;   //系统用户登陆名

    private String phone;       //联系电话

    private String email;       //邮箱

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

    private String bankProvince;    //银行所在省份

    private String bankCity;        //银行所在城市

    private String activePayChannels; //可使用的支付通道

    //接口费率  对照sys_pay_channel表
    private Double qq_qrcode;
    private Double qq_self_wap;
    private Double qq_h5_wake;
    private Double jd_self_wap;
    private Double jd_h5_wake;
    private Double jd_qrcode;
    private Double wx_self_wap;
    private Double wx_h5_wake;
    private Double wx_qrcode;
    private Double ali_self_wap;
    private Double ali_h5_wake;
    private Double ali_qrcode;
    private Double syt_all_in;
    private Double gate_h5;
    private Double gate_web_syt;
    private Double gate_web_direct;
    private Double gate_qrcode;

}
