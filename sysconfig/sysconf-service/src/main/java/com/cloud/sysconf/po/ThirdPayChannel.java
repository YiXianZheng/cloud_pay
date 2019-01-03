package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * 商户接口通道费率
 * @Auther Toney
 * @Date 2018/7/31 11:49
 * @Description:
 */
@Entity
@Data
@IdClass(ThirdPayChannel.class)
public class ThirdPayChannel extends BasePo {

    public static final int USABLE_YES = 1;
    public static final int USABLE_NO = 0;

    @Id
    private String thirdChannel;  //第三方通道ID

    @Id
    private String channelCode;   //接口通道code

    private Double channelRate;   //接口通道费率

    private Integer usable;       //是否可用

}
