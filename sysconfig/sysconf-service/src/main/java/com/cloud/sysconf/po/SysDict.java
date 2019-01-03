package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * 系统字典
 * @Auther Toney
 * @Date 2018/7/26 09:46
 * @Description:系统的一些可配默认参数
 */
@Entity
@Data
public class SysDict extends BasePo {

    @Id
    private String code;        //字典编码

    private String parentId;    //父级ID

    private String value;       //数据值

    private String label;       //字典名称

    private String type;        //字典分类

    private String description; //字典描述

    private int sort;           //排序

}
