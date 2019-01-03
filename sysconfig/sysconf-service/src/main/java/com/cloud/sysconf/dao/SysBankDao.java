package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.SysBank;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统银行的dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface SysBankDao extends BaseMybatisDao<SysBank, String> {

    /**
     * 获取所有可用银行
     * @return
     */
    List<SysBank> getUsable();

    /**
     * 通过银行编码获取银行信息
     * @param bankCode
     * @return
     */
    SysBank getByCode(@Param("bankCode")String bankCode);
}
