package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * 通道财务统计
 */
@Data
public class ChannelSummaryDto extends BaseDto {



	private String channelId;				//通道ID

	private Double totalMoney;				//总交易额

	private Double totalCharge;				//总利润

	private Integer totalOrder;				//订单总数

	private Integer totalSuccessOrder;		//成功订单总数

	private Integer totalPaid;				//总代付笔数

	private Integer totalSuccessPaid;		//总成功代付笔数

	private Integer totalRiskControlOrder;	//总风控订单数

	private Double totalPaidMoney;			//总代付金额


}