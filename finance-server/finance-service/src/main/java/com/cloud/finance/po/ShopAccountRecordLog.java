package com.cloud.finance.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * 交易流水历史库
 * @version 2018-08-13
 */
@Entity
@Data
public class ShopAccountRecordLog extends BasePo {

	@Id
	private String id;

	private Date moveDate;			//迁移时间

	private String sysUserId;		//用户ID

	private Integer type;			//账变类型 1：支付入账  2：代付出账  3：风控冻结

	private String channelId;		//通道ID

	private String unionOrderNo;	//关联订单或者代付单号

	private Double totalAmount;		//总流水金额

	private Double poundage;		//手续费

	private Double ownAmount;		//帐变

	private Integer status;			//状态  1：账变处理中  2：账变完成 3：账变失败

	private Date completeDate;		//完成时间

}