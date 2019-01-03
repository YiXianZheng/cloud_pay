package com.cloud.finance.common.vo.cash;

import com.cloud.sysconf.common.dto.ThirdChannelDto;
import lombok.Data;

import java.util.Date;

/**
 * 发起代付需要提交的数据
 * @Auther Toney
 * @Date 2018/9/20 21:18
 * @Description:
 */
@Data
public class CashReqData {

    //前端提交
    private String bankAccount; //银行卡开户人

    private String bankCode;    //银行编码

    private String bankNo;      //银行卡卡号

    private String bin;         //联行号

    private Double amount;      //代付金额

    private String key;         //安全码

    //非必填
    private String subbranch;   //支行名称

    private String province;    //省份

    private String city;        //城市

    private Double fee;         //代付手续费

    //后台传参
    private ThirdChannelDto thirdChannelDto;

}
