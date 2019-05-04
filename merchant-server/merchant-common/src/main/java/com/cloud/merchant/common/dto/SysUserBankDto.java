package com.cloud.merchant.common.dto;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

@Data
public class SysUserBankDto extends BasePo {

    private String id;

    private String sysUserId;

    private String bankName;

    private String bankBranchName;

    private String bankCardHolder;

    private String bankCardNo;

    private String bankProvince;

    private String bankCity;

    private String bankBin;
}
