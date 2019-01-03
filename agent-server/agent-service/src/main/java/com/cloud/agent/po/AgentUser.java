package com.cloud.agent.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 代理用户的 po
 * @Auther Toney
 * @Date 2018/7/29 15:48
 * @Description:
 */
@Entity
@Data
public class AgentUser extends BasePo {

    public static final int OPT_STATUS_COMMON = 1;
    public static final int OPT_STATUS_FROZEN = 0;

    public static final int AUTH_STATUS_WAIT = 1;
    public static final int AUTH_STATUS_SUCCESS = 2;
    public static final int AUTH_STATUS_FAIL = 3;

    public static final int CASH_STATUS_COMMON = 1;
    public static final int CASH_STATUS_FROZEN = 0;

    public static final int COMMISSION_TYPE_FIXED = 1;
    public static final int COMMISSION_TYPE_PERCENT = 2;

    @Id
    @GeneratedValue(generator = "idGenerator")
    @GenericGenerator(name = "idGenerator", strategy = "guid")
    private String id;

    private String agentCode;   //代理编码

    private String agentName;   //代理名称

    private String agentType;   //代理类型

    private String sysUserId;   //系统用户ID

    private String phone;       //联系电话

    private String email;       //邮箱

    private Integer optStatus;      //操作权限  1：正常   0：冻结

    private Integer authStatus;     //认证状态  1：待审核  2：审核通过  3：审核不通过

    private Integer cashStatus;     //提现状态  1：正常   0：冻结

    private String bankCode;        //银行编码

    private String bankName;        //银行名称

    private String bankBranchName;  //支行信息

    private String bankCardHolder;  //银行卡持卡人

    private String bankCardNo;      //银行卡号

    private String thirdChannels;   //指定的第三方通道集合  ，1，2，3，

    private Integer commissionType;    //代付手续费类型（1：固定费用  2：百分比）

    private Double commissionCharge;    //代付手续费

    /**
     * 初始化代理商状态
     */
    public void initStatus(){
        this.optStatus = OPT_STATUS_COMMON;
        this.authStatus = AUTH_STATUS_WAIT;
        this.cashStatus = CASH_STATUS_COMMON;
    }
}
