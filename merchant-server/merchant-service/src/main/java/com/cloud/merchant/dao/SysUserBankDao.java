package com.cloud.merchant.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.merchant.po.SysUserBank;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysUserBankDao extends BaseMybatisDao<SysUserBank, String> {

    /**
     * 通过id查询
     * @param id
     * @return
     */
    SysUserBank getById(@Param("id") String id);

    /**
     * 通过id查询
     * @param sysUserId
     * @return
     */
    List<SysUserBank> getByUserId(@Param("sysUserId") String sysUserId);

    /**
     * 通过卡号查询
     * @param sysUserId
     * @param bankCardNo
     * @return
     */
    SysUserBank getByCardNo(@Param("sysUserId") String sysUserId, @Param("bankCardNo") String bankCardNo);

    /**
     * 更新银行卡信息
     * @param sysUserBank
     */
    void updateInfo(SysUserBank sysUserBank);

    /**
     * 获取联行号
     * @param bankCardNo
     * @return
     */
    SysUserBank getBankBin(@Param("bankCardNo") String bankCardNo);
}
