package com.cloud.finance.common.vo.pay.finance;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * 财务概览Vo
 */
@Data
public class OverviewVo extends BaseDto {

	private String userCode;				//商户号或代理号或平台（"000000"）

	private String channelId;				//通道ID

	private String channelName;				//通道名称

 	private Double totalMoney;				//总交易额

	private Double dailyMoney;				//今日交易额

	private Integer totalOrder;				//订单总数

	private Integer dailyOrder;				//今日订单数

	private Integer totalSuccessOrder;		//成功订单总数

	private Integer dailySuccessOrder;		//今日成功订单数

	private Double totalSuccessOrderRate;	//总订单成功率

	private Double dailySuccessOrderRate;	//今日订单成功率

	private Double totalCharge;				//总利润

	private Double dailyCharge;				//今日利润

	private Double totalPaidMoney;			//总代付金额

	private Double dailyPaidMoney;			//今日代付金额

	private Integer totalPaid;				//总代付笔数

	private Integer dailyPaid;				//日代付笔数

	private Double totalSuccessRate;		//总代付成功率

	private Double dailySuccessRate;		//日代付成功率

	private Integer totalRiskControlOrder;	//总风控订单数

	private Integer dailyRiskControlOrder;	//日风控订单数

}