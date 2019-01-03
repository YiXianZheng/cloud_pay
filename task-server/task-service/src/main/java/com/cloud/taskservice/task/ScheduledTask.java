package com.cloud.taskservice.task;

import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ResultVo;
import com.cloud.sysuser.provider.SysUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


/**
 * @Auther Toney
 * @Date 2018/7/12 15:41
 * @Description:
 */
@Component
public class ScheduledTask {

    private Logger logger = LoggerFactory.getLogger(ScheduledTask.class);

    @Autowired
    private SysUserProvider sysUserProvider;

    /**
     *     @Scheduled(fixedDelay = 5000)
     *                fixedDelay = 5000表示当前方法执行完毕5000ms后，Spring scheduling会再次调用该方法
     *     @Scheduled(fixedRate = 5000)
     *                fixedRate = 5000表示当前方法开始执行5000ms后，Spring scheduling会再次调用该方法
     *     @Scheduled(initialDelay = 1000,
     *                initialDelay = 1000表示延迟1000ms执行第一次任务
     *     @Scheduled(cron = "0 0/1 * * * ?")
     *                cron接受cron表达式，根据cron表达式确定定时规则
     *
     *
     */

    /**
     * 每隔五分钟清理失效的token
     */
//    @Scheduled(cron = "0 0/5 * * * ?")
//    public void cleanFailureToken() {
//        logger.info("==============  begin clean failure token  ===============");
//        ApiResponse response = sysUserProvider.cleanFailureToken();
//        logger.info("==============  clean failure token result: >>>"+ response.getMsg() +"<<<  ===============");
//    }

}
