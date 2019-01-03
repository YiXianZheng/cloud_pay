package com.cloud.sysconf.service;

import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.po.SysBank;

import java.util.List;
import java.util.Map;

/**
 * 系统银行的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface SysBankService extends BaseMybatisService<SysBank, String> {

    String getByChannelAndSysCode(String sysBankCode, String thirdChannelId);

    /**
     * 获取系统银行下拉列表
     * @return
     */
    List<Map<String, String>> getSysSelectList();

    /**
     * 通过银行编码获取银行名称
     * @param bankCode
     * @return
     */
    String getBankNameByCode(String bankCode);
}
