package com.cloud.finance.third.ainong.vo;

import lombok.Data;

@Data
public class PayForReq extends BaseHeadReqData {

    /**
     * 支付金额
     */
    private String amount;        //n	20L	M	单位分

    /**
     * 代付类型
     */
    private String payForType;        //ans	255L	M	前端跳转地址

    /**
     * 支行联行号
     */
    private String bankChannelNo;        //ans	255L	M	前端跳转地址

    /**
     * 银行名称
     */
    private String bankName;        //ans	32L	M

    /**
     * 银行账户类型
     */
    private String bankAccountType;    //	an	18L	M

    /**
     * 银行卡卡号
     */
    private String bankAccountNo;        //ans	32L	M

    /**
     * 银行卡户名
     */
    private String bankAccountName;        //ans	32L	O
}
