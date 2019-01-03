package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.ThirdChannel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 第三方通道银行dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface ThirdChannelBankDao extends BaseMybatisDao<ThirdChannel, String> {

    /**
     * 通过系统银行编码和通道ID获取通道银行的编码
     * @param sysBankCode
     * @param thirdChannelId
     * @return
     */
    String getByChannelAndSysCode(@Param("sysBankCode") String sysBankCode, @Param("thirdChannelId") String thirdChannelId);

}
