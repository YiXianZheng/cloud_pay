package com.cloud.merchant.service;

import com.cloud.merchant.common.dto.SysUserBankDto;
import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.merchant.po.SysUserBank;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ReturnVo;

public interface SysUserBankService extends BaseMybatisService<SysUserBank, String> {

    /**
     * 绑定银行卡
     * @param sysUserBankDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo addBankCard(SysUserBankDto sysUserBankDto, HeaderInfoDto headerInfoDto);

    /**
     * 分页银行卡列表
     * @param pageQuery  分页条件
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     * 根据银行卡号获取信息
     * @param sysUserId
     * @param bankCardNo
     * @return
     */
    ReturnVo getByCardNo(String sysUserId, String bankCardNo);

    /**
     * 统计银行卡下发数据
     * @param userId
     */
    void summaryPaid(String userId);
}
