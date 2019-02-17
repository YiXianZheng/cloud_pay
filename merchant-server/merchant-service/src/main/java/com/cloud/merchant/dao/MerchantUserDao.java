package com.cloud.merchant.dao;

import com.cloud.merchant.po.MerchantUser;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

/**
 * 商户用户的dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface MerchantUserDao extends BaseMybatisDao<MerchantUser, String> {

    /**
     * 更新商户状态
     * @param merchantUser
     */
    int updateStatus(MerchantUser merchantUser);

    /**
     * 逻辑删除商户
     * @param merchantUser
     * @return
     */
    int delMerchant(MerchantUser merchantUser);

    /**
     * 通过商户号获取商户信息
     * @param merchantCode
     * @return
     */
    MerchantUser getByCode(@Param("merchantCode") String merchantCode);

    /**
     * 通过用户编号获取商户信息
     * @param sysUserId
     * @return
     */
    MerchantUser getByUserId(@Param("sysUserId") String sysUserId);

    /**
     * 通过商户名称获取商户详情
     * @param name
     * @return
     */
    MerchantUser getByName(@Param("merchantName") String name);
}
