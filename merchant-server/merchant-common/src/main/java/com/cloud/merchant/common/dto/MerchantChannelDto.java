package com.cloud.merchant.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @Auther Toney
 * @Date 2018/7/29 17:00
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class MerchantChannelDto extends BaseDto {


    private String merchantUser;   //商户ID

    private String channelCode;  //接口通道code

    private Double agentRate;   //接口费率

    private Integer usable;     //是否可用

}
