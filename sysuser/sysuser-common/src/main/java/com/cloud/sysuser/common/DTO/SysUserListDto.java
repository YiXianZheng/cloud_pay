package com.cloud.sysuser.common.DTO;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Auther Toney
 * @Date 2018/7/26 11:33
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class SysUserListDto extends BaseDto {

    private String id;

    private String no;              //工号

    private String companyName;     //公司名

    private String departName;      //部门

    private String loginName;       //登录名

    private String name;            //姓名

    private String email;           //邮箱

    private String phone;           //电话

    private String mobile;          //手机号
}
