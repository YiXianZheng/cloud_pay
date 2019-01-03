package com.cloud.finance.third.shtd1.vo;

import lombok.Data;

/**
 * 上海通道1 支付通知参数
 */
@Data
public class AliPayRespData {

    private String orderId;         //通道订单号

    private String amount;          //金额

    private String pay_number;      //系统订单号

    private String sign;            //验签

    private String respCode;        //返回码  "0000" 交易成功

    private String respInfo;        //返回码描述
}
