package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 系统菜单
 * @Auther Toney
 * @Date 2018/7/26 09:46
 * @Description:
 */
@Entity
@Data
public class SysMenu extends BasePo {

    @Id
    private String id;

    private String parentId;    //父级ID

    private String name;        //名称

    private String sort;        //排序

    private String path;        //链接

    private String icon;        //图标

    private Integer isShow;     //是否在菜单中显示

    public SysMenu(){
        super();
    }

}
