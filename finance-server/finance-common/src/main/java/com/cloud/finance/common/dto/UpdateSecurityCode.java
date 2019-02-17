package com.cloud.finance.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class UpdateSecurityCode extends BaseDto {

    private String oldCode;

    private String newCode;

    private String reNewCode;
}
