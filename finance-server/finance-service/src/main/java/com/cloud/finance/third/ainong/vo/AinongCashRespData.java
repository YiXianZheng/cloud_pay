package com.cloud.finance.third.ainong.vo;

import lombok.Data;

/**
 * 代付请求返回结果
 */
@Data
public class AinongCashRespData {

    public static final int TRADE_STATUS_SUCCESS = 1;
    public static final int TRADE_STATUS_DOING = 2;
    public static final int TRADE_STATUS_FAIL = 3;

    private Integer trade_status;    //订单状态 1：成功 2：处理中 3：失败\

    private String billno;          //平台订单号
}
