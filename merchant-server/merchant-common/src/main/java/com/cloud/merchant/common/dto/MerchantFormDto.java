package com.cloud.merchant.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther Toney
 * @Date 2018/7/29 17:00
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class MerchantFormDto extends BaseDto {

    private String id;

    private String agentId;         //代理ID

    private String merchantCode;    //商户号

    private String merchantName;    //商户名称

    private String merchantType;    //商户类型

    private String loginName;       //登陆名（管理员姓名）

    private String password;        //管理账号密码

    private Integer commissionType;    //代付手续费类型（1：固定费用  2：百分比）

    private Double commissionCharge;    //代付手续费

    private String bankName;        //开户行

    private String bankBranchName;  //分行信息

    private String bankCardNo;      //银行卡号

    private String bankCardHolder;  //持卡人

    private String bankProvince;    //银行所在省份

    private String bankCity;        //银行所在城市

    private Double dailyLimit;      //每日限额

    private String thirdChannels;   //指定的第三方通道集合 ，1，2，3，

    private List<PayChannelRateDto> channelRates; //接口费率集合json

}
