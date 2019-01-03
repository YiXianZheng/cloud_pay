package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.SysOffice;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 组织机构的dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface SysOfficeDao extends BaseMybatisDao<SysOffice, String> {

    List<SysOffice> querySysOffice();

    /**
     * 判断公司或部门名称是否存在
     * @param name
     * @param type
     * @return
     */
    int checkExist(@Param("name") String name, @Param("type") Integer type);
}
