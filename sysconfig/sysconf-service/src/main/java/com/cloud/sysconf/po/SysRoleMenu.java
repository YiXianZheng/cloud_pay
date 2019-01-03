package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * 角色菜单关系表
 * @Auther Toney
 * @Date 2018/7/26 10:49
 * @Description:
 */
@Entity
@Data
@IdClass(SysRoleMenu.class)
public class SysRoleMenu extends BasePo {

    @Id
    private String roleId;

    @Id
    private String menuId;

}
