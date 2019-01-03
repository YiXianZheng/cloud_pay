package com.cloud.sysconf.common.dto;

import lombok.Data;

/**
 * @Auther Toney
 * @Date 2018/7/29 21:29
 * @Description:
 */
@Data
public class PayChannelDto {

    private String id;

    private String name;        //中文名称

    private String enname;      //英文名称

    private String code;        //支付通道编码

    private Double costRate;    //成本费率
}
