package com.cloud.agent.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 支付通道费率配置
 * @Auther Toney
 * @Date 2018/7/29 21:51
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class PayChannelRateDto extends BaseDto {

    private String code;        //接口通道code

    private Double rate;        //接口通道费率

    private Integer usable;     //是否启用
}
