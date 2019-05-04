package com.cloud.merchant.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @Auther Toney
 * @Date 2018/8/6 10:46
 * @Description:
 */
@Data
public class MerchantInfoDto extends BaseDto {

    public static final int CASH_STATUS_COMMON = 1;
    public static final int CASH_STATUS_FROZEN = 0;

    private String id;

    private String agentId;     //代理ID

    private String merchantCode;   //商户号

    private String merchantName;   //商户名称

    private String merchantType;   //商户类型

    private String sysUserId;   //系统用户ID

    private String sysUserName;   //系统用户名称

    private String sysUserLoginName;   //系统用户登陆名

    private String phone;       //联系电话

    private String email;       //邮箱

    private Integer optStatus;      //操作权限  1：正常   0：冻结

    private Integer cashStatus;     //提现状态  1：正常   0：冻结

    private Integer payStatus;     //API支付权限  1：正常   0：冻结

    private Integer commissionType;    //代付手续费类型（1：固定费用  2：百分比）

    private Double commissionCharge;    //代付手续费

    private Double dailyLimit;      //每日限额

    private Integer rechargeLimit;   //下发次数

    private String bankCode;        //银行编码

    private String bankName;        //银行名称

    private String bankBranchName;  //支行信息

    private String bankCardHolder;  //银行卡持卡人

    private String bankCardNo;      //银行卡号

    private String bankProvince;    //银行所在省份

    private String bankCity;        //银行所在城市

    private String thirdChannels;   //指定的第三方通道集合 ，1，2，3，

    private Date createDate;

    private List<PayChannelRateDto> channels; //支付通道

}
