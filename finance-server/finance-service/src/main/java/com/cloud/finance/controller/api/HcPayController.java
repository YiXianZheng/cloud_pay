package com.cloud.finance.controller.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.enums.*;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.service.base.PayServiceFactory;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.common.vo.pay.req.RepPayCreateData;
import com.cloud.finance.common.vo.pay.req.ReqPayQueryData;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.FinanceService;
import com.cloud.finance.service.ShopAccountRecordService;
import com.cloud.finance.service.ShopPayService;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.Util;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 通道测试
 */
@RestController
@RequestMapping(value = "/hc")
public class HcPayController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(HcPayController.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private FinanceService financeService;
    @Autowired
    private ShopPayService shopPayService;
    @Autowired
    private PayServiceFactory payServiceFactory;
    @Autowired
    private ShopAccountRecordService shopAccountRecordService;

    /**
     * 商户支付接口
     * @param request
     * @return
     */
    @PostMapping("/pay")
    public String pay(HttpServletRequest request, HttpServletResponse response, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            logger.info("[test pay] auth:" + headerInfoDto.getAuth());
            return renderFailString(response, false, SysPayResultConstants.ERROR_SYS_PARAMS, "权限异常");
        }

        long startTime = System.currentTimeMillis();   //获取开始时间
        RepPayCreateData req = null;
        String merchantCode = this.getStringParameter("assCode");                //商户编号
        String merchantPayOrderNo = this.getStringParameter("assPayOrderNo");    //商户订单号
        String merchantNotifyUrl = this.getStringParameter("assNotifyUrl");    //商户回调地址
        String merchantReturnUrl = this.getStringParameter("assReturnUrl");    //商户返回地址
        String merchantCancelUrl = this.getStringParameter("assCancelUrl");    //商户取消支付跳转地址
        String paymentType = this.getStringParameter("paymentType");        //支付类型
        String subPayCode = this.getStringParameter("subPayCode");        //子支付类型
        String merchantPayMoney = this.getStringParameter("assPayMoney");        //以分为单位
        Double merchantPayMoneyYuan = SafeComputeUtils.div(this.getDoubleParameter("assPayMoney"), 100D);    //以元为单位
        //不参与签名
        String merchantPayMessage = this.getStringParameter("assPayMessage");  //商户保留字段
        String merchantGoodsTitle = this.getStringParameter("assGoodsTitle");    //支付产品标题
        String merchantGoodsDesc = this.getStringParameter("assGoodsDesc");    //支付产品描述
        String sign = request.getParameter("sign");                        //商户签名结果

        Integer source = Util.isMobileDevice(request)?1:2;

        if (StringUtils.isEmpty(merchantCode)) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_MERCHANT_ID_NULL, "[assCode]商户编号号不能为空");
        }
        if (StringUtils.isEmpty(merchantPayOrderNo)) {
            merchantPayOrderNo = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13);
        }

        if (StringUtils.isEmpty(paymentType)) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_MENT_TYPE_NULL, "[paymentType]支付类型参数为空");
        }
        if (StringUtils.isEmpty(SysPaymentTypeEnum.getLabelMap().get(paymentType))) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_MENT_TYPE_NOT_RIGHT, "[paymentType]支付类型参数不正确");
        }
        if (StringUtils.isEmpty(subPayCode)) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_MENT_TYPE_NULL, "[subPayCode]支付类型参数为空");
        }
        if (StringUtils.isEmpty(merchantNotifyUrl)) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_BACK_URL_NULL, "[assNotifyUrl]商户回调通知地址不能为空");
        }
        if (StringUtils.isEmpty(merchantReturnUrl)) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_BACK_URL_NULL, "[assReturnUrl]商户支付成功地址不能为空");
        }
        if (StringUtils.isEmpty(merchantCancelUrl)) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_BACK_URL_NULL, "[assCancelUrl]商户支付取消地址不能为空");
        }


        Map<String, String> merchantInfoDto = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
        String merchantMd5Key = merchantInfoDto.get("md5Key");

        try {
            req = new RepPayCreateData(merchantCode, merchantPayOrderNo, merchantNotifyUrl, merchantReturnUrl,
                    merchantCancelUrl, paymentType, subPayCode, merchantPayMoney, merchantPayMessage, merchantGoodsTitle,
                    merchantGoodsDesc, merchantMd5Key);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("[create pay url failed sign error exception],assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo);
            return renderFailString(response, false, SysPayResultConstants.ERROR_SIGN_RESULT_EXCEPTION, "[sign]MD5签名异常");
        }

        ShopPayDto shopPayDto = null;
        try {
            if(StringUtils.isNotBlank(sign) && !sign.equals(req.getSign())){
                logger.error("[create pay url failed sign error exception],assCode:"+merchantCode+",assPayOrderNo:"+merchantPayOrderNo);
                return renderFailString(response, false, SysPayResultConstants.ERROR_SIGN_RESULT_ERROR,"[sign]MD5签名错误");
            }

            if (shopPayService.checkExist(merchantCode, merchantPayOrderNo)) {
                //订单已经存在,请不要重复支付
                logger.error("[create pay url failed] 商户订单号重复 ,assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo);
                return renderFailString(response, false, SysPayResultConstants.ERROR_MERCHANT_ORDER_ID_REPEAT, "[assPayOrderNo]商户订单号重复");
            } else {

                long checkTime = System.currentTimeMillis();
                logger.info("[create pay url checked]:cost-" + (checkTime - startTime) + "ms,params:" + JSON.toJSONString(req));
                //生成支付地址
                MidPayCreateResult resValue = new MidPayCreateResult();

                String payTypeStr = SysPaymentTypeEnum.getLabelMap().get(paymentType);
                ThirdChannelDto thirdChannelDto = channelRoute(merchantCode, paymentType, merchantPayMoneyYuan);
                if(thirdChannelDto == null){
                    return renderFailString(response,false,SysPayResultConstants.ERROR_PAY_CHANNEL_NULL,"[paymentType]["+payTypeStr+"]无可用通道");
                }

                Double channelPayMin = thirdChannelDto.getPayPerMin();
                if (merchantPayMoneyYuan < channelPayMin) {
                    logger.info("[create pay url failed ：channel min pay limit] 通道最低限额 :"+ channelPayMin);
                    return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_AMOUNT_PARAM, "["+thirdChannelDto.getId()+"]支付金额小于"+channelPayMin+"元");
                }
                Double channelPayMax = thirdChannelDto.getPayPerMax();
                if (merchantPayMoneyYuan > channelPayMax) {
                    logger.info("[create pay url failed ：channel max pay limit] 通道最高限额 :"+ channelPayMax);
                    return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_AMOUNT_PARAM, "["+thirdChannelDto.getId()+"]单笔支付金额大于"+channelPayMax+"元");
                }

                //通过第三方通道的编码获取对应通道的实现类
                BasePayService basePayService = payServiceFactory.getPayment(thirdChannelDto.getChannelCode());
                if(basePayService == null){
                    logger.info("[create pay url failed ：channel usable] 无可用通道-----");
                    return renderFailString(response,false,SysPayResultConstants.ERROR_PAY_CHANNEL_NULL,"[paymentType]["+payTypeStr+"]无可用通道");
                }

                //开启了通道风控的花，支付金额随机上上一个值
                if(thirdChannelDto.getOpenRandom() != null && thirdChannelDto.getOpenRandom() != 0){
                    logger.info("[create pay url] 开启通道风控");
                    int min = thirdChannelDto.getRandomMin();
                    int max = thirdChannelDto.getRandomMax();
                    Random random = new Random();

                    int intRandom = random.nextInt(max-min) + min;
                    if(thirdChannelDto.getOpenRandom() == 1){
                        Double randomMoney = new Double(intRandom);
                        merchantPayMoneyYuan = SafeComputeUtils.add(merchantPayMoneyYuan, randomMoney);
                        logger.info("[create pay url] 开启通道风控 风控金额[整数]："+ randomMoney);
                    }else if(thirdChannelDto.getOpenRandom() == 2){
                        double temp = random.nextDouble() + intRandom;
                        Double randomMoney = new Double(SafeComputeUtils.numberFormate(temp));
                        merchantPayMoneyYuan = SafeComputeUtils.add(merchantPayMoneyYuan, randomMoney);
                        logger.info("[create pay url] 开启通道风控 风控金额[小数]："+ randomMoney);
                    }

                }

                shopPayDto = financeService.beforeCreateOrder(merchantCode, merchantPayOrderNo, merchantNotifyUrl, merchantReturnUrl,
                        merchantCancelUrl, paymentType, subPayCode, merchantPayMoneyYuan, merchantPayMessage, merchantGoodsTitle,
                        merchantGoodsDesc, merchantMd5Key, SysOrderTypeEnum.SYS_ORDER_TYPE_RECHARGE.getType(), source);

                shopPayDto.setSuccessFlag(ShopPayDto.SUCCESS_FLAG_YES);
                shopPayService.save(shopPayDto, headerInfoDto);

                //根据支付方式调用接口
                if(paymentType.equals(SysPaymentTypeEnum.WX_QR_CODE.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.ALI_QR_CODE.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.QQ_QR_CODE.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.JD_QR_CODE.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.GATE_QR_CODE.getValue())){

                    //扫码接口
                    resValue = basePayService.createQrCode(thirdChannelDto, shopPayDto);
                }else if(paymentType.equals(SysPaymentTypeEnum.WX_SELF_PAY.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.QQ_SELF_PAY.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.JD_SELF_PAY.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.ALI_SELF_PAY.getValue())){
                    //服务号接口
                    resValue = basePayService.createAppJumpUrl(thirdChannelDto, shopPayDto);
                }else if(paymentType.equals(SysPaymentTypeEnum.WX_H5_JUMP.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.QQ_H5_JUMP.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.JD_H5_JUMP.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.ALI_H5_JUMP.getValue())||
                        paymentType.equals(SysPaymentTypeEnum.GATE_H5.getValue())){

                    //H5接口
                    resValue = basePayService.createH5JumpUrl(thirdChannelDto, shopPayDto);
                }else if(paymentType.equals(SysPaymentTypeEnum.GATE_WEB_DIRECT.getValue())){

                    //网关直连
                    resValue = basePayService.createGateDirectJumpUrl(thirdChannelDto, shopPayDto);
                }else if(paymentType.equals(SysPaymentTypeEnum.GATE_WEB_SYT.getValue())){

                    //网关收银台
                    resValue = basePayService.createGateSytJump(thirdChannelDto, shopPayDto);
                }else if(paymentType.equals(SysPaymentTypeEnum.SYT_ALL_IN.getValue())){

                    //聚合收银台
                    resValue = basePayService.createSytAllIn(thirdChannelDto, shopPayDto);
                }else{
                    return renderFailString(response,false,SysPayResultConstants.ERROR_PAY_TYPE_NOT_SUPPORT,"["+payTypeStr+"]支付类型暂不支持");
                }

                ShopPay shopPay = shopPayService.getBySysOrderNo(shopPayDto.getSysPayOrderNo());
                shopAccountRecordService.addRecord(AccountRecordTypeEnum.ACCOUNT_RECORD_TYPE_PAY.getCode(), shopPay,
                        null, AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_DOING.getCode());

                Map<String, String> map = new HashMap<>();
                map.put("success", resValue.getStatus());
                map.put("message", "["+ payTypeStr +"]" + resValue.getResultMessage());
                map.put("code", resValue.getResultCode());
                map.put("payUrl", resValue.getPayUrl());
                map.put("assPayOrderNo", merchantPayOrderNo);
                map.put("sysPayOrderNo", shopPay.getSysPayOrderNo());
                map.put("money", merchantPayMoneyYuan + "");
                String txnTimeString = new SimpleDateFormat("yyyyMMdd").format(new Date());
                map.put("alertOrderId", resValue.getSysOrderNo().replace(txnTimeString, ""));

                long endTime = System.currentTimeMillis(); //获取结束时间
                logger.info("[create pay url success]:cost-" + (endTime - startTime) + "ms,assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo + ",paymentType:" + paymentType + ",assPayMoneyYuan:" + merchantPayMoneyYuan + ",sys order id:" + resValue.getSysOrderNo());

                return renderString(response, map);
            }
        } catch (Exception e) {
            if(shopPayDto != null) {
                shopPayDto.setSuccessFlag(ShopPayDto.SUCCESS_FLAG_NO);
                shopPayService.save(shopPayDto, headerInfoDto);
            }

            Map<String, String> map = new HashMap<>();
            map.put("message", "生成" + SysPaymentTypeEnum.getLabelMap().get(paymentType) + "支付链接失败");
            map.put("success", "false");
            map.put("code", SysPayResultConstants.ERROR_PAY_CHANNEL_NULL + "");
            map.put("assPayOrderNo", merchantPayOrderNo);
            logger.info("[create pay url failed]:cost-" + (System.currentTimeMillis() - startTime) + "ms,assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo + ",paymentType:" + paymentType + ",assPayMoneyYuan:" + merchantPayMoneyYuan);

            return renderString(response, map);
        }
    }

    /**
     * 手动补单
     * @param request
     * @param response
     * @param headers
     * @return
     */
    @PostMapping("/createOrder")
    public ApiResponse createPay(HttpServletRequest request, HttpServletResponse response, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            logger.info("[手动做单失败] auth:" + headerInfoDto.getAuth());
            return toApiResponse(ReturnVo.returnFail(ResponseCode.Base.AUTH_ERR));
        }

        long startTime = System.currentTimeMillis();   //获取开始时间
        String merchantCode = this.getStringParameter("assCode");                //商户编号
        String merchantPayOrderNo = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13);    //商户订单号
        String paymentType = this.getStringParameter("paymentType");        //支付类型
        String subPayCode = this.getStringParameter("subPayCode");        //子支付类型
        Double merchantPayMoney = this.getDoubleParameter("assPayMoney");    //充值金额
        String merchantNotifyUrl = "notifyUrl";    //商户回调地址
        String merchantReturnUrl = "returnUrl";    //商户返回地址
        String merchantCancelUrl = "cancelUrl";    //商户取消支付跳转地址
        //不参与签名
        String merchantPayMessage = "message";  //商户保留字段
        String merchantGoodsTitle = "title";    //支付产品标题
        String merchantGoodsDesc = "desc";    //支付产品描述

        String channelId = this.getStringParameter("channelId");                //通道ID

        Integer source = 3;

        if (StringUtils.isEmpty(merchantCode)) {
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "[assCode]商户编号号不能为空")));
        }
        if (StringUtils.isEmpty(merchantPayOrderNo)) {
            merchantPayOrderNo = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13);
        }

        if (StringUtils.isEmpty(paymentType)) {
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "[paymentType]支付类型参数为空")));
        }
        if (StringUtils.isEmpty(SysPaymentTypeEnum.getLabelMap().get(paymentType))) {
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "[paymentType]支付类型参数不正确")));
        }
        if (StringUtils.isEmpty(subPayCode)) {
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "[subPayCode]支付类型参数为空")));
        }


        Map<String, String> merchantInfoDto = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
        String merchantMd5Key = merchantInfoDto.get("md5Key");

        ShopPayDto shopPayDto = financeService.beforeCreateOrder(merchantCode, merchantPayOrderNo, merchantNotifyUrl, merchantReturnUrl,
                merchantCancelUrl, paymentType, subPayCode, merchantPayMoney, merchantPayMessage, merchantGoodsTitle,
                merchantGoodsDesc, merchantMd5Key, SysOrderTypeEnum.SYS_ORDER_TYPE_RECHARGE.getType(), source);

        try {
            shopPayDto.setSuccessFlag(ShopPayDto.SUCCESS_FLAG_YES);
            shopPayService.save(shopPayDto, headerInfoDto);

            long checkTime = System.currentTimeMillis();
            logger.info("[手动做单]:checked cost-" + (checkTime - startTime) + "ms");
            //生成支付地址
            MidPayCreateResult resValue = new MidPayCreateResult();

            ThirdChannelDto thirdChannelDto = null;
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, channelId);
            if(map.get("channelType") != null && "1".equals(map.get("channelType").toString())){
                thirdChannelDto = ThirdChannelDto.map2Object(map);
            }
            if(thirdChannelDto == null){
                logger.info("[手动做单失败] 通道不可用");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "通道不可用")));
            }

            Double channelPayMin = thirdChannelDto.getPayPerMin();
            if (merchantPayMoney < channelPayMin) {
                logger.info("[手动做单失败：channel min pay limit] 通道最低限额 :"+ channelPayMin);
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "["+thirdChannelDto.getId()+"]支付金额小于"+channelPayMin+"元")));
            }
            Double channelPayMax = thirdChannelDto.getPayPerMax();
            if (merchantPayMoney > channelPayMax) {
                logger.info("[手动做单失败 ：channel max pay limit] 通道最高限额 :"+ channelPayMax);
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),"["+thirdChannelDto.getId()+"]单笔支付金额大于"+channelPayMax+"元")));
            }

            shopPayService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());

            ShopPay shopPay = shopPayService.getBySysOrderNo(shopPayDto.getSysPayOrderNo());
            shopPay.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
            shopPay.setPayCompleteTime(new Date());
            shopPay.setThirdChannelRespMsg("手动补单");
            shopPay.setThirdChannelNotifyFlag(1);
            shopPay.setThirdChannelOrderNo("");
            shopPay.setRemarks("手动补单");
            shopPayService.updateOrderStatus(shopPay);

            shopAccountRecordService.addRecord(AccountRecordTypeEnum.ACCOUNT_RECORD_TYPE_PAY.getCode(), shopPay,
                    null, AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_DOING.getCode());

            long endTime = System.currentTimeMillis(); //获取结束时间
            logger.info("[手动做单成功]:cost-" + (endTime - startTime) + "ms,assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo + ",paymentType:" + paymentType + ",assPayMoneyYuan:" + merchantPayMoney + ",sys order id:" + resValue.getSysOrderNo());

            return toApiResponse(ReturnVo.returnSuccess(shopPay.getSysPayOrderNo()));

        } catch (Exception e) {
            if(shopPayDto != null) {
                shopPayDto.setSuccessFlag(ShopPayDto.SUCCESS_FLAG_NO);
                shopPayService.save(shopPayDto, headerInfoDto);
            }

            logger.info("[手动做单失败]:cost-" + (System.currentTimeMillis() - startTime) + "ms,assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo + ",paymentType:" + paymentType + ",assPayMoneyYuan:" + merchantPayMoney);

            return toApiResponse(ReturnVo.returnSuccess(shopPayDto!=null?shopPayDto.getSysPayOrderNo():null));
        }
    }


    /**
     * 验证支付结果
     * @param headers
     * @return
     */
    @PostMapping("/checkResult")
    public ApiResponse checkResult(@RequestHeader HttpHeaders headers){
        long startTime = System.currentTimeMillis();
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth()) && !HeaderInfoDto.AUTH_MERCHANT_SYSTEM.equals(headerInfoDto.getAuth())){
            logger.info("[pay result checked failed] auth:" + headerInfoDto.getAuth());
            return toApiResponse(ReturnVo.returnFail(ResponseCode.Base.AUTH_ERR));
        }
        String merchantCode = null;
        String merchantPayOrderNo = null;
        try {
            //签名结果
            merchantCode =this.getStringParameter("assCode");
            merchantPayOrderNo =this.getStringParameter("assPayOrderNo");
            if(StringUtils.isEmpty(merchantCode)){
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),"[assCode]商户号不能为空")));
            }

            if(StringUtils.isEmpty(merchantPayOrderNo)){
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),"[assPayOrderNo]商户订单号不能为空")));
            }


            Map<String, String> merchantInfoDto = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
            if(null==merchantInfoDto){
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),"[assCode]商户号未注册")));
            }
            ShopPay shopPay = shopPayService.getByMerchantCodeAndOrderNo(merchantCode, merchantPayOrderNo);

            if(shopPay==null){
                logger.info("[pay result checked failed] 订单不存在");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),"订单不存在")));
            }
            if(StringUtils.isBlank(shopPay.getThirdChannelId())){
                logger.info("[pay result checked failed] 通道异常;  channelId 为空");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),"通道异常")));
            }

            long checkTime = System.currentTimeMillis();
            logger.info("[pay result checked]:cost-" + (checkTime - startTime) + "ms");
            ThirdChannelDto thirdChannelDto = null;
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopPay.getThirdChannelId());
            if(map.get("channelType") != null && "1".equals(map.get("channelType").toString())){
                thirdChannelDto = ThirdChannelDto.map2Object(map);
            }
            if(thirdChannelDto == null){
                logger.info("[pay result checked failed] 通道不可用");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),"通道不可用")));
            }

            //通过第三方通道的编码获取对应通道的实现类
            BasePayService basePayService = payServiceFactory.getPayment(thirdChannelDto.getChannelCode());
            if(basePayService == null){
                logger.info("[pay result checked failed] 通道不支持此方法 channelId:" + thirdChannelDto.getChannelCode());
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),"通道不支持此方法")));
            }

            MidPayCheckResult checkResult = basePayService.checkOrderResult(thirdChannelDto, shopPay);


            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("assPayOrderNo", merchantPayOrderNo);
            resultMap.put("sysPayOrderNo", shopPay.getSysPayOrderNo());
            resultMap.put("orderStatus", 1==shopPay.getPayStatus()?"10000":"10001");
            resultMap.put("tradeAmt", checkResult.getAmount());
            resultMap.put("assCode", merchantCode);
            resultMap.put("success", "true");
            resultMap.put("code", SysPayResultConstants.SUCCESS_QUERY_ORDER+"");
            resultMap.put("message", checkResult.getRespMsg());

            long endTime = System.currentTimeMillis(); //获取结束时间
            logger.info("[pay result checked success]:cost-" + (endTime - startTime) + "ms,assCode:" + merchantCode
                    + ",assPayOrderNo:" + shopPay.getMerchantOrderNo() + ",sys order no:" + shopPay.getSysPayOrderNo());

            return ApiResponse.creatSuccess(resultMap);
        } catch (Exception e) {
            logger.info("[create pay url failed]:cost-" + (System.currentTimeMillis() - startTime) + "ms,assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo);
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(),"查询发生异常")));
        }
    }

    @PostMapping("/notify")
    public String notify(HttpServletRequest request, HttpServletResponse response){
        logger.info("----------------回调成功");
        Enumeration<String> paraNames = request.getParameterNames();
        JSONObject jsonObject = new JSONObject();
        for(Enumeration<String> e=paraNames;e.hasMoreElements();){
            String thisName=e.nextElement().toString();
            String thisValue=request.getParameter(thisName);
            this.logger.info("----------------key:"+thisName+"  ----  val:"+thisValue);
            jsonObject.put(thisName, thisValue);
        }
        return "success";
    }

    /**
     * 查询支付结果
     * @param request
     * @param response
     * @param model
     * @return
     * @throws Exception
     */
    @RequestMapping("/queryOrder")
    public String queryOrder(HttpServletRequest request, HttpServletResponse response,Model model) throws Exception {
        //签名结果
        String sign =this.getStringParameter("sign");
        String merchantCode =this.getStringParameter("merId");
        String merOrderId =this.getStringParameter("merOrderId");
        if(StringUtils.isEmpty(merchantCode)){
            return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_ID_NULL,"[merId]商户号不能为空");
        }

        if(StringUtils.isEmpty(merOrderId)){
            return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_ORDER_ID_NUL,"[merOrderId]商户订单号不能为空");
        }
        if(StringUtils.isEmpty(sign)){
            return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_ORDER_ID_NUL,"[sign]签名结果不能为空");
        }


        Map<String, String> merchantInfoDto = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
        if(null==merchantInfoDto){
            return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_NOT_REGIST,"[merId]商户号未注册");
        }
        String merchantMd5Key = merchantInfoDto.get("md5Key");
        ReqPayQueryData req = null;
        try {
            req =new ReqPayQueryData(merchantCode, merOrderId, merchantMd5Key);
        } catch (Exception e) {
            return renderFailString(response,false,SysPayResultConstants.ERROR_SIGN_RESULT_EXCEPTION,"[sign]查询接口MD5签名异常");
        }
        if(!sign.equals(req.getSign())){
            return renderFailString(response,false,SysPayResultConstants.ERROR_SIGN_RESULT_ERROR,"[sign]查询接口MD5签名错误");
        }
        ShopPay result = new ShopPay();
        result = this.shopPayService.getByMerchantCodeAndOrderNo(merchantCode, merOrderId);
        if(null==result){
            //生成二维码部分
            //订单已经存在,请不要重复支付
            model.addAttribute("success", false);
            model.addAttribute("message", "查询失败订单不存在");
            model.addAttribute("code", SysPayResultConstants.ERROR_QUERY_ORDER);
            model.addAttribute("merOrderId", merOrderId);
            model.addAttribute("finalOrderId", "0000");
            model.addAttribute("ptOrderId", "0000");
            model.addAttribute("orderStatus", "");
            model.addAttribute("orderMessage","");
            model.addAttribute("transAmt","");
            model.addAttribute("merId","");
            return renderString(response, model);
        }else{
            model.addAttribute("success", true);
            model.addAttribute("message", "查询成功");
            model.addAttribute("code", SysPayResultConstants.SUCCESS_QUERY_ORDER);
            model.addAttribute("merOrderId", result.getMerchantOrderNo());
            model.addAttribute("finalOrderId", result.getSysPayOrderNo());
            model.addAttribute("ptOrderId", result.getThirdChannelOrderNo());
            model.addAttribute("orderStatus",result.getPayStatus());
            model.addAttribute("orderMessage",result.getPayStatus());
            Double transamt=SafeComputeUtils.multiply(result.getMerchantPayMoney(), 100D);
            model.addAttribute("transAmt",transamt.intValue());
            model.addAttribute("merId",merchantCode);
            return renderString(response, model);
        }
    }


    /**
     * 第三通通道选择路由
     * @param merchantCode
     * @param paymentType
     * @param money
     * @return
     */
    private ThirdChannelDto channelRoute(String merchantCode, String paymentType, Double money){
        Set<String> channels = new HashSet<>();
        //获取商户信息 判断是否有专属通道
        Map<String, String> merchant = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
        if(merchant.get("thirdChannels") != null && StringUtils.isNotBlank(merchant.get("thirdChannels").toString())){
            String temp = merchant.get("thirdChannels").toString();
            String[] idArr = temp.split(",");
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
            for (String channelId : channels) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, channelId);
                if(map.get(paymentType) != null && StringUtils.isNotBlank(map.get(paymentType))
                        && map.get("channelType") != null && "1".equals(map.get("channelType").toString())){
                    ThirdChannelDto channelDto = ThirdChannelDto.map2Object(map);
                    if(thirdChannelDto.getRouteWeight()==null || channelDto.getRouteWeight() > thirdChannelDto.getRouteWeight()){
                        channelActive = true;
                        thirdChannelDto = channelDto;
                    }
                }
            }

            return channelActive ? thirdChannelDto : null;
        }else{
            return null;
        }
    }

}
