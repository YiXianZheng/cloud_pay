package com.cloud.finance.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * 冻结订单信息
 * @version 2018-08-13
 */
@Entity
@Data
public class ShopPayFrozen extends BasePo {

    public static final int STATUS_FROZEN = 1;
    public static final int STATUS_UNFROZEN = 2;

	@Id
	private String id;

	//平台相关
	private String sysPayOrderNo;			// 平台支付订单号

    private Date orderCreateDate;           //订单创建时间

	private String merchantUser;			//商户ID

	private Double merchantMoney;			//商户的钱

	private String agentUser;				//代理ID

	private Double agentMoney;				//代理的钱

	private Integer status;					//状态  1 冻结 2 解冻

    private String channelId;               //通道ID


}