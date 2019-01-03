package com.cloud.finance.third.ainong.vo;

import lombok.Data;

/**
 * 代付请求返回结果--支付宝
 */
@Data
public class AinongAliPayCashRespData {

    private String respcode;

    private String respmsg;

    private Double txnamt;

    private String merid;

    private String orderid;

    private String queryid;

    private String resultcode;

    private String resultmsg;

}
