package com.cloud.finance.third.ainong.vo;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class GatewayPayReq extends BaseHeadReqData {

    /**
     * 支付金额
     */
    @NotBlank(message = "支付金额不能为空")
    private String payAmount;        //n	20L	M	单位分

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    private String productName;        //ans	64L	M

    /**
     * 商品描述
     */
    @NotBlank(message = "商品描述不能为空")
    private String productDesc;        //ans	128L	M

    /**
     * 回调地址
     */
    @NotBlank(message = "回调地址不能为空")
    private String callBackUrl;        //ans	255L	M	支付异步通知地址

    /**
     * 前端跳转地址
     */
    @NotBlank(message = "前端跳转地址不能为空")
    private String frontUrl;        //ans	255L	M	前端跳转地址

    /**
     * 前端渠道类型 01-互联网  02-移动端
     */
    @NotBlank(message = "前端类型不能为空")
    private String channelType;        //ans	255L	M	前端跳转地址

    /**
     * 银行编码
     */
    private String bankCode;        //ans	32L	M

    /**
     * 身份证号
     */
    private String certNo;    //	an	18L	M

    /**
     * 银行卡卡号
     */
    private String cardNo;        //ans	32L	M

    /**
     * 银行卡户名
     */
    private String accountName;        //ans	32L	O

}
