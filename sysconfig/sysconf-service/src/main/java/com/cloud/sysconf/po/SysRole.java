package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 系统角色
 * @Auther Toney
 * @Date 2018/7/26 10:44
 * @Description:
 */
@Entity
@Data
public class SysRole extends BasePo {

    @Id
    private String id;

    private String officeId;    //归属机构

    private String name;        //角色名称

    private String enname;      //角色英文名

    private String roleType;    //角色类型

    private Integer usable;    //是否可用

}
