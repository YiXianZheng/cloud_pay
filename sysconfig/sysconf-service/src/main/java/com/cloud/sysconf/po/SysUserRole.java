package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

/**
 * 用户角色关系表
 * @Auther Toney
 * @Date 2018/7/26 10:53
 * @Description:
 */
@Entity
@Data
@IdClass(SysUserRole.class)
public class SysUserRole extends BasePo {

    @Id
    private String userId;

    @Id
    private String roleId;
}
