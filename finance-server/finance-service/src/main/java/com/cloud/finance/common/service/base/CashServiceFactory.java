package com.cloud.finance.common.service.base;

import com.cloud.agent.provider.AgentUserProvider;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @Auther Toney
 * @Date 2018/9/14 15:56
 * @Description: 所有代付通道都需要再次注册
 */
@Component
public class CashServiceFactory {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private AgentUserProvider agentUserProvider;
    @Autowired
    private MerchantUserProvider merchantUserProvider;
    @Autowired
    @Qualifier("AinongCashService")
    BaseCashService ainongCashService;
    @Autowired
    @Qualifier("AinongAliPayCashService")
    BaseCashService ainongAliPayCashService;
    @Autowired
    @Qualifier("HcCashService")
    BaseCashService hcCashService;
    @Autowired
    @Qualifier("Shtd1CashService")
    BaseCashService shtd1CashService;
    @Autowired
    @Qualifier("JinxinCashService")
    BaseCashService jinxinCashService;
    @Autowired
    @Qualifier("XunjiefuCashService")
    BaseCashService xunjiefuCashService;
    @Autowired
    @Qualifier("MoshangCashService")
    BaseCashService moshangCashService;
    @Autowired
    @Qualifier("AnyinfuCashService")
    BaseCashService anyinfuCashService;
    @Autowired
    @Qualifier("YunjifuCashService")
    BaseCashService yunjifuCashService;
    @Autowired
    @Qualifier("CaocaoCashService")
    BaseCashService caocaoCashService;
    @Autowired
    @Qualifier("ShkbCashService")
    BaseCashService shkbCashService;

    public BaseCashService getPayment(String channelCode){
        if("ainong_cash".equals(channelCode)) {
            logger.info("cash chose channel ainong ");
            return ainongCashService;
        }else if("ainong_cash_alipay".equals(channelCode)){
            logger.info("cash chose channel ainong alipay ");
            return ainongAliPayCashService;
        }else if("hc_cash".equals(channelCode)){
            logger.info("cash chose channel hc ");
            return hcCashService;
        }else if("shtd1_cash".equals(channelCode)){
            logger.info("cash chose channel shanghai channel 1 ");
            return shtd1CashService;
        }else if("jinxin_cash".equals(channelCode)){
            logger.info("cash chose channel jinxin ");
            return jinxinCashService;
        }else if("xunjiefu_cash".equals(channelCode)){
            logger.info("cash chose channel xunjiefu ");
            return xunjiefuCashService;
        }else if("moshang_cash".equals(channelCode)){
            logger.info("cash chose channel moshang ");
            return moshangCashService;
        }else if("anyinfu_cash".equals(channelCode)){
            logger.info("cash chose channel anyinfu ");
            return anyinfuCashService;
        }else if("yunjifu_cash".equals(channelCode)){
            logger.info("cash chose channel yunji ");
            return yunjifuCashService;
        }else if("caocao_cash".equals(channelCode)){
            logger.info("cash chose channel CaocaoCashService ");
            return caocaoCashService;
        }else if("shkb_cash".equals(channelCode)){
            logger.info("cash chose channel ShkbCashService ");
            return shkbCashService;
        }else{
            return null;
        }
    }

    /**
     * 第三通通道选择路由
     * @param agentUser
     * @param merchantUser
     * @param money
     * @return
     */
    public ThirdChannelDto channelRoute(String agentUser, String merchantUser, Double money){
        //获取代理或商户信息 判断是否有专属通道
        String thirdChannels = null;
        if(agentUser!=null){
            ApiResponse response = agentUserProvider.detailById(agentUser);
            if((ResponseCode.Base.SUCCESS.getCode()+"").equals(response.getCode())){
                Map agentInfoDto = (HashMap) response.getData();
                if(agentInfoDto.get("thirdChannels") != null)
                    thirdChannels = agentInfoDto.get("thirdChannels").toString();
            }
        }else if(merchantUser != null){
            ApiResponse response = merchantUserProvider.detailById(merchantUser);
            if((ResponseCode.Base.SUCCESS.getCode()+"").equals(response.getCode())){
                Map merchantInfoDto = (HashMap) response.getData();
                if(merchantInfoDto.get("thirdChannels") != null)
                    thirdChannels = merchantInfoDto.get("thirdChannels").toString();
            }
        }
        Set<String> channels = new HashSet<>();
        //获取商户信息 判断是否有专属通道
        if(StringUtils.isNotBlank(thirdChannels)){
            this.logger.info("----------------指定通道:" + thirdChannels);
            String[] idArr = thirdChannels.split(",");
            for (String channelId: idArr
                    ) {
                if(StringUtils.isNotBlank(channelId)){
                    channels.add(channelId);
                }
            }
        }else{
            channels = redisClient.GetWhereKeys(RedisConfig.THIRD_PAY_CHANNEL, "*");
        }
        //选出可用通道，然后路由
        boolean channelActive = false;//是否有可用通道
        ThirdChannelDto thirdChannelDto = new ThirdChannelDto();
        if(channels.size()>0) {
            for (String channelId : channels
                    ) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, channelId);
                if(map.get("channelType") != null && "2".equals(map.get("channelType").toString())){
                    ThirdChannelDto channelDto = ThirdChannelDto.map2Object(map);
                    if(thirdChannelDto.getRouteWeight()==null || channelDto.getRouteWeight() > thirdChannelDto.getRouteWeight()){
                        thirdChannelDto = channelDto;
                        channelActive = true;
                    }
                }
            }

            return channelActive?thirdChannelDto:null;
        }else{
            return null;
        }
    }
}
