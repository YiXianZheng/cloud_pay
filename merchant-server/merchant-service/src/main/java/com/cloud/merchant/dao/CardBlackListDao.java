package com.cloud.merchant.dao;

import com.cloud.merchant.po.CardBlackList;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

/**
 * @description: ${description}
 * @author: zyx
 * @create: 2019-05-04 12:35
 **/
public interface CardBlackListDao extends BaseMybatisDao<CardBlackList, String> {

    /**
     * 通过id查询
     * @param id
     * @return
     */
    CardBlackList getById(@Param("id") String id);

    /**
     * 通过id查询
     * @param bankCardHolder
     * @return
     */
    CardBlackList getByHolder(@Param("bankCardHolder") String bankCardHolder);

    /**
     * 删除
     * @param id
     */
    void deleteCard(@Param("id") String id);
}
