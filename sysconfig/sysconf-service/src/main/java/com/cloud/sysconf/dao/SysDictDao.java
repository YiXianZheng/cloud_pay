package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.SysDict;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 系统字典dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface SysDictDao extends BaseMybatisDao<SysDict, String> {

    SysDict findByCode(@Param("code") String code);

    List<SysDict> findAll();
}
