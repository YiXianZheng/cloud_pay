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
public class SysUserInfoDto extends BaseDto {

    private String id;

    private String panId;

    private String loginName;

    private String no;

    private String name;

    private String email;

    private String phone;

    private String mobile;
}
