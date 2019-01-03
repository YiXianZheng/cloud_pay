package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * 账户信息
 */
@Data
public class AccountInfoDto extends BaseDto {

	private Double totalMoney;		//可用金额

	private Double usableMoney;	    //可用金额

	private Double frozenMoney;	    //冻结金额

}