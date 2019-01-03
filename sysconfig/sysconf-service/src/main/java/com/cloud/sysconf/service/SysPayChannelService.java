package com.cloud.sysconf.service;

import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelInfoDto;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.po.SysPayChannel;

/**
 * 支付通道配置的service
 * @Auther Toney
 * @Date 2018/7/29 17:37
 * @Description:
 */
public interface SysPayChannelService extends BaseMybatisService<SysPayChannel, String> {

    /**
     * 获取可用的支付通道
     * @param headerInfoDto
     * @return
     */
    ReturnVo loadValidPayChannel(HeaderInfoDto headerInfoDto);

    /**
     * 初始化支付通道到Redis
     * @return
     */
    ReturnVo initChannelToRedis();

    /**
     * 加在第三方通道列表
     * @return
     */
    ReturnVo loadChannels();
}
