package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * 通道银行信息
 * @Auther Toney
 * @Date 2018/9/13 09:46
 * @Description:
 */
@Entity
@Data
@IdClass(ThirdChannelBank.class)
public class ThirdChannelBank extends BasePo {

	@Id
    private String sysBankCode;		//系统银行编码

	@Id
    private String thirdChannelId;	//通道ID

	@Id
    private String channelBankCode;	//通道银行编码

}