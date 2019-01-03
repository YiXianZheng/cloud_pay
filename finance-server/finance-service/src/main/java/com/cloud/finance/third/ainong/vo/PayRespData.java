package com.cloud.finance.third.ainong.vo;

import lombok.Data;

/**
 * 支付请求返回结果
 */
@Data
public class PayRespData {

    private String code;

    private String msg;

    private RespData data;


    @Data
    public static class RespData{

        private String mch_id;

        private String billno;

        private String out_trade_no;

        private String total_fee;

        private String trade_status;

        private String pay_url;
    }
}
