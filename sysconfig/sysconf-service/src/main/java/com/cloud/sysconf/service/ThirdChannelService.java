package com.cloud.sysconf.service;

import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelInfoDto;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.po.ThirdChannel;

import java.util.List;

/**
 * 第三方通道的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface ThirdChannelService extends BaseMybatisService<ThirdChannel, String> {

    /**
     * 刷新第三方通道的Redis缓存
     * @return
     */
    ReturnVo refreshRedis();

    /**
     * 获取第三方通道列表
     * @return
     */
    List<ThirdChannelInfoDto> getThirdChannel();

    /**
     * 更新通道信息
     * @param headerInfoDto
     * @param thirdChannelInfoDto
     * @return
     */
    ReturnVo thirdChannelInfoDto(HeaderInfoDto headerInfoDto, ThirdChannelInfoDto thirdChannelInfoDto);

    /**
     * 获取有效的第三方通道列表
     * @param channelType
     * @return
     */
    ReturnVo loadValidChannel(Integer channelType);
}
