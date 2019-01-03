package com.cloud.finance.dao;

import com.cloud.finance.po.ShopPayFrozen;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

/**
 * ShopPayFrozen dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface ShopPayFrozenDao extends BaseMybatisDao<ShopPayFrozen, String> {

    /**
     * 统计冻结金额
     * @param merchantUser
     * @param agentUser
     * @return
     */
    Double countFrozen(@Param("merchantUser") String merchantUser, @Param("agentUser") String agentUser);

    /**
     * 更新状态
     * @param shopPayFrozen
     * @return
     */
    int updateStatus(ShopPayFrozen shopPayFrozen);

}
