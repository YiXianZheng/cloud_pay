package com.cloud.finance.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * 代付订单信息
 * @version 2018-08-13
 */
@Entity
@Data
public class ShopRecharge extends BasePo {

	public static final int RECHARGE_RATE_TYPE_FIXED = 1;
	public static final int RECHARGE_RATE_TYPE_PERCENT = 2;

    public static final int NOTIFY_FLAG_YES = 1;
    public static final int NOTIFY_FLAG_NO = 2;

	@Id
	private String id;

	private String userId;					//用户ID

	private String userName;				//用户名

	private String agentUser;				//代理ID

	private String merchantUser;			//商户ID

	private String rechargeNo;				//平台代付单号

	private Double rechargeMoney;			//代付金额

	private Integer rechargeRateType;		//代付手续费类型 1：固定金额  2：百分比

	private Double rechargeRate;			//代付手续费

	private Double rechargeRateMoney;		//代付手续费金额

	private String bankAccount;				//银行卡账户名

	private String bankCode;				//银行编码

	private String bankNo;					//银行卡卡号

    private String bankBin;                 //联行号

	private String bankSubbranch;			//支行名称

	private String province;				//省份

	private String city;					//城市

	private Integer rechargeStatus;			//付款状态;0待审核;1代付成功;2代付处理中;3已驳回;4代付失败

	private Date completeTime;				//完成时间

	private String thirdChannelId;			//第三方通道ID

	private Integer thirdChannelType;		//第三方通道类型

	private Double thirdChannelCostRate;	//第三方通道费用---费率

	private Double thirdChannelCostMoney;	//第三方通道费用---金额

	private String thirdChannelOrderNo;		//第三方订单号

	private String thirdChannelRespMsg;		//第三方通道返回信息

	private Integer thirdChannelNotifyFlag;	//第三方通道是否回调成功  1 是  0 否


}