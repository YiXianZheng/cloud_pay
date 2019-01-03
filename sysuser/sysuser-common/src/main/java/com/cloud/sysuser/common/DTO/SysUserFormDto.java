package com.cloud.sysuser.common.DTO;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


/**
 * @Auther Toney
 * @Date 2018/7/6 14:05
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class SysUserFormDto extends BaseDto {

    private String id;

    private String loginName;

    private String password;

    private String name;        //管理员名称

    private String no;          //工号

    private String email;       //邮箱

    private String phone;       //电话

    private String mobile;      //手机

    private String photo;       //头像

    private Integer loginFlag;   //是否允许登陆

    private String roleId;      //角色ID

    private String roleType;

}
