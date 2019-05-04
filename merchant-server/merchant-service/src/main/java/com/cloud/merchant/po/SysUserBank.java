package com.cloud.merchant.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class SysUserBank extends BasePo {

    @Id
    private String id;

    private String sysUserId;

    private String bankName;

    private String bankCode;

    private String bankBranchName;  // 支行名称

    private String bankCardHolder;

    private String bankCardNo;

    private String bankProvince;

    private String bankCity;

    private String bankBin;         // 联行号

    private Integer cardStatus;

    private Double dailyMoney;

    private Double totalMoney;

    private Integer dailyRecharge;

    private Integer totalRecharge;

    public void initData() {
        this.dailyMoney = 0.0;
        this.totalMoney = 0.0;
        this.dailyRecharge = 0;
        this.totalRecharge = 0;
    }
}
