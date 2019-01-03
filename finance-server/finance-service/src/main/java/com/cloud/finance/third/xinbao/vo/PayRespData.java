package com.cloud.finance.third.xinbao.vo;

import lombok.Data;

/**
 * 支付请求返回结果
 */
@Data
public class PayRespData {

    //成功标志  表示接口调用是否成功。true：成功false：失败
    private String success;

    //响应编码
    private String resultCode;

    //订单确认支付地址
    private String payUrl;

    //交易流水号
    private String paymentNo;

    //协议类型
    private String protocol;

    //通道 服务名称
    private String service;

    //签名
    private String sign;

    private String signType;

    //商户ID
    private String partnerId;

    //请求号
    private String requestNo;

    //响应消息
    private String resultMessage;

    //服务版本号  与 service对应
    private String version;

}
