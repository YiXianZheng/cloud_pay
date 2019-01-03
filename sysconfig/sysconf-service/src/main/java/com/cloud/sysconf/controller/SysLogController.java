package com.cloud.sysconf.controller;

import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysDictDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.service.SysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 系统操作日志的Controller
 */
@RestController
@RequestMapping("/sys/log")
public class SysLogController extends BaseController {

    @Autowired
    private RedisClient redisClient;

    /**
     * 分页搜索系统日志
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping("/tablePage")
    public ApiResponse tablePageForAgent(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        try{
            Integer pageNum = pageQuery.getPageIndex();
            Integer pageSize = pageQuery.getPageSize();
            Map<String, Object> params = pageQuery.getParams();
            Date startTime = new Date();
            Date endTime = new Date();
            if(params != null && params.get("startTime") != null && params.get("endTime") != null) {
                startTime = DateUtil.stringToDate(params.get("startTime").toString(), DateUtil.DATE_PATTERN_02);
                endTime = DateUtil.stringToDate(params.get("endTime").toString(), DateUtil.DATE_PATTERN_02);
            }
            List<String> logs = new ArrayList<>();
            while (endTime.after(startTime) || endTime.equals(startTime)) {
                List<String> list = redisClient.findKeysForPage(RedisConfig.SYS_LOG_DB,
                        DateUtil.DateToString(startTime, DateUtil.DATE_PATTERN_11), pageNum, pageSize);
                startTime = DateUtil.getDateAfter(startTime, 1);
                logs.addAll(list);
            }
            if (logs.size()==0){
                return toApiResponse(ReturnVo.returnFail(ResponseCode.UNINTENDED_RESULT.RESULT_NULL));
            }else {
                List<Map> res = new ArrayList<>();
                for (String key: logs
                     ) {
                      res.add(redisClient.Gethgetall(RedisConfig.SYS_LOG_DB, key));
                }
                return toApiResponse(ReturnVo.returnSuccess(res));
            }
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

}
