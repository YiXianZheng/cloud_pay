package com.cloud.sysconf.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.PayChannelDto;
import com.cloud.sysconf.common.dto.ThirdChannelInfoDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.MyBeanUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.dao.SysPayChannelDao;
import com.cloud.sysconf.po.SysPayChannel;
import com.cloud.sysconf.service.SysPayChannelService;
import com.cloud.sysconf.service.ThirdChannelService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther Toney
 * @Date 2018/7/29 17:38
 * @Description:
 */
@Service
public class SysPayChannelServiceImpl extends BaseMybatisServiceImpl<SysPayChannel, String, SysPayChannelDao> implements SysPayChannelService {

    @Autowired
    private SysPayChannelDao sysPayChannelDao;
    @Autowired
    private ThirdChannelService thirdChannelService;
    @Autowired
    private RedisClient redisClient;

    @Override
    public ReturnVo loadValidPayChannel(HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();

        try{
            List<SysPayChannel> list = sysPayChannelDao.querylist(SysPayChannel.USABLE_YES, headerInfoDto.getAgentUser());

            if(list != null && list.size()==0){
                returnVo.code = 0;
                returnVo.responseCode = ResponseCode.UNINTENDED_RESULT.RESULT_NULL;
            }else{
                returnVo.code = ReturnVo.SUCCESS;
                List<PayChannelDto> channel = new ArrayList<>();
                for (SysPayChannel c: list) {
                    PayChannelDto pc = new PayChannelDto();
                    BeanUtils.copyProperties(c, pc);

                    channel.add(pc);
                }
                returnVo.object = JSONArray.toJSON(channel);
            }
        }catch (Exception e){
            e.printStackTrace();
            returnVo.code = ReturnVo.ERROR;
            returnVo.responseCode = ResponseCode.Base.API_ERR;
        }finally {
            return returnVo;
        }
    }

    @Override
    @PostConstruct //仅在项目启动后运行一次
    public ReturnVo initChannelToRedis() {

        try{
            logger.info("==============  begin init channel to redis  ===============");
            List<SysPayChannel> list = sysPayChannelDao.querylist(SysPayChannel.USABLE_YES, null);

            for (SysPayChannel channel: list
                 ) {
                redisClient.SetHsetJedis(RedisConfig.SYS_PAY_CHANNEL, channel.getCode(), MyBeanUtil.transBean2Map2(channel));
            }
            thirdChannelService.refreshRedis();
            logger.info("==============  init channel to redis result: >>> success <<<  ===============");
            return ReturnVo.returnSuccess();
        }catch (Exception e){
            e.printStackTrace();
            logger.info("==============  init channel to redis result: >>> error <<<  ===============");
            return ReturnVo.returnFail();
        }
    }

    @Override
    public ReturnVo loadChannels() {
        try{
            List<ThirdChannelInfoDto> list = thirdChannelService.getThirdChannel();
            return ReturnVo.returnSuccess(JSONArray.toJSON(list));
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnFail();
        }
    }
}
