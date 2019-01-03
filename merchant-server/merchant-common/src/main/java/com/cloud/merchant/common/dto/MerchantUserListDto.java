package com.cloud.merchant.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * @Auther Toney
 * @Date 2018/8/4 11:27
 * @Description:
 */
@Data
public class MerchantUserListDto extends BaseDto {

    private String id;

    private String agentName;     //代理名称

    private String merchantCode;   //商户号

    private String merchantName;   //商户名称

    private String merchantType;   //商户类型

    private String sysUserName;   //系统用户名称

    private String sysUserLoginName;   //系统用户登陆名

    private Integer optStatus;      //操作权限  1：正常   0：冻结

    private Integer cashStatus;     //提现状态  1：正常   0：冻结

    private Integer payStatus;      //API支付权限  1：正常  0：冻结

    private Integer commissionType;    //代付手续费类型（1：固定费用  2：百分比）

    private Double commissionCharge;    //代付手续费

    private String bankCode;        //银行编码

    private String bankName;        //银行名称

    private String bankBranchName;  //支行信息

    private String bankCardHolder;  //银行卡持卡人

    private String bankCardNo;      //银行卡号

    private String bankProvince;    //银行所在省份

    private String bankCity;        //银行所在城市
}
