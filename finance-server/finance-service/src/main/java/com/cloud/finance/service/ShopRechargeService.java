package com.cloud.finance.service;

import com.cloud.finance.common.dto.AccountDto;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ResultVo;
import com.cloud.sysconf.common.vo.ReturnVo;

/**
 * ShopRecharge的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface ShopRechargeService extends BaseMybatisService<ShopRecharge, String> {

    /**
     * 判断是否可以申请代付（提现）
     * @param cashReqData
     * @param headerInfoDto
     * @return
     */
    ResultVo checkAccount(CashReqData cashReqData, HeaderInfoDto headerInfoDto);

    /**
     * 通过代付单号获取代付订单
     * @param rechargeNo
     * @return
     */
    ShopRecharge getByRechargeNo(String rechargeNo);

    /**
     * 统计某用户的代付总金额
     * @param sysUserId
     * @return
     */
    AccountDto countTotalRecharge(String sysUserId);

    /**
     * 分页获取平台代付单
     * @param pageQuery
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     * 代付成功 更新代付单
     * @param shopRecharge
     * @return
     */
    int rechargeSuccess(ShopRecharge shopRecharge);

    /**
     * 更新第三方通道信息
     * @param shopRecharge
     * @return
     */
    void updateThirdInfo(ShopRecharge shopRecharge);

    /**
     * 代付失败  更新订单
     * @param shopRecharge
     * @return
     */
    int rechargeFail(ShopRecharge shopRecharge);

    /**
     * API判断是否可以申请代付
     * @param merCode
     * @param amount
     * @param key
     * @param bankAccount
     * @param bankCode
     * @param bankNo
     * @param bankSubbranch
     * @param bin
     * @param province
     * @param city
     * @return
     */
    ResultVo checkAccountForAPI(String merCode, Double amount, String key, String bankAccount, String bankCode, String bankNo,
                                String bankSubbranch, String bin, String province, String city);

    /**
     * 创建代付单
     * @param shopRecharge
     * @return
     */
    ShopRecharge apiCreate(ShopRecharge shopRecharge);

    /**
     * 更新代付单状态 仅限驳回时调用
     * @param shopRecharge
     */
    ReturnVo updateRechargeStatus(ShopRecharge shopRecharge);

    /**
     * 更新代付银行信息
     * @param shopRecharge
     * @return
     */
    ReturnVo updateBankInfo(ShopRecharge shopRecharge);
}
