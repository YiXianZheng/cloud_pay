package com.cloud.finance.service;

import com.cloud.finance.common.dto.UpdateSecurityCode;
import com.cloud.finance.po.ShopAccount;
import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.vo.ReturnVo;

/**
 * 账户的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface ShopAccountService extends BaseMybatisService<ShopAccount, String> {

    /**
     * 通过用户ID获取账户信息
     * @param sysUserId
     * @return
     */
    ShopAccount getByUserId(String sysUserId);

    /**
     * 初始化账户信息
     * @param headerInfoDto
     * @return
     */
    ShopAccount initAccount(HeaderInfoDto headerInfoDto);

    /**
     * 冻结或解冻账户余额
     * @param userId
     * @param frozenMoney    整数冻结，负数解冻
     * @return
     */
    int frozenAccount(String userId, Double frozenMoney);

    /**
     * 更新已代付总额,并更新冻结金额（代付到账更新专用）
     * @param rechargeMoney
     * @return
     */
    int updateRecharge(String userId, Double rechargeMoney);

    /**
     * 平台账户信息
     * @param headerInfoDto
     * @return
     */
    ReturnVo getAccount(HeaderInfoDto headerInfoDto);

    /**
     * 通过商户号获取商户信息
     * @param merchangCode
     * @return
     */
    ShopAccount getAccountByMerchantCode(String merchangCode);

    /**
     * 更新账户信息
     * @param shopAccount
     */
    void updateAccountInfo(ShopAccount shopAccount);

    /**
     * 修改商户代付安全码
     * @param securityCode
     * @param curUserId
     * @return
     */
    ReturnVo updateSecurityCode(UpdateSecurityCode securityCode, String curUserId);

    /**
     * 同步商户余额到数据库
     * @param userId
     * @param merchantUser
     */
    void loadAccount(String userId, String merchantUser);
}
