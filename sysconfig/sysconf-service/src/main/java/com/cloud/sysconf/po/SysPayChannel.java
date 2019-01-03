package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 支付通道配置
 * @Auther Toney
 * @Date 2018/7/29 00:14
 * @Description:
 */
@Data
@Entity
public class SysPayChannel extends BasePo {

    public static final int USABLE_YES = 1;
    public static final int USABLE_NO = 0;

    @Id
    private String id;

    private String panId;

    private String name;        //中文名称

    private String enname;      //英文名称

    private String code;        //支付通道编码

    private Double costRate;    //成本费率

    private Integer usable;     //是否启用
}
