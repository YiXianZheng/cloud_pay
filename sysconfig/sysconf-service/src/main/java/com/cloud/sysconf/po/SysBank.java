package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 系统银行列表
 * @Auther Toney
 * @Date 2018/7/26 09:46
 * @Description:
 */
@Entity
@Data
public class SysBank extends BasePo {

    @Id
    private String id;

    private String bankCode;    //银行编码

    private String bankName;    //银行名称

    private String icon;        //银行图标

    private int sort;           //排序

    private Integer usable;     //是否可用  1 是  2 否

}
