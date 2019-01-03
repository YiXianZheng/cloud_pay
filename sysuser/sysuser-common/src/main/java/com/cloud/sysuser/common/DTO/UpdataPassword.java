package com.cloud.sysuser.common.DTO;


import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class UpdataPassword extends BaseDto {

    private String oldpassword;

    private String newpassword;

    private String reinputspassword;
}
