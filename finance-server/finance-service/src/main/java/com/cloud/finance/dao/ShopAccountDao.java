package com.cloud.finance.dao;

import com.cloud.finance.common.dto.AccountInfoDto;
import com.cloud.finance.po.ShopAccount;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

/**
 * ShopRecharge dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface ShopAccountDao extends BaseMybatisDao<ShopAccount, String> {

    /**
     * 通过用户ID获取账户信息
     * @param sysUserId
     * @return
     */
    ShopAccount getByUserId(@Param("sysUserId") String sysUserId);

    /**
     * 冻结或解冻账户余额
     * @param sysUserId
     * @param frozenMoney  正数冻结，负数解冻
     * @return
     */
    int frozenAccount(@Param("sysUserId") String sysUserId, @Param("frozenMoney") Double frozenMoney);

    /**
     * 更新已代付总额,并更新冻结金额（代付到账更新专用）
     * @param sysUserId
     * @param rechargeMoney
     * @return
     */
    int updateRecharge(@Param("sysUserId") String sysUserId, @Param("rechargeMoney") Double rechargeMoney);

    /**
     * 获取平台账户信息
     * @param sysUserId
     * @return
     */
    AccountInfoDto getAccount(String sysUserId);

    /**
     * 更新账户
     * @param shopAccount
     */
    void updateAccountInfo(ShopAccount shopAccount);

    /**
     * 通过商户号获取账户信息
     * @param merchantCode
     * @return
     */
    ShopAccount getAccountByMerchantCode(@Param("merchantCode") String merchantCode);

    void updateSecurityCode(@Param("sysUserId") String sysUserId, @Param("securityCode") String securityCode);
}
