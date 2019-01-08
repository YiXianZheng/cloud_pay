package com.cloud.taskservice.task;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.utils.HttpClientUtil;
import com.cloud.finance.provider.FinanceProvider;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * 财务模块的定时任务
 * @Auther Toney
 * @Date 2018/7/12 15:41
 * @Description:
 */
@Component
public class FinanceTask {

    private Logger logger = LoggerFactory.getLogger(FinanceTask.class);

    @Autowired
    private FinanceProvider financeProvider;
    @Autowired
    private RedisClient redisClient;


    /**
     *     @Scheduled(fixedDelay = 5000)
     *                fixedDelay = 5000表示当前方法执行完毕5000ms后，Spring scheduling会再次调用该方法
     *     @Scheduled(fixedRate = 5000)
     *                fixedRate = 5000表示当前方法开始执行5000ms后，Spring scheduling会再次调用该方法
     *     @Scheduled(initialDelay = 1000,
     *                initialDelay = 1000表示延迟1000ms执行第一次任务
     *     @Scheduled(cron = "0 0/1 * * * ?")
     *                cron接受cron表达式，根据cron表达式确定定时规则
     */

    /**
     * 每隔10分钟更新财务
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void initOverview() {
        logger.info("==============  begin init overview  ===============");
        ApiResponse response = financeProvider.initOverview();
        logger.info("==============  init overview result: >>>"+ response.getMsg() +"<<<  ===============");
    }


    /**
     * 每隔10分钟更新通道财务
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void initChannelOverview() {
        logger.info("==============  begin init channel overview  ===============");
        ApiResponse response = financeProvider.initChannelOverview();
        logger.info("==============  init channel overview result: >>>"+ response.getMsg() +"<<<  ===============");
    }


    /**
     * 每个月1号凌晨十二点一分 迁移一个月之前的订单数据，并清算
     */
    @Scheduled(cron = "0 1 0 1 * ?")
    public void monthMove() {
        logger.info("==============  begin month move  ===============");
        ApiResponse response = financeProvider.monthMove();

        Date time = new Date();
        redisClient.SetHsetJedis(RedisConfig.TASK_RUN_LOG, "month move",
                DateUtil.DateToString(time, DateUtil.DATE_PATTERN_01), JSONObject.toJSONString(response));
        logger.info("==============  month move result: >>>"+ response.getMsg() +"<<<  ===============");
    }

    /**
     * 每天凌晨十二点二分更新昨天的订单的step 并核算
     */
    @Scheduled(cron = "0 3 0 * * ?")
    public void dailyUpdate() {
        logger.info("==============  begin daily update  ===============");
        ApiResponse response = financeProvider.dailyUpdate();

        Date time = new Date();
        redisClient.SetHsetJedis(RedisConfig.TASK_RUN_LOG, "daily update",
                DateUtil.DateToString(time, DateUtil.DATE_PATTERN_01), JSONObject.toJSONString(response));
        logger.info("==============  daily update result: >>>"+ response.getMsg() +"<<<  ===============");
    }

    /**
     * 每天十二点五分统计各个商户昨日数据，并放入redis
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void dailyAccounting() {
        logger.info("==============  begin daily accounting  ===============");
        ApiResponse response = financeProvider.dailySummary();

        Date time = new Date();
        redisClient.SetHsetJedis(RedisConfig.TASK_RUN_LOG, "daily accounting",
                DateUtil.DateToString(time, DateUtil.DATE_PATTERN_01), JSONObject.toJSONString(response));
        logger.info("==============  daily accounting result: >>>"+ response.getMsg() +"<<<  ===============");
    }

    /**
     * 每秒查询需要补发通知的订单
     */
    @Scheduled(cron = "0/1 * * * * ?")
    public void retryNotify() {
        logger.info("==============  begin retryNotify  ===============");
        try {
            String timesStr = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_TIMES");
            if(StringUtils.isBlank(timesStr)){
                logger.info("==============  retryNotify error: >>> sys_dic data [NOTIFY_TIMES] is undefined <<<  ===============");
            }
            String [] temp = timesStr.split(",");
            List<Integer> notifyTimes = new ArrayList<>();
            if(temp.length > 0) {
                for (String str : temp
                        ) {
                    if(StringUtils.isNotBlank(str)){
                        notifyTimes.add(Integer.parseInt(str));
                    }
                }
            }else{
                logger.info("==============  retryNotify error: >>> sys_dic data [NOTIFY_TIMES] is empty <<<  ===============");
            }

            int preTime = 1;
            for (int i=0; i<notifyTimes.size(); i++
                 ) {
                int time = notifyTimes.get(i);
                int redisDb = RedisConfig.UN_RESPONSE_NOTIFY + i;
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, -time);
                logger.info("==============  retryNotify: >>> now in redis db ["+redisDb+"] <<<  ===============");

                Set<String> keys = redisClient.GetWhereKeys(redisDb, DateUtil.DateToString(calendar.getTime(), "*"));
                if(keys != null && keys.size()>0){
                    for (String key:keys
                         ) {
                        String jsonStr = redisClient.rpoplpush(redisDb, key);
                        if(StringUtils.isBlank(jsonStr))
                            continue;

                        logger.info("==============  retryNotify: >>> jsonStr ["+jsonStr+"] <<<  ===============");
                        net.sf.json.JSONObject myJson = net.sf.json.JSONObject.fromObject(jsonStr);
                        Map params = myJson;

                        String notifyUrl = params.get("notifyUrl").toString();
                        params.remove("notifyUrl");
                        if(params.get("notifyTime") == null){
                            throw  new Exception();
                        }
                        String notifyTimeStr = params.get("notifyTime").toString();
                        params.remove("notifyTime");
                        Date notifyTime = new Date(Long.parseLong(notifyTimeStr));

                        if(notifyTime.before(calendar.getTime())){
                            String notifyResponseResult = HttpClientUtil.post(notifyUrl, params);
                            if (StringUtils.isEmpty(notifyResponseResult) && "success".equalsIgnoreCase(notifyResponseResult)) {
                                logger.info("==============  retryNotify result: >>> SUCCESS~~~, result string ["+ notifyResponseResult +"] <<<  =============== ");
                            } else {
                                if(i == notifyTimes.size()-1)
                                    redisDb = RedisConfig.UN_RESPONSE_NOTIFY_FINAL - 1;
                                redisClient.lpush(redisDb + 1, DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_18),jsonStr);
                                logger.info("==============  retryNotify result: >>> fail in redis db ["+ (redisDb + 1) +"], result string ["+ notifyResponseResult +"] <<<  =============== ");
                            }
                        }
                    }
                }else{
                    logger.info("==============  retryNotify result: >>> data in redis db ["+redisDb+"] is empty <<<  ===============");
                }
            }

        } catch (Exception e){
            e.printStackTrace();
            logger.info("==============  retryNotify result: >>> exception found <<<  ===============");
        }
        logger.info("==============  end retryNotify  ===============");
    }

}
