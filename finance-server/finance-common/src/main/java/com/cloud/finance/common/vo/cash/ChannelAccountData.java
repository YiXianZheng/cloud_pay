package com.cloud.finance.common.vo.cash;

import lombok.Data;

/**
 * 查询通道余额返回的数据
 * @Auther Toney
 * @Date 2018/9/20 21:18
 * @Description:
 */
@Data
public class ChannelAccountData {

    public static final String STATUS_SUCCESS = "0000";    //成功

    public static final String STATUS_ERROR = "0001";      //失败

    private String status;          //状态

    private String msg;             //消息

    private Double amount;              //当前可用余额

    private Double frozenAmount;       //冻结金额

}
