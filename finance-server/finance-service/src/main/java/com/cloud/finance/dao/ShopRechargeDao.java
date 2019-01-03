package com.cloud.finance.dao;

import com.cloud.finance.common.dto.AccountDto;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import org.apache.ibatis.annotations.Param;

/**
 * ShopRecharge dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface ShopRechargeDao extends BaseMybatisDao<ShopRecharge, String> {

    /**
     * 判断代付订单是否已经存在
     * @param rechargeNo
     * @return
     */
    int checkExist(@Param("rechargeNo") String rechargeNo);


    /**
     * 通过平台代付单号获取代付订单详情
     * @param rechargeNo
     * @return
     */
    ShopRecharge getByRechargeNo(@Param("rechargeNo") String rechargeNo);

    /**
     * 更新代付订单通道信息
     * @param shopRecharge
     */
    void updateThirdInfo(ShopRecharge shopRecharge);

    /**
     * 统计某用户代付总金额
     * @param sysUserId
     * @return
     */
    AccountDto countTotalRecharge(@Param("sysUserId") String sysUserId);

    /**
     * 代付成功 更新代付单
     * @param shopRecharge
     * @return
     */
    int rechargeSuccess(ShopRecharge shopRecharge);

    /**
     * 代付失败 更新代付单
     * @param shopRecharge
     * @return
     */
    int rechargeFail(ShopRecharge shopRecharge);

    /**
     * 更新代付状态
     * @param shopRecharge
     * @return
     */
    int updateRechargeStatus(ShopRecharge shopRecharge);

    /**
     * 更新代付银行信息
     * @param shopRecharge
     * @return
     */
    int updateBankInfo(ShopRecharge shopRecharge);
}
