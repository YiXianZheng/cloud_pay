package com.cloud.finance.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloud.agent.provider.AgentUserProvider;
import com.cloud.finance.common.dto.*;
import com.cloud.finance.common.enums.PayStatusEnum;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.pay.finance.OverviewVo;
import com.cloud.finance.dao.ShopPayDao;
import com.cloud.finance.po.ShopAccountRecord;
import com.cloud.finance.service.FinanceService;
import com.cloud.finance.service.ShopAccountRecordService;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.*;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysuser.provider.SysUserProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * @Auther Toney
 * @Date 2018/7/29 17:52
 * @Description:
 */
@Service
public class FinanceServiceImpl implements FinanceService {
    private static Logger logger = LoggerFactory.getLogger(FinanceServiceImpl.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private MerchantUserProvider merchantUserProvider;
    @Autowired
    private AgentUserProvider agentUserProvider;
    @Autowired
    private ShopPayDao shopPayDao;
    @Autowired
    private SysUserProvider sysUserProvider;
    @Autowired
    private ShopAccountRecordService shopAccountRecordService;

    @Override
    public ShopPayDto beforeCreateOrder(String mercCode, String mercPayOrderNo, String mercNotifyUrl, String mercReturnUrl,
                                  String mercCancelUrl, String paymentType, String subPayCode, Double mercPayMoney,
                                  String mercPayMessage, String mercGoodsTitle, String mercGoodsDesc, String mercMd5Key,
                                        Integer sysOrderType, Integer source) {

        ShopPayDto shopPayDto = new ShopPayDto();

        Map<String, String> map = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, mercCode);
        logger.info("get redis info in db ["+ RedisConfig.MERCHANT_INFO_DB +"]");
        if(map == null || map.size()==0){
            ApiResponse apiResponse = merchantUserProvider.initMerchantToRedis(mercCode);
            if(apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())){
                map = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, mercCode);
            }else{
                return null;
            }
        }

        if(map == null || map.size()>0){
            shopPayDto.setSysPayOrderNo("E" + DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_17)
                        + StringUtil.getRandom(4));
            shopPayDto.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
            shopPayDto.setChannelTypeCode(paymentType);
            shopPayDto.setBankCode(subPayCode);
            shopPayDto.setCreateTime(new Date());
            shopPayDto.setSource(source);

            shopPayDto.setMerchantCode(mercCode);
            shopPayDto.setMerchantUser(map.get("id"));
            shopPayDto.setMerchantOrderNo(mercPayOrderNo);
            shopPayDto.setMerchantGoodsTitle(mercGoodsTitle);
            shopPayDto.setMerchantGoodsDesc(mercGoodsDesc);
            shopPayDto.setMerchantPayMoney(mercPayMoney);
            shopPayDto.setMerchantPayMessage(mercPayMessage);
            shopPayDto.setMerchantNotifyUrl(mercNotifyUrl);
            shopPayDto.setMerchantReturnUrl(mercReturnUrl);
            shopPayDto.setMerchantCancelUrl(mercCancelUrl);
            shopPayDto.setSysPayOrderType(sysOrderType);
            shopPayDto.setThirdChannelNotifyFlag(0);

            shopPayDto.setAgentCode(map.get("agentCode"));
            shopPayDto.setAgentUser(map.get("agentId"));

            if(StringUtils.isNotBlank(map.get("agentId"))) {
                if(map.get(paymentType)==null)
                    return null;
                //商户通道费用
                Double mercCostRate = Double.parseDouble(map.get(paymentType));
                shopPayDto.setMerchantCostRate(mercCostRate);
                Double mercCostMoney = SafeComputeUtils.multiply(mercCostRate, mercPayMoney);
                shopPayDto.setMerchantCostMoney(mercCostMoney);

                //代理商通道费用
                Map<String, String> agent = redisClient.Gethgetall(RedisConfig.AGENT_INFO_DB, map.get("agentCode"));
                if (agent == null || agent.size() == 0) {
                    ApiResponse apiResponse = agentUserProvider.initAgentToRedis(map.get("agentCode"));
                    if (apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())) {
                        agent = redisClient.Gethgetall(RedisConfig.AGENT_INFO_DB, mercCode);
                    } else {
                        return null;
                    }
                }

                if(StringUtils.isNotBlank(agent.get(paymentType))) {
                    Double agentCostRate = Double.parseDouble(agent.get(paymentType));
                    shopPayDto.setAgentCostRate(agentCostRate);
                    Double agentCostMoney = SafeComputeUtils.multiply(agentCostRate, mercPayMoney);
                    shopPayDto.setAgentCostMoney(agentCostMoney);
                }
            }

            return shopPayDto;

        }
        return null;
    }

    @Override
    public ReturnVo initOverview(Integer type, String userCode) {
        Date startTime = new Date();
        Date endTime = startTime;

        //不区分通道，  统计当天的数据
        List<FinanceOverviewDto> overview = new ArrayList<>();
        if(1 == type || 4 == type) {
            List<FinanceOverviewDto> overview1 = shopPayDao.getOverViewInfo(startTime, endTime);
            overview.addAll(overview1);
        }else if(2 == type || 4 == type){
            List<FinanceOverviewDto> overview2 = shopPayDao.getAgentOverViewInfo(startTime, endTime, userCode);
            overview.addAll(overview2);
        }else if(3 == type || 4 == type) {
            List<FinanceOverviewDto> overview3 = shopPayDao.getMerchantOverViewInfo(startTime, endTime, userCode);
            overview.addAll(overview3);
        }else{
            return ReturnVo.returnFail();
        }

        //存到Redis中
        for (FinanceOverviewDto finance: overview
                ) {
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, finance.getUserCode());

            logger.info("get redis info from db "+ RedisConfig.ORDER_COUNT_DB);

            logger.info("总成功订单：" + Integer.parseInt(map.get("totalSuccessOrder")));
            RedisFinanceDto financeDto = RedisFinanceDto.map2Object(map);

            // 总利润
            financeDto.setDailyTotalCharge(finance.getTotalCharge());
            // 总交易金额
            financeDto.setDailyTotalMoney(finance.getTotalMoney());
            // 总代付笔数
            financeDto.setDailyTotalPaid(finance.getTotalPaid());
            // 总风控订单数
            financeDto.setDailyTotalRiskControlOrder(finance.getTotalRiskControlOrder());
            // 总成功订单数
            financeDto.setDailyTotalSuccessOrder(finance.getTotalSuccessOrder());
            // 总订单数
            financeDto.setDailyTotalOrder(finance.getTotalOrder());
            // 总成功代付笔数
            financeDto.setDailyTotalSuccessPaid(finance.getTotalSuccessPaid());

            redisClient.SetHsetJedis(RedisConfig.ORDER_COUNT_DB, finance.getUserCode(), MyBeanUtil.transBean2Map2(financeDto));
            logger.info("set redis info from db "+ RedisConfig.ORDER_COUNT_DB);

        }
        return ReturnVo.returnSuccess();
    }

    @Override
    public ReturnVo overview(Integer type, String userCode) {
        try {
            Map<String, String> redisInfo = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, userCode);
            logger.info("set redis info from db "+ RedisConfig.ORDER_COUNT_DB);
            if(redisInfo == null || redisInfo.size()==0){
                initOverview(type, userCode);
                redisInfo = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, userCode);
                logger.info("set redis info from db "+ RedisConfig.ORDER_COUNT_DB);
            }

            RedisFinanceDto redisFinanceDto = RedisFinanceDto.map2Object(redisInfo);
            OverviewVo overviewVo = new OverviewVo();
            overviewVo.setDailyCharge(redisFinanceDto.getDailyTotalCharge());                       // 今日利润
            overviewVo.setDailyMoney(redisFinanceDto.getDailyTotalMoney());                         // 今日交易金额
            overviewVo.setDailyOrder(redisFinanceDto.getDailyTotalOrder());                         // 今日订单数
            overviewVo.setDailyPaid(redisFinanceDto.getDailyTotalPaid());                           // 今日代付笔数
            overviewVo.setDailyRiskControlOrder(redisFinanceDto.getDailyTotalRiskControlOrder());   // 今日风控订单笔数
            overviewVo.setDailySuccessOrder(redisFinanceDto.getDailyTotalSuccessOrder());           // 今日成功订单数

            double dailySuccessRate = 0D;
            if(redisFinanceDto.getDailyTotalOrder()>0){
                int a = redisFinanceDto.getDailyTotalSuccessOrder().intValue();
                int b = redisFinanceDto.getDailyTotalOrder().intValue();
                dailySuccessRate = a/(double) b;
            }
            overviewVo.setDailySuccessOrderRate(dailySuccessRate);                                  // 今日订单成功率

            double dailySuccessPaidRate = 0D;
            if(redisFinanceDto.getDailyTotalPaid()>0){
                int a = redisFinanceDto.getDailyTotalSuccessPaid().intValue();
                int b = redisFinanceDto.getDailyTotalPaid();
                dailySuccessPaidRate = a/(double) b;
            }
            overviewVo.setDailySuccessRate(dailySuccessPaidRate);                                   // 今日代付成功率

            // 总利润
            overviewVo.setTotalCharge(SafeComputeUtils.add(redisFinanceDto.getDailyTotalCharge(),
                    SafeComputeUtils.add(redisFinanceDto.getHistoryTotalCharge(), redisFinanceDto.getTotalCharge())));
            // 总交易金额
            overviewVo.setTotalMoney(SafeComputeUtils.add(redisFinanceDto.getDailyTotalMoney(),
                    SafeComputeUtils.add(redisFinanceDto.getHistoryTotalMoney(), redisFinanceDto.getTotalMoney())));
            // 总订单数
            overviewVo.setTotalOrder(redisFinanceDto.getHistoryTotalOrder() + redisFinanceDto.getTotalOrder()
                    + redisFinanceDto.getDailyTotalOrder());
            // 总代付笔数
            overviewVo.setTotalPaid(redisFinanceDto.getHistoryTotalPaid() + redisFinanceDto.getTotalPaid() + redisFinanceDto.getDailyTotalPaid());
            // 总风控订单数
            overviewVo.setTotalRiskControlOrder(redisFinanceDto.getHistoryTotalRiskControlOrder() + redisFinanceDto.getTotalRiskControlOrder()
                    + redisFinanceDto.getDailyTotalRiskControlOrder());
            // 总成功订单数
            overviewVo.setTotalSuccessOrder(redisFinanceDto.getHistoryTotalSuccessOrder() + redisFinanceDto.getTotalSuccessOrder()
                    + redisFinanceDto.getDailyTotalSuccessOrder());

            double totalSuccessRate = 0D;
            if(overviewVo.getTotalOrder()>0){
                int a = overviewVo.getTotalSuccessOrder();
                int b = overviewVo.getTotalOrder();
                totalSuccessRate = a/(double) b;
            }
            overviewVo.setTotalSuccessOrderRate(totalSuccessRate);                                 // 总订单成功率

            double totalSuccessPaidRate = 0D;
            int totalSuccessPaid = redisFinanceDto.getHistoryTotalSuccessPaid() + redisFinanceDto.getTotalSuccessPaid() + redisFinanceDto.getDailyTotalSuccessPaid();
            int totalPaid = redisFinanceDto.getHistoryTotalPaid() + redisFinanceDto.getTotalPaid() + redisFinanceDto.getDailyTotalPaid();
            if(totalPaid>0){
                totalSuccessPaidRate = totalSuccessPaid/(double) totalPaid;
            }
            overviewVo.setTotalSuccessRate(totalSuccessPaidRate);                                   // 总代付成功率

            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, JSONObject.toJSON(overviewVo));
        }catch (Exception e){
            return ReturnVo.returnError(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    @Override
    public ReturnVo merchantRanking() {
        try {
            List<Map<String, Object>> ranking = new ArrayList<>();
            Set<String> res = redisClient.GetWhereKeys(RedisConfig.ORDER_COUNT_DB, "???????????????");
            for (String str: res
                 ) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, str);
                RedisFinanceDto finance = RedisFinanceDto.map2Object(map);

                Double totalCount = SafeComputeUtils.add(
                        SafeComputeUtils.add(finance.getTotalMoney(), finance.getHistoryTotalMoney()
                            ), finance.getDailyTotalMoney());


                Map<String, Object> rank = new HashMap<>();
                rank.put("merchantCode", str);
                rank.put("merchantName", redisClient.Gethget(RedisConfig.MERCHANT_INFO_DB, str, "merchantName"));
                rank.put("totalMoney", totalCount);

                ranking.add(rank);
            }

            Collections.sort(ranking, new Comparator<Map<String, Object>>(){
                public int compare(Map<String, Object> arg0, Map<String, Object> arg1) {
                    Double num1 = (Double) arg0.get("totalMoney");
                    Double num2 = (Double) arg1.get("totalMoney");

                    return num1>num2?-1:1;
                }
            });

            List<Map<String, Object>> rankings = new ArrayList<>();
            for (Map<String, Object> rank: ranking
                    ) {
                rankings.add(rank);
                if(rankings.size() >= 7){
                    break;
                }
            }

            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, JSONArray.toJSON(rankings));
        }catch (Exception e){
            return ReturnVo.returnError(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    @Override
    public ReturnVo channelRate(String code, Double money) {

        String channelDate = redisClient.Gethget(RedisConfig.ORDER_FINANCE_GRAPH, Constant.REDIS_FINANCE_CHANNEL_RATE, code);
        if(channelDate ==null){
            Set<String> channels = redisClient.GetWhereKeys(RedisConfig.SYS_PAY_CHANNEL, "*");

            logger.info("通道：" + channels);

            for (String channelCode: channels
                 ) {
                Map map = redisClient.Gethgetall(RedisConfig.SYS_PAY_CHANNEL, channelCode);
                Map<String, String> channelRate = new HashMap<>();
                channelRate.put("channelCode", map.get("code").toString());
                channelRate.put("channelName", map.get("name").toString());
                channelRate.put("totalMoney", "0");

                redisClient.SetHsetJedis(RedisConfig.ORDER_FINANCE_GRAPH, Constant.REDIS_FINANCE_CHANNEL_RATE, channelCode, JSONObject.toJSONString(channelRate));
                if(channelCode.equals(code)){
                    channelDate = JSONObject.toJSONString(channelRate);
                }
            }

        }

        Map<String, String> map = (Map<String, String>) JSONObject.parse(channelDate);
        if(map != null && map.size()>0){
            if(map.get("totalMoney") != null){
                Double m = Double.parseDouble(map.get("totalMoney"));
                map.put("totalMoney", SafeComputeUtils.add(m, money)+"");
            }else{
                map.put("totalMoney", money+"");
            }
            redisClient.SetHsetJedis(RedisConfig.ORDER_FINANCE_GRAPH, Constant.REDIS_FINANCE_CHANNEL_RATE, code, JSONObject.toJSONString(map));
            return ReturnVo.returnSuccess();
        }

        return ReturnVo.returnFail();
    }

    @Override
    public ReturnVo initChannelOverview(HeaderInfoDto headerInfoDto) {
        Date startTime = new Date();
        Date endTime = startTime;

        Set<String> merchantCodes = new HashSet<>();
        if(StringUtils.isBlank(headerInfoDto.getAuth()) || HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            merchantCodes = redisClient.GetWhereKeys(RedisConfig.MERCHANT_INFO_DB, "*");
        }else {
            ApiResponse apiResponse = sysUserProvider.getMerchantCodes(headerInfoDto.getCurUserId());
            if (apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())) {
                String temp = apiResponse.getData().toString();
                String[] arr = temp.split(",");
                for (String codes : arr
                        ) {
                    merchantCodes.add(codes);
                }
            }
        }

        for (String merCode: merchantCodes
                ) {
            //存到Redis中
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merCode);
            if(StringUtils.isNotBlank(map.get("sysUserId"))) {
                List<ChannelSummaryDto> summaryDtos = shopAccountRecordService.channelSummary(startTime, endTime, map.get("sysUserId"));
                logger.info("刷新当天的Redis数据【merchant channel count】--------------list size:" + summaryDtos.size());
                for (ChannelSummaryDto summary: summaryDtos
                        ) {
                    String jsonStr = redisClient.Gethget(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, merCode, summary.getChannelId());
                    Map<String, String> merchantChannel = JSONObject.parseObject(jsonStr, HashMap.class);
                    RedisChannelSummaryDto redisChannelSummaryDto = RedisChannelSummaryDto.map2Object(merchantChannel);

                    redisChannelSummaryDto.setDailyTotalCharge(summary.getTotalCharge());
                    redisChannelSummaryDto.setDailyTotalMoney(summary.getTotalMoney());
                    redisChannelSummaryDto.setDailyTotalPaidMoney(summary.getTotalPaidMoney());
                    redisChannelSummaryDto.setDailyTotalPaid(summary.getTotalPaid());
                    redisChannelSummaryDto.setDailyTotalRiskControlOrder(summary.getTotalRiskControlOrder());
                    redisChannelSummaryDto.setDailyTotalSuccessOrder(summary.getTotalSuccessOrder());
                    redisChannelSummaryDto.setDailyTotalOrder(summary.getTotalOrder());
                    redisChannelSummaryDto.setDailyTotalSuccessPaid(summary.getTotalSuccessPaid());

                    redisClient.SetHsetJedis(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, merCode, summary.getChannelId(),
                            JSONObject.toJSONString(MyBeanUtil.transBean2Map2(redisChannelSummaryDto)));
                }
                logger.info("刷新当天的Redis数据【merchant channel count】--------------merCode:" + merCode);
            }
        }
        return ReturnVo.returnSuccess();
    }

    @Override
    public ReturnVo channelOverview(HeaderInfoDto headerInfoDto) {
        try {
            Set<String> merchantCodes = new HashSet<>();
            if(HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
                merchantCodes = redisClient.GetWhereKeys(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, "*");
            }else {
                ApiResponse apiResponse = sysUserProvider.getMerchantCodes(headerInfoDto.getCurUserId());
                if (apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())) {
                    String temp = apiResponse.getData().toString();
                    String[] arr = temp.split(",");
                    for (String codes : arr
                            ) {
                        merchantCodes.add(codes);
                    }
                }
            }

            Map<String, Object> res = new HashMap<>();
            for (String merCode: merchantCodes
                    ) {
                Map<String, String> redisInfo = redisClient.Gethgetall(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, merCode);
                if (redisInfo == null || redisInfo.size() == 0) {
                    initChannelOverview(headerInfoDto);
                    redisInfo = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, merCode);
                }
                for (Map.Entry<String, String> entry : redisInfo.entrySet()) {
                    Map<String, String> map = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merCode);
                    if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth()) && StringUtils.isNotBlank(map.get("thirdChannels"))) {
                        String tempStr = map.get("thirdChannels");
                        String [] cid = tempStr.split(",");
                        boolean flag = false;
                        for (String id: cid
                             ) {
                            if(id.equals(entry.getKey())){
                                flag = true;
                                break;
                            }
                        }
                        if (!flag){
                            continue;
                        }
                    }

                    Map<String, String> merchantChannel = JSONObject.parseObject(entry.getValue(), HashMap.class);
                    RedisChannelSummaryDto redisChannelSummaryDto = RedisChannelSummaryDto.map2Object(merchantChannel);
                    OverviewVo overviewVo = (OverviewVo) res.get(entry.getKey());
                    if(overviewVo == null) {
                        overviewVo = new OverviewVo();
                        overviewVo.setChannelId(entry.getKey());
                    }

                    overviewVo.setDailyCharge(SafeComputeUtils.add(overviewVo.getDailyCharge(), redisChannelSummaryDto.getDailyTotalCharge()));            // 今日利润
                    overviewVo.setDailyMoney(SafeComputeUtils.add(overviewVo.getDailyMoney(), redisChannelSummaryDto.getDailyTotalMoney()));               // 今日交易金额
                    overviewVo.setDailyPaidMoney(SafeComputeUtils.add(overviewVo.getDailyPaidMoney(), redisChannelSummaryDto.getDailyTotalPaidMoney()));   // 今日代付金额
                    if(redisChannelSummaryDto.getDailyTotalOrder() != null)
                        overviewVo.setDailyOrder((overviewVo.getDailyOrder()!=null?overviewVo.getDailyOrder():0)
                                + redisChannelSummaryDto.getDailyTotalOrder());                                 // 今日订单数
                    if(redisChannelSummaryDto.getDailyTotalPaid() != null)
                        overviewVo.setDailyPaid((overviewVo.getDailyPaid()!=null?overviewVo.getDailyPaid():0)
                                + redisChannelSummaryDto.getDailyTotalPaid());                                  // 今日代付笔数
                    if(redisChannelSummaryDto.getDailyTotalRiskControlOrder() != null)
                        overviewVo.setDailyRiskControlOrder((overviewVo.getDailyRiskControlOrder()!=null?overviewVo.getDailyRiskControlOrder():0)
                                + redisChannelSummaryDto.getDailyTotalRiskControlOrder());                      // 今日风控订单笔数
                    if(redisChannelSummaryDto.getDailyTotalSuccessOrder() != null)
                        overviewVo.setDailySuccessOrder((overviewVo.getDailySuccessOrder()!=null?overviewVo.getDailySuccessOrder():0)
                                + redisChannelSummaryDto.getDailyTotalSuccessOrder());                          // 今日成功订单数

                    double dailySuccessRate = 0D;
                    if (redisChannelSummaryDto.getDailyTotalOrder() > 0) {
                        int a = redisChannelSummaryDto.getDailyTotalSuccessOrder().intValue();
                        int b = redisChannelSummaryDto.getDailyTotalOrder().intValue();
                        dailySuccessRate = a / (double) b;
                    }
                    overviewVo.setDailySuccessOrderRate(dailySuccessRate);                                  // 今日订单成功率

                    double dailySuccessPaidRate = 0D;
                    if (redisChannelSummaryDto.getDailyTotalPaid() > 0) {
                        int a = redisChannelSummaryDto.getDailyTotalSuccessPaid().intValue();
                        int b = redisChannelSummaryDto.getDailyTotalPaid();
                        dailySuccessPaidRate = a / (double) b;
                    }
                    overviewVo.setDailySuccessRate(dailySuccessPaidRate);                                   // 今日代付成功率

                    // 总利润
                    overviewVo.setTotalCharge(SafeComputeUtils.add(overviewVo.getTotalCharge(), SafeComputeUtils.add(redisChannelSummaryDto.getDailyTotalCharge(),
                            SafeComputeUtils.add(redisChannelSummaryDto.getHistoryTotalCharge(), redisChannelSummaryDto.getTotalCharge()))));
                    // 总交易金额
                    overviewVo.setTotalMoney(SafeComputeUtils.add(overviewVo.getTotalMoney(), SafeComputeUtils.add(redisChannelSummaryDto.getDailyTotalMoney(),
                            SafeComputeUtils.add(redisChannelSummaryDto.getHistoryTotalMoney(), redisChannelSummaryDto.getTotalMoney()))));
                    // 总代付金额
                    overviewVo.setTotalPaidMoney(SafeComputeUtils.add(overviewVo.getTotalPaidMoney(), SafeComputeUtils.add(redisChannelSummaryDto.getDailyTotalPaidMoney(),
                            SafeComputeUtils.add(redisChannelSummaryDto.getHistoryTotalPaidMoney(), redisChannelSummaryDto.getTotalPaidMoney()))));
                    // 总订单数
                    overviewVo.setTotalOrder(overviewVo.getTotalOrder()!=null?overviewVo.getTotalOrder():0 + redisChannelSummaryDto.getHistoryTotalOrder()
                            + redisChannelSummaryDto.getTotalOrder() + redisChannelSummaryDto.getDailyTotalOrder());
                    // 总代付笔数
                    overviewVo.setTotalPaid(overviewVo.getTotalPaid()!=null?overviewVo.getTotalPaid():0 + redisChannelSummaryDto.getHistoryTotalPaid()
                            + redisChannelSummaryDto.getTotalPaid() + redisChannelSummaryDto.getDailyTotalPaid());
                    // 总风控订单数
                    overviewVo.setTotalRiskControlOrder(overviewVo.getTotalRiskControlOrder()!=null?overviewVo.getTotalRiskControlOrder():0
                            + redisChannelSummaryDto.getHistoryTotalRiskControlOrder() + redisChannelSummaryDto.getTotalRiskControlOrder()
                            + redisChannelSummaryDto.getDailyTotalRiskControlOrder());
                    // 总成功订单数
                    overviewVo.setTotalSuccessOrder(overviewVo.getTotalSuccessOrder()!=null?overviewVo.getTotalSuccessOrder():0
                            + redisChannelSummaryDto.getHistoryTotalSuccessOrder() + redisChannelSummaryDto.getTotalSuccessOrder()
                            + redisChannelSummaryDto.getDailyTotalSuccessOrder());

                    double totalSuccessRate = 0D;
                    if (overviewVo.getTotalOrder() > 0) {
                        int a = overviewVo.getTotalSuccessOrder();
                        int b = overviewVo.getTotalOrder();
                        totalSuccessRate = a / (double) b;
                    }
                    overviewVo.setTotalSuccessOrderRate(totalSuccessRate);                                 // 总订单成功率

                    double totalSuccessPaidRate = 0D;
                    int totalSuccessPaid = redisChannelSummaryDto.getHistoryTotalSuccessPaid() + redisChannelSummaryDto.getTotalSuccessPaid() + redisChannelSummaryDto.getDailyTotalSuccessPaid();
                    int totalPaid = redisChannelSummaryDto.getHistoryTotalPaid() + redisChannelSummaryDto.getTotalPaid() + redisChannelSummaryDto.getDailyTotalPaid();
                    if (totalPaid > 0) {
                        totalSuccessPaidRate = totalSuccessPaid / (double) totalPaid;
                    }
                    overviewVo.setTotalSuccessRate(totalSuccessPaidRate);                                   // 总代付成功率

                    res.put(entry.getKey(), overviewVo);
                }
            }
            List<OverviewVo> list = new ArrayList<>();
            for (Map.Entry<String, Object> entry : res.entrySet()) {
                OverviewVo overviewVo = (OverviewVo) entry.getValue();
                Map<String, String> channel = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, overviewVo.getChannelId());
                if("2".equals(channel.get("channelType"))){
                    continue;
                }
                String channelName=null;
                if(HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
                    channelName = channel.get("channelName");
                    overviewVo.setDailyCharge(SafeComputeUtils.sub(overviewVo.getDailyMoney(), overviewVo.getDailyCharge()));
                    overviewVo.setTotalCharge(SafeComputeUtils.sub(overviewVo.getTotalMoney(), overviewVo.getTotalCharge()));
                }else{
                    channelName = channel.get("externalChannelName");
                }
                overviewVo.setChannelName(channelName);
                list.add(overviewVo);
            }

            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, JSONObject.toJSON(list));
        }catch (Exception e){
            return ReturnVo.returnError(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    @Override
    public ReturnVo channelGraph() {
        try {
            List<Map<String, String>> list = new ArrayList<>();

            Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, Constant.SYS_ACCOUNT_NO);
            RedisFinanceDto finance = RedisFinanceDto.map2Object(map);
            Double totalMoney = SafeComputeUtils.add(SafeComputeUtils.add(finance.getHistoryTotalMoney(), finance.getTotalMoney()),
                    finance.getDailyTotalMoney());

            Set<String> channels = redisClient.GetWhereKeys(RedisConfig.SYS_PAY_CHANNEL, "*");
            for (String channelCode: channels
                    ) {
                String jsonStr = redisClient.Gethget(RedisConfig.ORDER_FINANCE_GRAPH, Constant.REDIS_FINANCE_CHANNEL_RATE, channelCode);

                Map<String, String> c = new HashMap<>();
                if(StringUtil.isNotBlank(jsonStr)){
                    c = (Map<String, String>) JSONObject.parse(jsonStr);
                }else{
                    continue;
                }

                Double money = c.get("totalMoney")!=null?Double.parseDouble(c.get("totalMoney")):0;
                if(totalMoney != 0){
                    c.put("rate", SafeComputeUtils.div(money, totalMoney, 2) + "");
                }else{
                    c.put("rate", "0");
                }
                list.add(c);
            }

            Collections.sort(list, new Comparator<Map<String, String>>(){
                public int compare(Map<String, String> arg0, Map<String, String> arg1) {
                    Double num1 = arg0.get("totalMoney")!=null?Double.parseDouble(arg0.get("totalMoney")):0D;
                    Double num2 = arg1.get("totalMoney")!=null?Double.parseDouble(arg1.get("totalMoney")):0D;

                    return num1>num2?-1:1;
                }
            });

            List<Map<String, String>> res = new ArrayList<>();
            Map<String, Object> overFive = new HashMap<>();
            overFive.put("totalMoney", 0);
            overFive.put("rate", 0);
            int i=0;
            for (Map<String, String> m: list
                    ) {
                Double money = m.get("totalMoney")!=null?Double.parseDouble(m.get("totalMoney")):0D;
                Double rate = m.get("rate")!=null?Double.parseDouble(m.get("rate")):0;
                if(money.doubleValue() == 0){
                    continue;
                }

                if(i >= 5){
                    overFive.put("totalMoney", SafeComputeUtils.add((Double)overFive.get("totalMoney"), money));
                    overFive.put("rate", SafeComputeUtils.add((Double)overFive.get("rate"), rate));
                    continue;
                }
                res.add(m);
                i++;
            }
            if(overFive.size()>0){
                Map<String, String> m = new HashMap<>();
                m.put("channelCode", "0");
                m.put("channelName", "其它");
                m.put("totalMoney", overFive.get("totalMoney").toString());
                m.put("rate", overFive.get("rate").toString());
                res.add(m);
            }


            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, JSONArray.toJSONString(res));
        } catch (Exception e){
            return ReturnVo.returnFail();
        }

    }


}
