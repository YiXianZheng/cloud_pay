package com.cloud.finance.controller;

import com.cloud.agent.provider.AgentUserProvider;
import com.cloud.finance.common.enums.PayStatusEnum;
import com.cloud.finance.common.service.MerchantPayService;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.service.base.PayServiceFactory;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.FinanceService;
import com.cloud.finance.service.ShopPayService;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.Util;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/8/17 14:34
 * @Description:
 */
@RestController
@RequestMapping(value = "/finance")
public class FinanceController extends BaseController {

    @Autowired
    private FinanceService financeService;
    @Autowired
    private ShopPayService shopPayService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private PayServiceFactory payServiceFactory;
    @Autowired
    private AgentUserProvider agentUserProvider;
    @Autowired
    private MerchantUserProvider merchantUserProvider;
    @Autowired
    private MerchantPayService merchantPayService;

    /**
     * 每月将一个月之前的订单数据移至历史库
     *      e.g. 加入今天是八月某日，那么就把六月一号之前的数据移至历史库
     * @return
     */
    @PostMapping(value = "/order/monthMove")
    public ApiResponse monthMove(){
        try{
            return this.toApiResponse(shopPayService.monthMove());
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 每天凌晨更新昨日的订单  step 1  -->  step 2
     *      并更新redis统计的缓存数据
     * @return
     */
    @PostMapping(value = "/order/dailyUpdate")
    public ApiResponse dailyUpdate(){
        try{
            return this.toApiResponse(shopPayService.dailyUpdate());
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 每天凌晨更新个月的订单  step 1  -->  step 2
     *      并更新redis统计的缓存数据
     * @return
     */
    @PostMapping(value = "/order/initMonth")
    public ApiResponse initMonth(){
        try{
            shopPayService.initMonth();
            return ApiResponse.creatSuccess(ResponseCode.Base.SUCCESS);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 生成财务分析页概览
     *      统计当天数据并与Redis中的统计数据接合
     * @return
     */
    @PostMapping(value = "/initOverview")
    public ApiResponse initOverview(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        String userCode = Constant.SYS_ACCOUNT_NO;
        int type = 1;
        if(StringUtils.isNotBlank(headerInfoDto.getAgentUser())){
            ApiResponse apiResponse = agentUserProvider.detailById(headerInfoDto.getAgentUser());
            if (apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())) {
                Map agentInfo = Util.toHashMap((LinkedHashMap) apiResponse.getData());
                userCode = agentInfo.get("agentCode").toString();
            } else {
                return null;
            }
            type = 2;
        }else if(StringUtils.isNotBlank(headerInfoDto.getMerchantUser())){
            ApiResponse apiResponse = merchantUserProvider.detailById(headerInfoDto.getMerchantUser());
            if(apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())){
                Map merchantInfo = Util.toHashMap((LinkedHashMap) apiResponse.getData());
                userCode = merchantInfo.get("merchantCode").toString();
            }else{
                return null;
            }
            type = 3;
        }

        try{
            return this.toApiResponse(financeService.initOverview(type, userCode));
        } catch (Exception e){
            e.printStackTrace();
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 财务分析页概览
     * @return
     */
    @PostMapping(value = "/overview")
    public ApiResponse overview(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        String userCode = Constant.SYS_ACCOUNT_NO;
        int type = 1;
        if(StringUtils.isNotBlank(headerInfoDto.getAgentUser())){
            ApiResponse apiResponse = agentUserProvider.detailById(headerInfoDto.getAgentUser());
            if (apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())) {
                Map agentInfo = Util.toHashMap((LinkedHashMap) apiResponse.getData());
                userCode = agentInfo.get("agentCode").toString();
            } else {
                return null;
            }
            type = 2;
        }else if(StringUtils.isNotBlank(headerInfoDto.getMerchantUser())){
            ApiResponse apiResponse = merchantUserProvider.detailById(headerInfoDto.getMerchantUser());
            if(apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())){
                Map merchantInfo = Util.toHashMap((LinkedHashMap) apiResponse.getData());
                userCode = merchantInfo.get("merchantCode").toString();
            }else{
                return null;
            }
            type = 3;
        }

        try{
            return this.toApiResponse(financeService.overview(type, userCode));
        } catch (Exception e){
            e.printStackTrace();
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 生成通道财务分析页概览
     *      统计当天数据并与Redis中的统计数据接合
     * @return
     */
    @PostMapping(value = "/initChannelOverview")
    public ApiResponse initChannelOverview(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            return this.toApiResponse(financeService.initChannelOverview(headerInfoDto));
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 通道财务分析页概览
     * @return
     */
    @PostMapping(value = "/channelOverview")
    public ApiResponse channelOverview(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            return this.toApiResponse(financeService.channelOverview(headerInfoDto));
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 商户排名数据
     * @return
     */
    @PostMapping(value = "/merchantRanking")
    public ApiResponse merchantRanking(){
        try{
            return this.toApiResponse(financeService.merchantRanking());
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 通道图形数据
     * @return
     */
    @PostMapping(value = "/channelGraph")
    public ApiResponse channelGraph(){
        try{
            return this.toApiResponse(financeService.channelGraph());
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 分页查询平台订单  最近两个月的数据 （本月和上个月一整个月）
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping(value = "/orderPage")
    public ApiResponse orderPage(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            return this.toApiResponse(shopPayService.listForTablePage(pageQuery, headerInfoDto));
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 冻结订单
     * @param headers
     * @param orderNo
     * @param status      1 冻结  2 解冻
     * @return
     */
    @PostMapping(value = "/orderFrozen")
    public ApiResponse orderFrozen(@RequestHeader HttpHeaders headers, @RequestParam String orderNo, @RequestParam Integer status){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = shopPayService.orderFrozen(orderNo, status, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }


    /**
     * 分页查询商户订单
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping(value = "/merchant/orderPage")
    public ApiResponse merchantOrderPage(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            Map<String, Object> params = pageQuery.getParams();
            if(params == null)
                params = new HashMap<>();
            params.put("sysUserId", headerInfoDto.getCurUserId());
            pageQuery.setParams(params);

            return this.toApiResponse(shopPayService.listForTablePage(pageQuery, headerInfoDto));
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }


    /**
     * 通道余额查询
     * @param channelId   通道id
     * @return
     */
    @PostMapping("/query")
    public ApiResponse query(@RequestParam String channelId){
        Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, channelId);
        ThirdChannelDto channelDto = ThirdChannelDto.map2Object(map);

        //通过第三方通道的编码获取对应通道的实现类
        BasePayService basePayService = payServiceFactory.getPayment(channelDto.getChannelCode());
        if(basePayService == null){
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前通道不可用")));
        }
        ChannelAccountData data = basePayService.queryAccount(channelDto);
        if(ChannelAccountData.STATUS_SUCCESS.equals(data.getStatus())){
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), data.getMsg()), data));
        }else{
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), data.getMsg())));
        }
    }

    /**
     * 订单手动补发通知
     * @param sysPayOrderNo   订单号
     * @return
     */
    @PostMapping("/reNotify")
    public ApiResponse reNotify(@RequestParam("sysPayOrderNo") String sysPayOrderNo, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        logger.info("【手动补通知】 ---------------  ");
        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            logger.info("【手动补通知】 没有操作权限");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                    "没有操作权限")));
        }
        try {
            ShopPay shopOrder = shopPayService.getBySysOrderNo(sysPayOrderNo);
            //回调成功
            if (shopOrder != null) {
                shopOrder.setThirdChannelRespMsg("手动补发支付成功通知");
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo("");
                shopOrder.setRemarks("手动补发通知");

                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopPayService.updateOrderStatus(shopOrder);

                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(),
                            "通知补发成功")));
                } else {
                    logger.info("【手动补通知】 补通知失败");
                    return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                            "通知补发失败")));
                }

            } else {
                logger.info("【手动补通知】 找不到订单");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                        "找不到订单")));
            }
        } catch (Exception e){
            logger.info("【手动补通知】 系统异常");
            return toApiResponse(ReturnVo.returnError(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                    "通知补发异常")));
        }
    }


    /**
     * 统计商户昨日的交易数据
     *      并更新redis统计的缓存数据
     * @return
     */
    @PostMapping(value = "/order/dailySummary")
    public ApiResponse dailySummary(){
        try{
            return this.toApiResponse(shopPayService.dailySummary());
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     *   每日汇总数据
     * @param headers
     * @return
     */
    @PostMapping(value = "/order/data/dailySummary")
    public ApiResponse dailySummaryData(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            return this.toApiResponse(shopPayService.getDailySummary(headerInfoDto));
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 人工完结订单
     * @param sysPayOrderNo
     * @param headers
     * @return
     */
    @PostMapping(value = "/order/updateStatus")
    public ApiResponse updateOrderStatus(@RequestParam("sysPayOrderNo") String sysPayOrderNo, @RequestHeader HttpHeaders headers) {
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        logger.info("【人工完结订单】 ---------------  ");
        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            logger.info("【人工完结订单】 没有操作权限");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                    "没有操作权限")));
        }
        try {
            ShopPay shopOrder = shopPayService.getBySysOrderNo(sysPayOrderNo);
            // 人工完结订单为已付款   只能修改未付款
            if (shopOrder != null) {
                if (shopOrder.getPayStatus() == 0) {
                    shopOrder.setPayStatus(1);
                    shopOrder.setPayCompleteTime(new Date());
                    shopOrder.setRemarks("人工完结订单");
                    shopPayService.updateOrderStatus(shopOrder);

                    //通知商户
                    boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                    if (notifyResult) {
                        return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(),
                                "订单完结成功")));
                    } else {
                        return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                                "订单完结失败")));
                    }
                } else {
                    logger.info("【人工完结订单】 当前状态不可操作");
                    return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                            "当前状态不可操作")));
                }
            } else {
                logger.info("【人工完结订单】 找不到订单");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                        "找不到订单")));
            }

        } catch (Exception e) {
            logger.info("【人工完结订单】 系统异常");
            return toApiResponse(ReturnVo.returnError(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),
                    "人工完结订单异常")));
        }
    }

    /**
     * 商户每日通道累计数据
     * @param headers
     * @return
     */
    @PostMapping(value = "/order/merchant/channelSummary")
    public ApiResponse getChannelDailySummary(@RequestHeader HttpHeaders headers) {
        logger.info("【商户每日通道累计数据】");

        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            return this.toApiResponse(shopPayService.getChannelDailySummary(headerInfoDto));
        } catch (Exception e){
            e.printStackTrace();
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }
//    /**
//     * 每天凌晨更新昨日的订单  step 1  -->  step 2
//     *      并更新redis统计的缓存数据
//     * @return
//     */
//    @PostMapping(value = "/order/dailyUpdate")
//    public ApiResponse dailyUpdate(@RequestHeader HttpHeaders headers){
//        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
//        try{
//            if("1".equals(headerInfoDto.getRoleId()) && HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())) {
//                return this.toApiResponse(shopPayService.dailyUpdate());
//            }else{
//                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前通道不可用")));
//            }
//        } catch (Exception e){
//            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
//        }
//    }

}
