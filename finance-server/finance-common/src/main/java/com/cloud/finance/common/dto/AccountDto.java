package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

import java.util.Date;

/**
 * 通道账户余额信息
 */
@Data
public class AccountDto extends BaseDto {

	private Double amount;			//可用金额

	private Double frozenAmount;	//冻结金额


}