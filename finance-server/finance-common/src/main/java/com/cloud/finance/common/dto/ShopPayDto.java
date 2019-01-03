package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * 订单信息
 */
@Data
public class ShopPayDto extends BaseDto {

	public static final int SUCCESS_FLAG_YES = 1;
	public static final int SUCCESS_FLAG_NO = 2;

	public static final int SOURCE_MOBILE = 1;
	public static final int SOURCE_PC = 2;

	private String id;

	//平台相关
	private String sysPayOrderNo;			// 平台支付订单号

	private Integer sysPayOrderType;		//平台订单类型

	private Integer payStatus;				// 付款状态;0未付款;1已付款;2已经退款;3部分退款;4付款中;

	private String channelTypeCode;			// 支付类型

	private String bankCode;				//银行编码

	private Date createTime;				//创建时间

	private Date payCompleteTime;			// 支付时间

	//商户相关
	private String merchantCode;			//商户号

	private String merchantUser;			//商户ID

	private String merchantOrderNo;			// 商户订单号

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

	private Double  agentCostRate;			//代理通道费用---费率

	private Double  agentCostMoney;			//代理通道费用---金额

	private Double  thirdChannelCostRate;	//第三方通道费用---费率

	private Double  thirdChannelCostMoney;	//第三方通道费用---金额

	private String thirdChannelOrderNo;		//第三方订单号

	private String thirdChannelRespMsg;		//第三方通道返回信息

	private Integer thirdChannelNotifyFlag;	//第三方通道是否回调成功  1 是  0 否

	private Integer successFlag;			//是否成功  1 是  2 否

	private Integer source;					//来源  1：移动端  2：PC端


}