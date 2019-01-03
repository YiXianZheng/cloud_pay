package com.cloud.sysconf.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysconf.po.ThirdChannel;

import java.util.List;
import java.util.Map;

/**
 * 第三方通道dao
 * @Auther Toney
 * @Date 2018/7/19 11:48
 * @Description:
 */
public interface ThirdChannelDao extends BaseMybatisDao<ThirdChannel, String> {

    /**
     * 获取所有可用的第三方通道
     * @return
     */
    List<ThirdChannel> getThirdChannel();

    /**
     * 通过通道类型获取可用第三方通道
     * @param channelType
     * @return
     */
    List<Map<String,Object>> getByType(Integer channelType);
}
