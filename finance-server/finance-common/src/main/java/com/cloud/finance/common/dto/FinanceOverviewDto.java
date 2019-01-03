package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

import java.util.Date;

/**
 * 财务概览
 */
@Data
public class FinanceOverviewDto extends BaseDto {

//	totalMoney;				//总交易额
//	dailyMoney;				//今日交易额
//	totalOrder;				//订单总数
//	dailyOrder;				//今日订单数
//	totalSuccessOrder;		//成功订单总数
//	dailySuccessOrder;		//今日成功订单数
//	totalSuccessOrderRate;	//总订单成功率
//	dailySuccessOrderRate;	//今日订单成功率
//	totalCharge;			//总利润
//	dailyCharge;			//今日利润
//	totalPaid;				//总代付笔数
//	dailyPaid;				//日代付笔数
//	totalSuccessRate;		//总代付成功率
//	dailySuccessRate;		//日代付成功率
//	totalRiskControlOrder;	//总风控订单数
//	dailyRiskControlOrder;	//日风控订单数

	private String userCode;				//商户号或代理号或平台（"000000"）

	private Integer userType;				//用户类型  1平台  2代理  3商户

	private Double totalMoney;				//总交易额

	private Integer totalOrder;				//订单总数

	private Integer totalSuccessOrder;		//成功订单总数

	private Double totalCharge;				//总利润

	private Integer totalPaid;				//总代付笔数

	private Integer totalSuccessPaid;		//总成功代付笔数

	private Integer totalRiskControlOrder;	//总风控订单数


}