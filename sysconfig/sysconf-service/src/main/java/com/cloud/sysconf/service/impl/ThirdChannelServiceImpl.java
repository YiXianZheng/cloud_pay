package com.cloud.sysconf.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.dto.ThirdChannelInfoDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.MyBeanUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.dao.ThirdChannelDao;
import com.cloud.sysconf.dao.ThirdPayChannelDao;
import com.cloud.sysconf.po.*;
import com.cloud.sysconf.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
@Service
public class ThirdChannelServiceImpl extends BaseMybatisServiceImpl<ThirdChannel, String, ThirdChannelDao> implements ThirdChannelService {

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ThirdChannelDao thirdChannelDao;
    @Autowired
    private ThirdPayChannelDao thirdPayChannelDao;

    @Override
    @PostConstruct //仅在项目启动后运行一次
    public ReturnVo refreshRedis() {
        try {
            logger.info("==============  begin init third channel to redis  ===============");
            List<ThirdChannel> list = thirdChannelDao.getThirdChannel();
            if(list.size() ==0 ) return ReturnVo.returnSuccess();
            for (ThirdChannel channel : list
                    ) {
                ThirdChannelDto thirdChannelDto = new ThirdChannelDto();
                BeanUtils.copyProperties(channel, thirdChannelDto);
                List<ThirdPayChannel> rates = thirdPayChannelDao.getByThirdChannel(channel.getId());
                for (ThirdPayChannel rate: rates
                     ) {
                    if("ali_qrcode".equals(rate.getChannelCode())) {
                        thirdChannelDto.setAli_qrcode(rate.getChannelRate());
                    }else if("ali_h5_wake".equals(rate.getChannelCode())){
                        thirdChannelDto.setAli_h5_wake(rate.getChannelRate());
                    }else if("ali_self_wap".equals(rate.getChannelCode())){
                        thirdChannelDto.setAli_self_wap(rate.getChannelRate());
                    }else if("wx_qrcode".equals(rate.getChannelCode())){
                        thirdChannelDto.setWx_qrcode(rate.getChannelRate());
                    }else if("wx_self_wap".equals(rate.getChannelCode())){
                        thirdChannelDto.setWx_self_wap(rate.getChannelRate());
                    }else if("wx_h5_wake".equals(rate.getChannelCode())){
                        thirdChannelDto.setWx_h5_wake(rate.getChannelRate());
                    }else if("jd_qrcode".equals(rate.getChannelCode())){
                        thirdChannelDto.setJd_qrcode(rate.getChannelRate());
                    }else if("jd_h5_wake".equals(rate.getChannelCode())){
                        thirdChannelDto.setJd_h5_wake(rate.getChannelRate());
                    }else if("jd_self".equals(rate.getChannelCode())){
                        thirdChannelDto.setJd_self_wap(rate.getChannelRate());
                    }else if("qq_h5_wake".equals(rate.getChannelCode())){
                        thirdChannelDto.setQq_h5_wake(rate.getChannelRate());
                    }else if("qq_self_wap".equals(rate.getChannelCode())){
                        thirdChannelDto.setQq_self_wap(rate.getChannelRate());
                    }else if("qq_qrcode".equals(rate.getChannelCode())){
                        thirdChannelDto.setQq_qrcode(rate.getChannelRate());
                    }else if("gate_qrcode".equals(rate.getChannelCode())){
                        thirdChannelDto.setGate_qrcode(rate.getChannelRate());
                    }else if("gate_web_direct".equals(rate.getChannelCode())){
                        thirdChannelDto.setGate_web_direct(rate.getChannelRate());
                    }else if("gate_web_syt".equals(rate.getChannelCode())){
                        thirdChannelDto.setGate_web_syt(rate.getChannelRate());
                    }else if("gate_h5".equals(rate.getChannelCode())){
                        thirdChannelDto.setGate_h5(rate.getChannelRate());
                    }else if("syt_all_in".equals(rate.getChannelCode())){
                        thirdChannelDto.setSyt_all_in(rate.getChannelRate());
                    }
                }

                redisClient.SetHsetJedis(RedisConfig.THIRD_PAY_CHANNEL, channel.getId(),MyBeanUtil.transBean2Map2(thirdChannelDto));


            }
            logger.info("==============  success init third channel to redis  ===============");
            return ReturnVo.returnSuccess();
        }catch (Exception e){
            logger.info("==============  fail to init third channel to redis  ===============");
            return ReturnVo.returnFail();
        }
    }

    @Override
    public List<ThirdChannelInfoDto> getThirdChannel() {
        List<ThirdChannelInfoDto> res = new ArrayList<>();
        List<ThirdChannel> list = thirdChannelDao.getThirdChannel();
        for (ThirdChannel channel : list
                ) {
            ThirdChannelInfoDto thirdChannelInfoDto = new ThirdChannelInfoDto();
            BeanUtils.copyProperties(channel, thirdChannelInfoDto);
            res.add(thirdChannelInfoDto);
        }
        return res;
    }

    @Override
    public ReturnVo thirdChannelInfoDto(HeaderInfoDto headerInfoDto, ThirdChannelInfoDto thirdChannelInfoDto) {
        try {
            ThirdChannel thirdChannel = thirdChannelDao.getById(thirdChannelInfoDto.getId());
            if(thirdChannel != null){
                thirdChannel.setPayPerMax(thirdChannelInfoDto.getPayPerMax());
                thirdChannel.setPayPerMin(thirdChannelInfoDto.getPayPerMin());
                thirdChannel.setRoutePayStatus(thirdChannelInfoDto.getRoutePayStatus());
                thirdChannel.setRouteCashStatus(thirdChannelInfoDto.getRouteCashStatus());
                thirdChannel.setRouteWeight(thirdChannelInfoDto.getRouteWeight());
                thirdChannel.setPayDayMax(thirdChannelInfoDto.getPayDayMax());
                thirdChannel.setOpenRandom(thirdChannelInfoDto.getOpenRandom());
                thirdChannel.setRandomMin(thirdChannelInfoDto.getRandomMin());
                thirdChannel.setRandomMax(thirdChannelInfoDto.getRandomMax());
                thirdChannel.preUpdate(headerInfoDto.getCurUserId());
                thirdChannelDao.update(thirdChannel);

                return ReturnVo.returnSuccess();
            }else{
                return  ReturnVo.returnFail();
            }
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnFail();
        }
    }

    @Override
    public ReturnVo loadValidChannel(Integer channelType) {
        List<Map<String, Object>> list = thirdChannelDao.getByType(channelType);
        if(list != null && list.size()>0){
            return ReturnVo.returnSuccess(list);
        }else{
            return  ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前无可用通道"));
        }
    }
}
