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
public class ShopAccount extends BasePo {

    public static final int STATUS_COMMON = 1;
    public static final int STATUS_FROZEN = 2;

	@Id
	private String sysUserId;

	private Double totalMoney;		//账户累计金额

	private Double usableMoney;		//可用余额

	private Double frozenMoney;		//冻结金额

	private Double rechargeMoney;	//代付总金额（已提现）

	private Integer status;			//账户状态 1：正常  2：冻结

	private String securityCode;	//安全码


}