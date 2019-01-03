package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * 商户通道每日财务统计
 */
@Data
public class MerchantChannelDto extends BaseDto {

    private String merchantCode;            // 商户编号

    private String merchantName;            // 商户名称

    private String channelId;               // 通道编号

    private Double channelDailyMoney;       // 通道交易金额

    private Double channelSuccessMoney;     // 通道成功交易金额

    private Double merchantCostMoney;       // 商户通道手续费

    private Double merchantCostRate;        // 商户通道手续费率

    private String summaryTime;             // 累计时间
}
