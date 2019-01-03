package com.cloud.finance.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * 订单信息
 * @version 2018-08-13
 */
@Entity
@Data
public class ShopPay extends BasePo {

    public static final int SUCCESS_FLAG_YES = 1;
    public static final int SUCCESS_FLAG_NO = 2;

    public static final int STEP_TODAY = 1;
    public static final int STEP_BEFORE_TODAY = 2;

	@Id
	private String id;

	//平台相关
	private String sysPayOrderNo;			// 平台支付订单号

	private Integer sysPayOrderType;		// 平台订单类型

	private Integer payStatus;				// 付款状态;0未付款;1已付款;2已经退款;3部分退款;4付款中; 5风控冻结;

	private String channelTypeCode;			// 支付类型

	private String bankCode;				//银行编码

	private Date payCompleteTime;			// 支付时间

	//商户相关
	private String merchantCode;			//商户号

	private String merchantUser;			//商户ID

	private String merchantOrderNo;				// 商户订单号

	private String merchantGoodsTitle;			// 商户订单标题

	private String merchantGoodsDesc;			// 商户订单描述

	private Double merchantPayMoney;			// 支付金额

	private String merchantPayMessage;			// 商户备注

	private String merchantNotifyUrl;			// 异步通知地址

	private String merchantReturnUrl;			// 同步通知地址

	private String merchantCancelUrl;			// 取消地址

	//代理相关
	private String agentUser;				//代理ID

	private String agentCode;				//代理号

	//通道费用
	private Double merchantCostRate;			//商户通道费用---费率

	private Double merchantCostMoney;			//商户通道费用---金额

	private Double agentCostRate;			//代理通道费用---费率

	private Double agentCostMoney;			//代理通道费用---金额

	private String thirdChannelId;			//第三方通道ID

	private Integer thirdChannelType;		//第三方通道类型

	private Double thirdChannelCostRate;	//第三方通道费用---费率

	private Double thirdChannelCostMoney;	//第三方通道费用---金额

	private String thirdChannelOrderNo;		//第三方订单号

	private String thirdChannelRespMsg;		//第三方通道返回信息

	private Integer thirdChannelNotifyFlag;	//第三方通道是否回调成功  1 是  0 否

    private Integer successFlag;			//是否成功  1 是  2 否

	private Integer step;					//今日订单： 是 1  否 2

	private Integer source;					//来源  1：移动端  2：PC端  3：系统补单


}