package com.cloud.sysconf.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * 通道信息
 */
@Data
public class ThirdChannelInfoDto extends BaseDto {


    private String id;						//id
    private String channelName;				//通道名称  不可修改
    private String channelCode;				//通道编码  不可修改
    private Integer channelType;			//通道类型  1:充值  2：代付  不可修改

    private Double payDayMax;				//每日交易最高限额
    private Double payPerMax;				//单笔最高限额
    private Double payPerMin;				//单笔最低

    private Integer routeWeight;			//权重
    private Integer routePayStatus;			//收银开启状态
    private Integer routeCashStatus;		//代付开启状态

    private Integer openRandom;			//是否开启随机数风控
    private int randomMin;			//随机数最小值
    private int randomMax;			//随机数最大值

}
