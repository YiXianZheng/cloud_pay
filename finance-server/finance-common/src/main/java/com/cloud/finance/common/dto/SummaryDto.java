package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * 商户汇总信息
 */
@Data
public class SummaryDto extends BaseDto {

	private String merchantCode;

	private Double totalMoney;

	private Double totalSuccessMoney;

	private Double merchantCostRate;

	private Double merchantCostMoney;

	private String summaryTime;


}