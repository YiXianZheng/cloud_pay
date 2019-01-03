package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * 通道账户信息
 */
@Data
public class ChannelAccountDto extends BaseDto {

	private Double income;		//总入账

	private Double outcome;	    //总出账

	private Double frozen;	    //冻结的

	private Double unfrozen;	//解冻的

}