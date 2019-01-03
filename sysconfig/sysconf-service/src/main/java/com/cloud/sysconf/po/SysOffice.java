package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 组织结构(公司部门)
 * @Auther Toney
 * @Date 2018/7/26 09:46
 * @Description:系统的一些可配默认参数
 */
@Entity
@Data
public class SysOffice extends BasePo {

    public static final int TYPE_COMPANY = 1;
    public static final int TYPE_DEPARTMENT = 2;

    public static final int USABLE_YES = 1;
    public static final int USABLE_NO = 0;

    @Id
    private String id;

    private String parentId;    //父级ID

    private String name;        //组织名称

    private Integer type;       //机构类型 1: 公司  2：部门

    private String address;     //联系地址

    private String master;      //负责人

    private String phone;       //联系电话

    private String fax;         //传真

    private String email;       //邮箱

    private Integer usable;     //是否可用

    private int sort;           //排序

}
