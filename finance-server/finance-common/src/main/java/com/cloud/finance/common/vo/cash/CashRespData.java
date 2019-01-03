package com.cloud.finance.common.vo.cash;

import lombok.Data;

/**
 * 代付返回的数据
 * @Auther Toney
 * @Date 2018/9/20 21:18
 * @Description:
 */
@Data
public class CashRespData {

    public static final String STATUS_SUCCESS = "0000";    //成功

    public static final String STATUS_DOING = "0001";       //银行处理中

    public static final String STATUS_ERROR = "0002";      //失败

    private String status;          //状态

    private String msg;             //消息

}
