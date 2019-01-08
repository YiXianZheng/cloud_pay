package com.cloud.finance.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.*;
import com.cloud.finance.common.enums.AccountRecordStatusEnum;
import com.cloud.finance.common.enums.PayStatusEnum;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.dao.ShopPayDao;
import com.cloud.finance.dao.ShopPayFrozenDao;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.po.ShopPayFrozen;
import com.cloud.finance.po.ShopPayLog;
import com.cloud.finance.service.ShopAccountRecordService;
import com.cloud.finance.service.ShopPayLogService;
import com.cloud.finance.service.ShopPayService;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.*;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.utils.page.PageResult;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysuser.provider.SysUserProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


/**
 * @Auther Toney
 * @Date 2018/7/29 17:52
 * @Description:
 */
@Service
public class ShopPayServiceImpl extends BaseMybatisServiceImpl<ShopPay, String, ShopPayDao> implements ShopPayService {

    @Autowired
    private ShopPayDao shopPayDao;
    @Autowired
    private ShopPayLogService shopPayLogService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private SysUserProvider sysUserProvider;
    @Autowired
    private ShopAccountRecordService shopAccountRecordService;
    @Autowired
    private ShopPayFrozenDao shopPayFrozenDao;

    @Transactional
    @Override
    public ReturnVo save(ShopPayDto shopPayDto, HeaderInfoDto headerInfoDto) {
        try {
            ShopPay shopPay = new ShopPay();
            BeanUtils.copyProperties(shopPayDto, shopPay);

            ShopPay sp = shopPayDao.getBySysOrderNo(shopPay.getSysPayOrderNo());
            if(sp != null){
                shopPay.setId(sp.getId());
            }
            if(shopPay != null && StringUtils.isNotBlank(shopPay.getId())){
                shopPay.preUpdate(headerInfoDto.getCurUserId());
                shopPayDao.update(shopPay);
            }else{
                shopPay.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                shopPayDao.add(shopPay);
            }
            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, JSONObject.toJSON(shopPay));
        } catch (Exception e){
            return ReturnVo.returnError();
        }
    }

    @Override
    public boolean checkExist(String merchantCode, String merchantOrderNo) {
        int num = shopPayDao.checkExist(merchantCode, merchantOrderNo);
        if(num>0){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto) {
        try {
            PageResult pageResult = this.queryForTablePage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, JSONObject.toJSON(pageResult));

        }catch (Exception e){
            return ReturnVo.returnFail();
        }
    }

    @Override
    public void initMonth() {
        try {
            logger.info("开始初始化近一个月Redis数据--------------");
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.DATE, 1);// 设为当前月的1号
            calendar.add(Calendar.MONTH, -1);


            Date startTime = calendar.getTime();
            Date endTime = DateUtil.getDateBefore(new Date(), 1);

            //不区分通道
            List<FinanceOverviewDto> overview = shopPayDao.getOverViewInfo(startTime, endTime);
            Set<String> agentCodes = redisClient.GetWhereKeys(RedisConfig.AGENT_INFO_DB,"*");
            for (String agentCode: agentCodes
                    ) {
                List<FinanceOverviewDto> overview2 = shopPayDao.getAgentOverViewInfo(startTime, endTime, agentCode);
                overview.addAll(overview2);
            }
            Set<String> merCodes = redisClient.GetWhereKeys(RedisConfig.MERCHANT_INFO_DB,"*");
            for (String merCode: merCodes
                    ) {
                List<FinanceOverviewDto> overview3 = shopPayDao.getMerchantOverViewInfo(startTime, endTime, merCode);
                overview.addAll(overview3);
            }

            for (FinanceOverviewDto finance : overview
                    ) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, finance.getUserCode());
                if (map == null || map.size() == 0) {
                    shopPayLogService.countHistory();
                    map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, finance.getUserCode());
                }
                map.put("totalMoney", finance.getTotalMoney()+"");
                map.put("totalCharge", finance.getTotalCharge()+"");
                map.put("totalOrder", finance.getTotalOrder()+"");
                map.put("totalSuccessOrder", finance.getTotalSuccessOrder()+"");
                map.put("totalPaid", finance.getTotalPaid()+"");
                map.put("totalSuccessPaid", finance.getTotalSuccessPaid()+"");
                map.put("totalRiskControlOrder", finance.getTotalRiskControlOrder()+"");

                redisClient.SetHsetJedis(RedisConfig.ORDER_COUNT_DB, finance.getUserCode(), map);
                logger.info("初始化近一个月Redis数据【order count】--------------userCode:"+finance.getUserCode()+"; data:"+JSONObject.toJSONString(map));

            }
            //区分通道
            for (String merCode: merCodes
                    ) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merCode);
                if(StringUtils.isNotBlank(map.get("sysUserId"))) {
                    List<ChannelSummaryDto> summaryDtos = shopAccountRecordService.channelSummary(startTime, endTime, map.get("sysUserId"));
                    for (ChannelSummaryDto summary: summaryDtos
                            ) {
                        String jsonStr = redisClient.Gethget(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, merCode, summary.getChannelId());
                        Map<String, String> merchantChannel = JSONObject.parseObject(jsonStr, HashMap.class);
                        if(merchantChannel == null){
                            merchantChannel = new HashMap<>();
                        }

                        merchantChannel.put("totalMoney", summary.getTotalMoney()+"");
                        merchantChannel.put("totalCharge", summary.getTotalCharge()+"");
                        merchantChannel.put("totalOrder", summary.getTotalOrder()+"");
                        merchantChannel.put("totalSuccessOrder", summary.getTotalSuccessOrder()+"");
                        merchantChannel.put("totalPaid", summary.getTotalPaid()+"");
                        merchantChannel.put("totalSuccessPaid", summary.getTotalSuccessPaid()+"");
                        merchantChannel.put("totalRiskControlOrder", summary.getTotalRiskControlOrder()+"");
                        redisClient.SetHsetJedis(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, merCode, summary.getChannelId(), JSONObject.toJSONString(merchantChannel));
                    }
                    logger.info("初始化近一个月Redis数据【merchant channel count】--------------merCode:" + merCode);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            logger.error("=========== 重置近一个月统计数据异常 ==========");
        }
    }

    @Override
    public ShopPay getBySysOrderNo(String orderNo) {
        return shopPayDao.getBySysOrderNo(orderNo);
    }

    @Transactional
    @Override
    public void updateOrderStatus(ShopPay shopPay) {
        shopPayDao.updateOrderStatus(shopPay);
        Integer status = AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_FAIL.getCode();
        if(PayStatusEnum.PAY_STATUS_ALREADY.getStatus() == shopPay.getPayStatus()){
            status = AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_SUCCESS.getCode();
        }
        shopAccountRecordService.updateRecordStatus(shopPay.getSysPayOrderNo(), status);

        if(PayStatusEnum.PAY_STATUS_ALREADY.getStatus() == shopPay.getPayStatus()){
            String channelDate = redisClient.Gethget(RedisConfig.ORDER_FINANCE_GRAPH, Constant.REDIS_FINANCE_CHANNEL_RATE, shopPay.getChannelTypeCode());
            if(channelDate ==null){
                Set<String> channels = redisClient.GetWhereKeys(RedisConfig.SYS_PAY_CHANNEL, "*");

                for (String channelCode: channels
                        ) {
                    Map map = redisClient.Gethgetall(RedisConfig.SYS_PAY_CHANNEL, channelCode);
                    Map<String, String> channelRate = new HashMap<>();
                    channelRate.put("channelCode", map.get("code").toString());
                    channelRate.put("channelName", map.get("name").toString());
                    channelRate.put("totalMoney", "0");

                    redisClient.SetHsetJedis(RedisConfig.ORDER_FINANCE_GRAPH, Constant.REDIS_FINANCE_CHANNEL_RATE, channelCode, JSONObject.toJSONString(channelRate));
                    if(channelCode.equals(shopPay.getChannelTypeCode())){
                        channelDate = JSONObject.toJSONString(channelRate);
                    }
                }

            }

            Map<String, String> map = (Map<String, String>) JSONObject.parse(channelDate);
            if(map != null && map.size()>0){
                if(map.get("totalMoney") != null){
                    Double m = Double.parseDouble(map.get("totalMoney"));
                    map.put("totalMoney", SafeComputeUtils.add(m, shopPay.getMerchantPayMoney())+"");
                }else{
                    map.put("totalMoney", shopPay.getMerchantPayMoney()+"");
                }
                redisClient.SetHsetJedis(RedisConfig.ORDER_FINANCE_GRAPH, Constant.REDIS_FINANCE_CHANNEL_RATE, shopPay.getChannelTypeCode(), JSONObject.toJSONString(map));
            }

        }
    }

    @Override
    public void updateThirdInfo(String sysPayOrderNo, String channelId) {
        //将第三方通道的信息更新到平台订单中

        ShopPay shopPay = shopPayDao.getBySysOrderNo(sysPayOrderNo);
        shopPay.setThirdChannelId(channelId);
        String channelTypeStr = redisClient.Gethget(RedisConfig.THIRD_PAY_CHANNEL, channelId, "channelType");
        shopPay.setThirdChannelType(channelTypeStr!=null?Integer.parseInt(channelTypeStr.toString()):ThirdChannelDto.CHANNEL_TYPE_RECHARGE);

        String costRateStr = redisClient.Gethget(RedisConfig.THIRD_PAY_CHANNEL, channelId, shopPay.getChannelTypeCode());
        double costRate = 0D;
        if(costRateStr!=null){
            costRate = Double.parseDouble(costRateStr.toString());
        }
        shopPay.setThirdChannelCostRate(costRate);

        Double thirdChannelMoney = SafeComputeUtils.multiply(shopPay.getMerchantPayMoney(), costRate);
        shopPay.setThirdChannelCostMoney(thirdChannelMoney);

        shopPay.preUpdate(shopPay.getUpdateBy());
        shopPayDao.updateThirdInfo(shopPay);
    }

    @Override
    public ShopPay getByMerchantCodeAndOrderNo(String merchantCode, String merchantOrderNo) {
        ShopPay shopPay = shopPayDao.getByMerchantCodeAndOrderNo(merchantCode, merchantOrderNo);
        if(shopPay != null){
            return shopPay;
        }else{
            ShopPayLog shopPayLog = shopPayLogService.getByMerchantCodeAndOrderNo(merchantCode, merchantOrderNo);
            if(shopPayLog != null){
                BeanUtils.copyProperties(shopPayLog, shopPay);
                return shopPay;
            }else{
                return null;
            }
        }
    }

    @Override
    public ReturnVo dailySummary() {
        try {
            Date date = DateUtil.getDateBefore(new Date(), 1);
            List<SummaryDto> list = shopPayDao.getDailySummary(date);
            List<MerchantChannelDto> list1 = shopPayDao.getChannelDailySummary(date);
            if(list.size()>0){
                for (SummaryDto summary: list
                        ) {
                    redisClient.SetHsetJedis(RedisConfig.MERCHANT_DAILY_PAY_COUNT_DB, summary.getMerchantCode(),
                            DateUtil.DateToString(date, DateUtil.DATE_PATTERN_02), JSONObject.toJSONString(summary));
                }
            }

            if (list1.size() > 0) {
                for (MerchantChannelDto merchantChannelDto: list1) {
                    List<MerchantChannelDto> redisList = new ArrayList<>();

                    // 获取redis数据
                    String redisStr = redisClient.Gethget(RedisConfig.MERCHANT_DAILY_CHANNEL_COUNT_DB,
                                    merchantChannelDto.getMerchantCode(), DateUtil.DateToString(date, DateUtil.DATE_PATTERN_02));
                    if (StringUtils.isNotEmpty(redisStr)) {
                        redisList = JSONArray.parseArray(redisStr, MerchantChannelDto.class);
                    }
                    redisList.add(merchantChannelDto);
                    for (int i = 0; i < redisList.size()-1; i++) {
                        for (int j = redisList.size()-1; j > i; j--) {
                            if (redisList.get(j).getChannelId().equals(redisList.get(i).getChannelId())) {
                                redisList.remove(j);
                            }
                        }
                    }
                    // 存储数据
                    redisClient.SetHsetJedis(RedisConfig.MERCHANT_DAILY_CHANNEL_COUNT_DB, merchantChannelDto.getMerchantCode(),
                            DateUtil.DateToString(date, DateUtil.DATE_PATTERN_02), JSONObject.toJSONString(redisList));
                }
            }
            return ReturnVo.returnSuccess();
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnFail();
        }
    }

    @Override
    public ReturnVo getDailySummary(HeaderInfoDto headerInfoDto) {
        try {
            List<Map<String, String>> list = new ArrayList<>();
            Set<String> merchantCodes = new HashSet<>();
            if(HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
                merchantCodes = redisClient.GetWhereKeys(RedisConfig.MERCHANT_DAILY_PAY_COUNT_DB, "*");
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

            for (String codes: merchantCodes
                    ) {
                int i = 1;
                while (i < 31) {
                    Date date = DateUtil.getDateBefore(new Date(), i);
                    String info = redisClient.Gethget(RedisConfig.MERCHANT_DAILY_PAY_COUNT_DB, codes,
                            DateUtil.DateToString(date, DateUtil.DATE_PATTERN_02));

                    i++;
                    if(StringUtils.isBlank(info)){
                        continue;
                    }
                    Map<String, String> respMap = JSONObject.parseObject(info, HashMap.class);
                    list.add(respMap);
                }
            }
            return ReturnVo.returnSuccess(list);
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnFail();
        }
    }

    @Transactional
    @Override
    public ReturnVo orderFrozen(String orderNo, Integer status, HeaderInfoDto headerInfoDto) {
        ShopPay shopPay = shopPayDao.getBySysOrderNo(orderNo);
        if(shopPay != null){
            String userId = redisClient.Gethget(RedisConfig.MERCHANT_INFO_DB, shopPay.getMerchantCode(), "sysUserId");
            if(StringUtils.isBlank(userId)){
                logger.info("[order frozen] userId is null");
                return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "系统异常"));
            }
            if(1 == status){
                if(shopPay.getCreateDate().before(DateUtil.getDateBefore(new Date(), 7))){
                    logger.info("[order frozen] order time error");
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "只能冻结七天以内的订单"));
                }
                if(!shopPay.getPayStatus().equals(PayStatusEnum.PAY_STATUS_ALREADY.getStatus())){
                    logger.info("[order frozen] order status error");
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前状态不可操作"));
                }
                shopPayDao.updateStatus(shopPay.getId(), PayStatusEnum.PAY_STATUS_FROZEN.getStatus(), headerInfoDto.getCurUserId(), new Date());

                ShopPayFrozen frozen = new ShopPayFrozen();
                frozen.setId(shopPay.getId());
                frozen.setSysPayOrderNo(orderNo);
                frozen.setOrderCreateDate(shopPay.getCreateDate());
                frozen.setMerchantUser(shopPay.getMerchantUser());
                frozen.setMerchantMoney(SafeComputeUtils.sub(shopPay.getMerchantPayMoney(), shopPay.getMerchantCostMoney()));
                frozen.setAgentUser(shopPay.getAgentUser());
                frozen.setAgentMoney(SafeComputeUtils.sub(shopPay.getMerchantCostMoney(), shopPay.getAgentCostMoney()));
                frozen.setStatus(ShopPayFrozen.STATUS_FROZEN);
                frozen.setChannelId(shopPay.getThirdChannelId());
                frozen.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                shopPayFrozenDao.add(frozen);

                if(ShopPay.STEP_BEFORE_TODAY == shopPay.getStep())
                    initMonth();
                return ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), "冻结成功"));
            }else if(2 == status){
                if(shopPay.getCreateDate().before(DateUtil.getDateBefore(new Date(), 15))){
                    logger.info("[order frozen] order time error");
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "只能解冻15日以内的订单"));
                }
                if(!shopPay.getPayStatus().equals(PayStatusEnum.PAY_STATUS_FROZEN.getStatus())){
                    logger.info("[order frozen] order status error");
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前状态不可操作"));
                }
                shopPayDao.updateStatus(shopPay.getId(), PayStatusEnum.PAY_STATUS_ALREADY.getStatus(), headerInfoDto.getCurUserId(), new Date());

                ShopPayFrozen frozen = new ShopPayFrozen();
                frozen.setId(shopPay.getId());
                frozen.setStatus(ShopPayFrozen.STATUS_UNFROZEN);
                frozen.preUpdate(headerInfoDto.getCurUserId());
                shopPayFrozenDao.updateStatus(frozen);

                if(ShopPay.STEP_BEFORE_TODAY == shopPay.getStep())
                    initMonth();
                return ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), "解冻成功"));
            }
        }else{
            return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "订单不存在"));
        }
        return null;
    }

    @Override
    public ReturnVo getChannelDailySummary(HeaderInfoDto headerInfoDto) {

        try {
            List<MerchantChannelDto> list = new ArrayList<>();
            Set<String> merchantCodes = new HashSet<>();
            // 获取商户编号
            if (HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())) {
                merchantCodes = redisClient.GetWhereKeys(RedisConfig.MERCHANT_DAILY_CHANNEL_COUNT_DB, "*");
            } else {
                ApiResponse apiResponse = sysUserProvider.getMerchantCodes(headerInfoDto.getCurUserId());
                if (apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())) {
                    String temp = apiResponse.getData().toString();
                    String[] arr = temp.split(",");
                    for (String codes : arr) {
                        merchantCodes.add(codes);
                    }
                }
            }

            for (String codes : merchantCodes) {
                int i = 1;
                while (i < 31) {
                    Date date = DateUtil.getDateBefore(new Date(), i);
                    String info = redisClient.Gethget(RedisConfig.MERCHANT_DAILY_CHANNEL_COUNT_DB, codes,
                            DateUtil.DateToString(date, DateUtil.DATE_PATTERN_02));

                    i++;
                    if (StringUtils.isBlank(info)) {
                        continue;
                    }
                    List<MerchantChannelDto> redisList = JSONArray.parseArray(info, MerchantChannelDto.class);
                    list.addAll(redisList);
                }
            }
            return ReturnVo.returnSuccess(list);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【获取商户每日通道汇总异常】");
            return ReturnVo.returnFail();
        }
    }

    @Override
    @Transactional
    public ReturnVo monthMove() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.DATE, 1);// 设为当前月的1号
            calendar.add(Calendar.MONTH, -1);// 减两个月


            Date startTime = DateUtil.getPreviousMonthFirst(calendar.getTime());
            Date endTime = DateUtil.getPreviousMonthEnd(calendar.getTime());

            //不区分通道
            List<FinanceOverviewDto> overview = shopPayDao.getOverViewInfo(startTime, endTime);
            Set<String> agentCodes = redisClient.GetWhereKeys(RedisConfig.AGENT_INFO_DB,"*");
            for (String agentCode: agentCodes
                    ) {
                List<FinanceOverviewDto> overview2 = shopPayDao.getAgentOverViewInfo(startTime, endTime, agentCode);
                overview.addAll(overview2);
            }
            Set<String> merCodes = redisClient.GetWhereKeys(RedisConfig.MERCHANT_INFO_DB,"*");
            for (String merCode: merCodes
                    ) {
                List<FinanceOverviewDto> overview3 = shopPayDao.getMerchantOverViewInfo(startTime, endTime, merCode);
                overview.addAll(overview3);
            }

            for (FinanceOverviewDto finance: overview
                    ) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, finance.getUserCode());
                if(map != null && map.size()>0) {
                    this.initMonth();
                    map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, finance.getUserCode());
                }
                RedisFinanceDto redisFinanceDto = RedisFinanceDto.map2Object(map);

                //总利润
                Double hisTotalCharge = redisFinanceDto.getHistoryTotalCharge();
                Double totalCharge = finance.getTotalCharge();
                redisFinanceDto.setHistoryTotalCharge(SafeComputeUtils.add(hisTotalCharge, totalCharge));

                Double redisTotalCharge = redisFinanceDto.getTotalCharge();
                redisFinanceDto.setTotalCharge(SafeComputeUtils.sub(redisTotalCharge, totalCharge));
                //总交易额
                Double hisTotalMoney = redisFinanceDto.getHistoryTotalMoney();
                Double totalMoney = finance.getTotalMoney();
                redisFinanceDto.setHistoryTotalMoney(SafeComputeUtils.add(hisTotalMoney, totalMoney));

                Double redisTotalMoney = redisFinanceDto.getTotalMoney();
                redisFinanceDto.setTotalMoney(SafeComputeUtils.sub(redisTotalMoney,  totalMoney));
                //总订单数
                Integer hisTotalOrder = redisFinanceDto.getHistoryTotalOrder();
                Integer totalOrder = finance.getTotalOrder();
                redisFinanceDto.setHistoryTotalOrder((hisTotalOrder + totalOrder));

                Integer redisTotalOrder = redisFinanceDto.getTotalOrder();
                redisFinanceDto.setTotalOrder((redisTotalOrder - totalOrder));
                //总成功订单数
                Integer hisTotalSuccessOrder = redisFinanceDto.getHistoryTotalSuccessOrder();
                Integer totalSuccessOrder = finance.getTotalSuccessOrder();
                redisFinanceDto.setHistoryTotalSuccessOrder((hisTotalSuccessOrder + totalSuccessOrder));

                Integer redisSuccessOrder = redisFinanceDto.getTotalSuccessOrder();
                redisFinanceDto.setTotalSuccessOrder((redisSuccessOrder - totalSuccessOrder));
                //总代付笔数
                Integer hisTotalPaid = redisFinanceDto.getHistoryTotalPaid();
                Integer totalPaid = finance.getTotalPaid();
                redisFinanceDto.setHistoryTotalPaid((hisTotalPaid + totalPaid));

                Integer redisTotalPaid = redisFinanceDto.getTotalPaid();
                redisFinanceDto.setTotalPaid((redisTotalPaid - totalPaid));
                //总成功代付笔数
                Integer hisTotalSuccessPaid = redisFinanceDto.getHistoryTotalSuccessPaid();
                Integer totalSuccessPaid = finance.getTotalSuccessPaid();
                redisFinanceDto.setHistoryTotalSuccessPaid((hisTotalSuccessPaid + totalSuccessPaid));

                Integer redisTotalSuccessPaid = redisFinanceDto.getTotalSuccessPaid();
                redisFinanceDto.setTotalSuccessPaid((redisTotalSuccessPaid - totalSuccessPaid));
                //总风控订单
                Integer hisTotalRisk = redisFinanceDto.getHistoryTotalRiskControlOrder();
                Integer totalRisk = finance.getTotalRiskControlOrder();
                redisFinanceDto.setHistoryTotalRiskControlOrder((hisTotalRisk + totalRisk));

                Integer redisTotalRisk = redisFinanceDto.getTotalSuccessPaid();
                redisFinanceDto.setTotalRiskControlOrder((redisTotalRisk - totalRisk));


                redisClient.SetHsetJedis(RedisConfig.ORDER_COUNT_DB, finance.getUserCode(), MyBeanUtil.transBean2Map2(redisFinanceDto));
            }

            shopPayDao.monthMove(calendar.getTime());

            //区分通道
            for (String merCode: merCodes
                    ) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merCode);
                if(StringUtils.isNotBlank(map.get("sysUserId"))) {
                    List<ChannelSummaryDto> summaryDtos = shopAccountRecordService.channelSummary(startTime, endTime, map.get("sysUserId"));
                    for (ChannelSummaryDto summary: summaryDtos
                            ) {
                        String jsonStr = redisClient.Gethget(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, merCode, summary.getChannelId());
                        Map<String, String> merchantChannel = JSONObject.parseObject(jsonStr, HashMap.class);
                        RedisChannelSummaryDto redisChannelSummaryDto = RedisChannelSummaryDto.map2Object(merchantChannel);

                        //总利润
                        Double hisTotalCharge = redisChannelSummaryDto.getHistoryTotalCharge();
                        Double totalCharge = summary.getTotalCharge();
                        redisChannelSummaryDto.setHistoryTotalCharge(SafeComputeUtils.add(hisTotalCharge, totalCharge));

                        Double redisTotalCharge = redisChannelSummaryDto.getTotalCharge();
                        redisChannelSummaryDto.setTotalCharge(SafeComputeUtils.sub(redisTotalCharge, totalCharge));
                        //总代付金额
                        Double hisTotalPaidMoney = redisChannelSummaryDto.getHistoryTotalPaidMoney();
                        Double totalPaidMoney = summary.getTotalPaidMoney();
                        redisChannelSummaryDto.setHistoryTotalPaidMoney(SafeComputeUtils.add(hisTotalPaidMoney, totalPaidMoney));

                        Double redisTotalPaidMoney = redisChannelSummaryDto.getTotalPaidMoney();
                        redisChannelSummaryDto.setTotalPaidMoney(SafeComputeUtils.sub(redisTotalPaidMoney, totalPaidMoney));
                        //总交易额
                        Double hisTotalMoney = redisChannelSummaryDto.getHistoryTotalMoney();
                        Double totalMoney = summary.getTotalMoney();
                        redisChannelSummaryDto.setHistoryTotalMoney(SafeComputeUtils.add(hisTotalMoney, totalMoney));

                        Double redisTotalMoney = redisChannelSummaryDto.getTotalMoney();
                        redisChannelSummaryDto.setTotalMoney(SafeComputeUtils.sub(redisTotalMoney,  totalMoney));
                        //总订单数
                        Integer hisTotalOrder = redisChannelSummaryDto.getHistoryTotalOrder();
                        Integer totalOrder = summary.getTotalOrder();
                        redisChannelSummaryDto.setHistoryTotalOrder((hisTotalOrder + totalOrder));

                        Integer redisTotalOrder = redisChannelSummaryDto.getTotalOrder();
                        redisChannelSummaryDto.setTotalOrder((redisTotalOrder - totalOrder));
                        //总成功订单数
                        Integer hisTotalSuccessOrder = redisChannelSummaryDto.getHistoryTotalSuccessOrder();
                        Integer totalSuccessOrder = summary.getTotalSuccessOrder();
                        redisChannelSummaryDto.setHistoryTotalSuccessOrder((hisTotalSuccessOrder + totalSuccessOrder));

                        Integer redisSuccessOrder = redisChannelSummaryDto.getTotalSuccessOrder();
                        redisChannelSummaryDto.setTotalSuccessOrder((redisSuccessOrder - totalSuccessOrder));
                        //总代付笔数
                        Integer hisTotalPaid = redisChannelSummaryDto.getHistoryTotalPaid();
                        Integer totalPaid = summary.getTotalPaid();
                        redisChannelSummaryDto.setHistoryTotalPaid((hisTotalPaid + totalPaid));

                        Integer redisTotalPaid = redisChannelSummaryDto.getTotalPaid();
                        redisChannelSummaryDto.setTotalPaid((redisTotalPaid - totalPaid));
                        //总成功代付笔数
                        Integer hisTotalSuccessPaid = redisChannelSummaryDto.getHistoryTotalSuccessPaid();
                        Integer totalSuccessPaid = summary.getTotalSuccessPaid();
                        redisChannelSummaryDto.setHistoryTotalSuccessPaid((hisTotalSuccessPaid + totalSuccessPaid));

                        Integer redisTotalSuccessPaid = redisChannelSummaryDto.getTotalSuccessPaid();
                        redisChannelSummaryDto.setTotalSuccessPaid((redisTotalSuccessPaid - totalSuccessPaid));
                        //总风控订单
                        Integer hisTotalRisk = redisChannelSummaryDto.getHistoryTotalRiskControlOrder();
                        Integer totalRisk = summary.getTotalRiskControlOrder();
                        redisChannelSummaryDto.setHistoryTotalRiskControlOrder((hisTotalRisk + totalRisk));

                        Integer redisTotalRisk = redisChannelSummaryDto.getTotalSuccessPaid();
                        redisChannelSummaryDto.setTotalRiskControlOrder((redisTotalRisk - totalRisk));

                        redisClient.SetHsetJedis(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, merCode, summary.getChannelId(),
                                JSONObject.toJSONString(MyBeanUtil.transBean2Map2(redisChannelSummaryDto)));
                    }
                    logger.info("迁移一个月前的数据【shop account record】--------------merCode:" + merCode);
                }
            }

            return ReturnVo.returnSuccess();
        }catch (Exception e){
            return ReturnVo.returnFail();
        }
    }

    @Override
    @Transactional
    public ReturnVo dailyUpdate() {
        try {
            Date beginTime = DateUtil.getPreviousMonthFirst(new Date());
            Date endTime = DateUtil.getDateBefore(new Date(), 1);

            logger.info("开始时间：" + DateUtil.DateToString(beginTime, DateUtil.DATE_PATTERN_01) + " 结束时间：" + DateUtil.DateToString(endTime, DateUtil.DATE_PATTERN_01));
            //不区分通道
            List<FinanceOverviewDto> overview = shopPayDao.getOverViewInfo(beginTime, endTime);
            Set<String> agentCodes = redisClient.GetWhereKeys(RedisConfig.AGENT_INFO_DB,"*");
            for (String agentCode: agentCodes
                    ) {
                List<FinanceOverviewDto> overview2 = shopPayDao.getAgentOverViewInfo(beginTime, endTime, agentCode);
                overview.addAll(overview2);
            }
            Set<String> merCodes = redisClient.GetWhereKeys(RedisConfig.MERCHANT_INFO_DB,"*");
            for (String merCode: merCodes
                    ) {
                List<FinanceOverviewDto> overview3 = shopPayDao.getMerchantOverViewInfo(beginTime, endTime, merCode);
                overview.addAll(overview3);
            }

            for (FinanceOverviewDto finance: overview
                    ) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, finance.getUserCode());
                if(map == null || map.size()==0) {
                    this.initMonth();
                    map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, finance.getUserCode());
                }
                RedisFinanceDto redisFinanceDto = RedisFinanceDto.map2Object(map);

                //总利润
                redisFinanceDto.setTotalCharge(finance.getTotalCharge());
                redisFinanceDto.setDailyTotalCharge(0D);

                //总交易额
                redisFinanceDto.setTotalMoney(finance.getTotalMoney());
                redisFinanceDto.setDailyTotalMoney(0D);

                //总订单数
                redisFinanceDto.setTotalOrder(finance.getTotalOrder());
                redisFinanceDto.setDailyTotalOrder(0);

                //总成功订单数
                redisFinanceDto.setTotalSuccessOrder(finance.getTotalSuccessOrder());
                redisFinanceDto.setDailyTotalSuccessOrder(0);

                //总代付笔数
                redisFinanceDto.setTotalPaid(finance.getTotalPaid());
                redisFinanceDto.setDailyTotalPaid(0);

                //总成功代付笔数
                redisFinanceDto.setTotalSuccessPaid(finance.getTotalSuccessPaid());
                redisFinanceDto.setDailyTotalSuccessPaid(0);

                //总风控订单
                redisFinanceDto.setTotalRiskControlOrder(finance.getTotalRiskControlOrder());
                redisFinanceDto.setDailyTotalRiskControlOrder(0);

                redisClient.SetHsetJedis(RedisConfig.ORDER_COUNT_DB, finance.getUserCode(), MyBeanUtil.transBean2Map2(redisFinanceDto));
                logger.info("刷新昨天的Redis数据【order count】--------------userCode:" + finance.getUserCode());
            }
            shopPayDao.updateStep();

            //区分通道
            for (String merCode: merCodes
                    ) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merCode);
                if(StringUtils.isNotBlank(map.get("sysUserId"))) {
                    List<ChannelSummaryDto> summaryDtos = shopAccountRecordService.channelSummary(beginTime, endTime, map.get("sysUserId"));
                    for (ChannelSummaryDto summary: summaryDtos
                            ) {
                        String jsonStr = redisClient.Gethget(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, merCode, summary.getChannelId());
                        Map<String, String> merchantChannel = JSONObject.parseObject(jsonStr, HashMap.class);
                        RedisChannelSummaryDto redisChannelSummaryDto = RedisChannelSummaryDto.map2Object(merchantChannel);

                        //总利润
                        redisChannelSummaryDto.setTotalCharge(summary.getTotalCharge());
                        redisChannelSummaryDto.setDailyTotalCharge(0D);

                        //总交易额
                        redisChannelSummaryDto.setTotalMoney(summary.getTotalMoney());
                        redisChannelSummaryDto.setDailyTotalMoney(0D);

                        //总代付
                        redisChannelSummaryDto.setTotalPaidMoney(summary.getTotalPaidMoney());
                        redisChannelSummaryDto.setDailyTotalPaidMoney(0D);

                        //总订单数
                        redisChannelSummaryDto.setTotalOrder(summary.getTotalOrder());
                        redisChannelSummaryDto.setDailyTotalOrder(0);

                        //总成功订单数
                        redisChannelSummaryDto.setTotalSuccessOrder(summary.getTotalSuccessOrder());
                        redisChannelSummaryDto.setDailyTotalSuccessOrder(0);

                        //总代付笔数
                        redisChannelSummaryDto.setTotalPaid(summary.getTotalPaid());
                        redisChannelSummaryDto.setDailyTotalPaid(0);

                        //总成功代付笔数
                        redisChannelSummaryDto.setTotalSuccessPaid(summary.getTotalSuccessPaid());
                        redisChannelSummaryDto.setDailyTotalSuccessPaid(0);

                        //总风控订单
                        redisChannelSummaryDto.setTotalRiskControlOrder(summary.getTotalRiskControlOrder());
                        redisChannelSummaryDto.setDailyTotalRiskControlOrder(0);

                        redisClient.SetHsetJedis(RedisConfig.MERCHANT_CHANNEL_COUNT_DB, merCode, summary.getChannelId(),
                                JSONObject.toJSONString(MyBeanUtil.transBean2Map2(redisChannelSummaryDto)));
                    }
                    logger.info("刷新昨天的Redis数据【merchant channel count】--------------merCode:" + merCode);
                }
            }
            shopAccountRecordService.updateStep();
            return ReturnVo.returnSuccess();
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnFail();
        }
    }


}
