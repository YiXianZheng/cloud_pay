package com.cloud.sysuser.common.DTO;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 用户应用间通讯的数据传输
 * @Auther Toney
 * @Date 2018/7/6 14:05
 * @Description:
 */
@Data
@EqualsAndHashCode(callSuper=true)
public class SysUserProviderDto extends BaseDto {

    public static final int LOGIN_FLAG_YES = 1;     //可以登陆
    public static final int LOGIN_FLAG_NO = 0;      //不可以登陆

    private String id;

    private String panId;

    private String loginName;

    private String password;

    private String roleType;

    private String name;        //管理员名称

    private String optUser;     //操作人

}
