package com.cloud.merchant.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 商户用户的 po
 * @Auther Toney
 * @Date 2018/7/29 15:48
 * @Description:
 */
@Entity
@Data
public class MerchantUser extends BasePo {

    public static final int OPT_STATUS_COMMON = 1;
    public static final int OPT_STATUS_FROZEN = 0;

    public static final int CASH_STATUS_COMMON = 1;
    public static final int CASH_STATUS_FROZEN = 0;

    public static final int PAY_STATUS_COMMON = 1;
    public static final int PAY_STATUS_FROZEN = 0;

    public static final int COMMISSION_TYPE_FIXED = 1;
    public static final int COMMISSION_TYPE_PERCENT = 2;

    @Id
    private String id;

    private String agentId;         //代理ID

    private String merchantCode;   //商户编码

    private String md5Key;          //商户公钥(密匙)

    private String md5Source;       //商户私钥

    private String merchantName;   //商户名称

    private String merchantType;   //商户类型

    private String sysUserId;   //系统用户ID

    private String phone;       //联系电话

    private String email;       //邮箱

    private Integer optStatus;      //操作权限  1：正常   0：冻结

    private Integer payStatus;     //API支付权限  1：正常   0：冻结

    private Integer cashStatus;     //提现状态  1：正常   0：冻结

    private Integer commissionType;    //代付手续费类型（1：固定费用  2：百分比）

    private Double commissionCharge;    //代付手续费

    private Double dailyLimit;      //每日限额

    private Integer rechargeLimit;   //下发限制次数

    private String bankCode;        //银行编码

    private String bankName;        //银行名称

    private String bankBranchName;  //支行信息

    private String bankCardHolder;  //银行卡持卡人

    private String bankCardNo;      //银行卡号

    private String bankProvince;    //银行所在省份

    private String bankCity;        //银行所在城市

    private String thirdChannels;   //第三方通道集合  ,1,2,3,

    /**
     * 初始化代商户状态
     */
    public void initStatus(){
        this.optStatus = OPT_STATUS_COMMON;
        this.cashStatus = CASH_STATUS_COMMON;
    }
}
