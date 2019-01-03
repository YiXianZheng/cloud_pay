package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.ThirdPayChannel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 第三方通道费率dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface ThirdPayChannelDao extends BaseMybatisDao<ThirdPayChannel, String> {

    /**
     * 获取第三方通道的费率
     * @param thirdChannel
     * @return
     */
    List<ThirdPayChannel> getByThirdChannel(@Param("thirdChannel") String thirdChannel);

}
