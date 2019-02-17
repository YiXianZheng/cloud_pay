package com.cloud.finance.third.ainong.vo;

import lombok.Data;

/**
 * 查询通道余额返回的数据
 * @Auther Toney
 * @Date 2018/9/20 21:18
 * @Description:
 */
@Data
public class AccountQueryData {

    private String code;          //状态

    private String msg;             //消息

    private RespData data;

    @Data
    private static class RespData{
        private String appid;       //appid

        private String mch_id;      //商户号

        private Double amount;         //当前可用余额

        private Double freeze_amount;  //冻结金额
    }
}
