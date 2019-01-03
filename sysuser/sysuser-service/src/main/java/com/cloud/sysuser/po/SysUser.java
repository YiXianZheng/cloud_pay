package com.cloud.sysuser.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.*;
import java.util.Date;

/**
 * @Auther Toney
 * @Date 2018/7/6 10:24
 * @Description:
 */
@Data
@Entity
@Table(name = "sys_user")
@EqualsAndHashCode(callSuper=true)
public class SysUser extends BasePo {

    public static final int LOGIN_FLAG_YES = 1;     //可以登陆
    public static final int LOGIN_FLAG_NO = 0;      //不可以登陆

    @Id
    private String id;

    private String company;     //所属公司

    private String department;  //所属部门

    private String loginName;   //登陆名

    private String password;    //密码

    private String no;          //工号

    private String name;        //姓名

    private String email;       //邮箱

    private String phone;       //电话

    private String mobile;      //手机

    private String photo;       //头像

    private String loginIp;     //最后登陆IP

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:SS")
    private Date loginDate;     //最后登陆日期

    private Integer loginFlag;   //是否允许登陆

    private String token;       //token

    /////////////////

    private String roleId;

    private String agentUser;

    private String merchantUser;

    private Integer optStatus;

}
