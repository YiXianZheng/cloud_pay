package com.cloud.finance.controller.api;

import com.alibaba.fastjson.JSON;
import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.enums.AccountRecordStatusEnum;
import com.cloud.finance.common.enums.AccountRecordTypeEnum;
import com.cloud.finance.common.enums.SysOrderTypeEnum;
import com.cloud.finance.common.enums.SysPaymentTypeEnum;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.service.base.PayServiceFactory;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.common.vo.pay.req.RepPayCreateData;
import com.cloud.finance.common.vo.pay.req.ReqPayQueryData;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.FinanceService;
import com.cloud.finance.service.ShopAccountRecordService;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.ainong.utils.MD5Util;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.Util;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ebank对外服务接口
 */
@RestController
@RequestMapping(value = "/ebank")
public class EbankPayController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(EbankPayController.class);

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
    public String pay(HttpServletRequest request, HttpServletResponse response){

        long startTime = System.currentTimeMillis();   //获取开始时间
        RepPayCreateData req;
        String merchantCode = this.getStringParameter("assCode");                   //商户编号
        String merchantPayOrderNo = this.getStringParameter("assPayOrderNo");       //商户订单号
        String merchantNotifyUrl = this.getStringParameter("assNotifyUrl");         //商户回调地址
        String merchantReturnUrl = this.getStringParameter("assReturnUrl");         //商户返回地址
        String merchantCancelUrl = this.getStringParameter("assCancelUrl");         //商户取消支付跳转地址
        String paymentType = this.getStringParameter("paymentType");                //支付类型
        String subPayCode = this.getStringParameter("subPayCode");                  //子支付类型
        String merchantPayMoney = this.getStringParameter("assPayMoney");           //以分为单位
        Double merchantPayMoneyYuan = SafeComputeUtils.div(this.getDoubleParameter("assPayMoney"), 100D);    //以元为单位
        //不参与签名
        String merchantPayMessage = this.getStringParameter("assPayMessage");       //商户保留字段
        String merchantGoodsTitle = this.getStringParameter("assGoodsTitle");       //支付产品标题
        String merchantGoodsDesc = this.getStringParameter("assGoodsDesc");         //支付产品描述
        String sign = request.getParameter("sign");                                         //商户签名结果
        Integer source = Util.isMobileDevice(request)?1:2;

        //快捷支付添加字段
        //银行卡号   bankcardno
        //持卡人姓名 payeename
        //身份证号   idnumber
        //预留手机号 telephone
        if (StringUtils.isEmpty(merchantPayOrderNo)) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_MERCHANT_ORDER_ID_NULL, "[assPayOrderNo]商户订单号不能为空");
        }

        String payMin = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_MIN");
        Double minPay = StringUtils.isNotBlank(payMin)?Double.parseDouble(payMin):0D;
        if (merchantPayMoneyYuan < minPay) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_AMOUNT_PARAM, "[]支付金额小于"+payMin+"元");
        }
        String payMax = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_MAX");
        Double maxPay = StringUtils.isNotBlank(payMax)?Double.parseDouble(payMax):0D;
        if (merchantPayMoneyYuan > maxPay) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_AMOUNT_PARAM, "[]单笔支付金额超过上限");
        }
        if (StringUtils.isEmpty(paymentType)) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_MENT_TYPE_NULL, "[paymentType]支付类型参数为空");
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
        if (StringUtils.isEmpty(sign)) {
            return renderFailString(response, false, SysPayResultConstants.ERROR_SIGN_RESULT_NULL, "[sign]MD5签名结果不能为空");
        }
        logger.info("[请求的签名]：" + sign);

        Map<String, String> merchantInfoDto = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
        String merchantMd5Key = merchantInfoDto.get("md5Key");

        HeaderInfoDto headerInfoDto = new HeaderInfoDto();
        headerInfoDto.setPanId(merchantInfoDto.get("panId"));
        headerInfoDto.setCurUserId(merchantInfoDto.get("id"));

        try {
            req = new RepPayCreateData(merchantCode, merchantPayOrderNo, merchantNotifyUrl, merchantReturnUrl,
                    merchantCancelUrl, paymentType, subPayCode, merchantPayMoney, merchantPayMessage, merchantGoodsTitle,
                    merchantGoodsDesc, merchantMd5Key);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error("[create pau url failed sign error exception],assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo);
            return renderFailString(response, false, SysPayResultConstants.ERROR_SIGN_RESULT_EXCEPTION, "[sign]MD5签名异常");
        }
        ShopPayDto shopPayDto = null;
        try {
            if(!sign.equals(req.getSign())){
                logger.error("[create pay url failed MD5签名错误],assCode:"+merchantCode+",assPayOrderNo:"+merchantPayOrderNo);
                return renderFailString(response, false, SysPayResultConstants.ERROR_SIGN_RESULT_ERROR,"[sign]MD5签名错误");
            }

            if (shopPayService.checkExist(merchantCode, merchantPayOrderNo)) {
                //订单已经存在,请不要重复支付
                logger.error("[create pay url failed] 商户订单号重复,assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo);
                return renderFailString(response, false, SysPayResultConstants.ERROR_MERCHANT_ORDER_ID_REPEAT, "[assPayOrderNo]商户订单号重复");
            } else {
                long checkTime = System.currentTimeMillis();
                logger.info("[create pay url action checked]:cost-" + (checkTime - startTime) + "ms,params:" + JSON.toJSONString(req));
                //生成支付地址
                MidPayCreateResult resValue = new MidPayCreateResult();

                String payTypeStr = SysPaymentTypeEnum.getLabelMap().get(paymentType);
                ThirdChannelDto thirdChannelDto = channelRoute(merchantCode, paymentType, merchantPayMoneyYuan);
                if(thirdChannelDto == null){
                    logger.info("[create pay url failed channel usable] 无可用通道");
                    return renderFailString(response,false,SysPayResultConstants.ERROR_PAY_CHANNEL_NULL,"[paymentType]["+payTypeStr+"]无可用通道");
                }

                Double channelPayMin = thirdChannelDto.getPayPerMin();
                if (merchantPayMoneyYuan < channelPayMin) {
                    logger.info("[create pay url failed channel min pay limit] 单笔支付最低 :"+ channelPayMin);
                    return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_AMOUNT_PARAM, "["+thirdChannelDto.getId()+"]支付金额小于"+channelPayMin+"元");
                }
                Double channelPayMax = thirdChannelDto.getPayPerMax();
                if (merchantPayMoneyYuan > channelPayMax) {
                    logger.info("[create pay url failed channel max pay limit] 单笔支付最高 :"+ channelPayMax);
                    return renderFailString(response, false, SysPayResultConstants.ERROR_PAY_AMOUNT_PARAM, "["+thirdChannelDto.getId()+"]单笔支付金额大于"+channelPayMax+"元");
                }

                //通过第三方通道的编码获取对应通道的实现类
                BasePayService basePayService = payServiceFactory.getPayment(thirdChannelDto.getChannelCode());
                if(basePayService == null){
                    logger.info("[create pay url failed channel usable] 无可用通道 - ");
                    return renderFailString(response,false,SysPayResultConstants.ERROR_PAY_CHANNEL_NULL,"[paymentType]["+payTypeStr+"]无可用通道");
                }

                //开启了通道风控的话，支付金额随机上上一个值
                if(thirdChannelDto.getOpenRandom() != null && thirdChannelDto.getOpenRandom() != 0){
                    logger.info("[create pay url] 开启通道风控");
                    int min = thirdChannelDto.getRandomMin();
                    int max = thirdChannelDto.getRandomMax();
                    Random random = new Random();

                    int intRandom = random.nextInt(max-min) + min;
                    if(thirdChannelDto.getOpenRandom() == 1){
                        Double randomMoney = new Double(intRandom);
                        logger.info("[create pay url] 开启通道风控 风控金额[整数]："+ randomMoney);
                        merchantPayMoneyYuan = SafeComputeUtils.add(merchantPayMoneyYuan, randomMoney);
                    }else if(thirdChannelDto.getOpenRandom() == 2){
                        double temp = random.nextDouble() + intRandom;
                        Double randomMoney = new Double(SafeComputeUtils.numberFormate(temp));
                        logger.info("[create pay url] 开启通道风控 风控金额[小数]："+ randomMoney);
                        merchantPayMoneyYuan = SafeComputeUtils.add(merchantPayMoneyYuan, randomMoney);
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
                map.put("sysPayOrderNo", shopPayDto.getSysPayOrderNo());
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
            map.put("success", "false");
            map.put("message", "生成" + SysPaymentTypeEnum.getLabelMap().get(paymentType) + "支付链接失败");
            map.put("code", SysPayResultConstants.ERROR_PAY_CHANNEL_NULL + "");
            map.put("assPayOrderNo", merchantPayOrderNo);
            logger.error("[create pay url failed]:cost-" + (System.currentTimeMillis() - startTime) + "ms,assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo + ",paymentType:" + paymentType + ",assPayMoneyYuan:" + merchantPayMoneyYuan);

            return renderString(response, map);
        }
    }

//    /**
//     * 验证支付结果
//     * @param request
//     * @return
//     */
//    @PostMapping("/checkResult")
//    public String checkResult(HttpServletRequest request, HttpServletResponse response){
//        long startTime = System.currentTimeMillis();
//        String merchantCode = null;
//        String merchantPayOrderNo = null;
//        try {
//            //签名结果
//            String sign =this.getStringParameter("sign");
//            merchantCode =this.getStringParameter("assCode");
//            merchantPayOrderNo =this.getStringParameter("assPayOrderNo");
//            if(StringUtils.isEmpty(merchantCode)){
//                return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_ID_NULL,"[assCode]商户号不能为空");
//            }
//
//            if(StringUtils.isEmpty(merchantPayOrderNo)){
//                return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_ORDER_ID_NUL,"[assPayOrderNo]商户订单号不能为空");
//            }
//            if(StringUtils.isEmpty(sign)){
//                return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_ORDER_ID_NUL,"[sign]签名结果不能为空");
//            }
//
//
//            Map<String, String> merchantInfoDto = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
//            if(null==merchantInfoDto){
//                return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_NOT_REGIST,"[assCode]商户号未注册");
//            }
//            String merchantMd5Key = merchantInfoDto.get("md5Key");
//            ReqPayQueryData req = null;
//            try {
//                req =new ReqPayQueryData(merchantCode, merchantPayOrderNo, merchantMd5Key);
//            } catch (Exception e) {
//                return renderFailString(response,false,SysPayResultConstants.ERROR_SIGN_RESULT_EXCEPTION,"[sign]查询接口MD5签名异常");
//            }
//            if(!sign.equals(req.getSign())){
//                return renderFailString(response,false,SysPayResultConstants.ERROR_SIGN_RESULT_ERROR,"[sign]查询接口MD5签名错误");
//            }
//            ShopPay shopPay = new ShopPay();
//            shopPay = this.shopPayService.getByMerchantCodeAndOrderNo(merchantCode, merchantPayOrderNo);
//
//            if(shopPay==null){
//                logger.info("[pay result checked failed] 订单不存在");
//                return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_ORDER,"订单不存在");
//            }else if (PayStatusEnum.PAY_STATUS_ALREADY.getStatus() != shopPay.getPayStatus()){
//                logger.info("[pay result checked failed] 订单尚未完结;  订单状态[" + PayStatusEnum.getByStatus(shopPay.getPayStatus()) + "]");
//                return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_ORDER,"订单尚未完结");
//            }
//            if(StringUtils.isBlank(shopPay.getThirdChannelId())){
//                logger.info("[pay result checked failed] 通道异常;  channelId 为空");
//                return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_ORDER,"通道异常");
//            }
//
//            long checkTime = System.currentTimeMillis();
//            logger.info("[pay result checked]:cost-" + (checkTime - startTime) + "ms,params:" + JSON.toJSONString(req));
//            ThirdChannelDto thirdChannelDto = null;
//            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopPay.getThirdChannelId());
//            if(map.get("channelType") != null && "1".equals(map.get("channelType").toString())){
//                thirdChannelDto = ThirdChannelDto.map2Object(map);
//            }
//            if(thirdChannelDto == null){
//                logger.info("[pay result checked failed] 通道不可用");
//                return renderFailString(response,false,SysPayResultConstants.ERROR_PAY_CHANNEL_NULL,"通道不可用");
//            }
//
//            //通过第三方通道的编码获取对应通道的实现类
//            BasePayService basePayService = payServiceFactory.getPayment(thirdChannelDto.getChannelCode());
//            if(basePayService == null){
//                logger.info("[pay result checked failed] 通道不可用 channelId:" + thirdChannelDto.getChannelCode());
//                return renderFailString(response,false,SysPayResultConstants.ERROR_PAY_CHANNEL_NULL,"通道不可用");
//            }
//
//            MidPayCheckResult checkResult = basePayService.checkOrderResult(thirdChannelDto, shopPay);
//
//
//            Map<String, String> resultMap = new HashMap<>();
//            resultMap.put("assPayOrderNo", merchantPayOrderNo);
//            resultMap.put("sysPayOrderNo", shopPay.getSysPayOrderNo());
//            resultMap.put("orderStatus", 1==shopPay.getPayStatus()?"10000":"10001");
//            resultMap.put("tradeAmt", checkResult.getAmount());
//            resultMap.put("assCode", merchantCode);
//
//            String signMsg = ASCIISortUtil.buildSign(resultMap, "=", merchantMd5Key);
//            logger.info("-------before sing data :"+"&" + signMsg);
//
//            String signStr = MD5Util.MD5Encode("&" + signMsg);
//            logger.info("-------sign data :" +signStr);
//            resultMap.put("sign", signStr);
//            resultMap.put("success", "true");
//            resultMap.put("code", SysPayResultConstants.SUCCESS_QUERY_ORDER+"");
//            resultMap.put("message", checkResult.getRespMsg());
//
//            long endTime = System.currentTimeMillis(); //获取结束时间
//            logger.info("[pay result checked success]:cost-" + (endTime - startTime) + "ms,assCode:" + merchantCode
//                    + ",assPayOrderNo:" + shopPay.getMerchantOrderNo() + ",sys order no:" + shopPay.getSysPayOrderNo());
//
//            return renderString(response, resultMap);
//        } catch (Exception e) {
//            logger.info("[create pay url failed]:cost-" + (System.currentTimeMillis() - startTime) + "ms,assCode:" + merchantCode + ",assPayOrderNo:" + merchantPayOrderNo);
//            return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_ORDER,"查询失败");
//        }
//    }

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
        String merchantCode =this.getStringParameter("assCode");
        String merOrderId =this.getStringParameter("assPayOrderNo");
        if(StringUtils.isEmpty(merchantCode)){
            return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_ID_NULL,"[assCode]商户号不能为空");
        }

        if(StringUtils.isEmpty(merOrderId)){
            return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_ORDER_ID_NUL,"[assPayOrderNo]商户订单号不能为空");
        }
        if(StringUtils.isEmpty(sign)){
            return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_ORDER_ID_NUL,"[sign]签名结果不能为空");
        }


        Map<String, String> merchantInfoDto = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
        if(null==merchantInfoDto){
            return renderFailString(response,false,SysPayResultConstants.ERROR_QUERY_MERCHANT_NOT_REGIST,"[assCode]商户号未注册");
        }
        String merchantMd5Key = merchantInfoDto.get("md5Key");
        ReqPayQueryData req = null;
        try {
            req = new ReqPayQueryData(merchantCode, merOrderId, merchantMd5Key);
        } catch (Exception e) {
            return renderFailString(response,false,SysPayResultConstants.ERROR_SIGN_RESULT_EXCEPTION,"[sign]查询接口MD5签名异常");
        }
        if(!sign.equals(req.getSign())){
            return renderFailString(response,false,SysPayResultConstants.ERROR_SIGN_RESULT_ERROR,"[sign]查询接口MD5签名错误");
        }
        ShopPay result = new ShopPay();
        result = this.shopPayService.getByMerchantCodeAndOrderNo(merchantCode, merOrderId);
        if(null==result){
            model.addAttribute("success", "false");
            model.addAttribute("message", "订单不存在");
            model.addAttribute("code", SysPayResultConstants.ERROR_QUERY_ORDER+"");
            model.addAttribute("assPayOrderNo", merOrderId);
            model.addAttribute("sysPayOrderNo", "");
            model.addAttribute("orderStatus", "");
            model.addAttribute("tradeAmt","");
            model.addAttribute("assCode","");
            model.addAttribute("sign","");
            return renderString(response, model);
        }else{
            model.addAttribute("success", "true");
            model.addAttribute("message", "查询成功");
            model.addAttribute("code", SysPayResultConstants.SUCCESS_QUERY_ORDER+"");
            model.addAttribute("assPayOrderNo", result.getMerchantOrderNo());
            model.addAttribute("sysPayOrderNo", result.getSysPayOrderNo());
            model.addAttribute("orderStatus",1==result.getPayStatus()?"10000":"10001");
            model.addAttribute("tradeAmt",SafeComputeUtils.numberFormate(result.getMerchantPayMoney()));
            model.addAttribute("assCode",merchantCode);

            Map<String, String> params = new HashMap<>();
            params.put("assPayOrderNo", result.getMerchantOrderNo());
            params.put("sysPayOrderNo", result.getSysPayOrderNo());
            params.put("orderStatus", 1==result.getPayStatus()?"10000":"10001");
            params.put("tradeAmt", SafeComputeUtils.numberFormate(result.getMerchantPayMoney()));
            params.put("assCode", merchantCode);

            String signMsg = ASCIISortUtil.buildSign(params, "=", merchantMd5Key);
            logger.info("-------before sing data :"+"&" + signMsg);

            String signStr = MD5Util.MD5Encode("&" + signMsg);
            logger.info("-------sign data :" +signStr);

            model.addAttribute("sign",signStr);

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
            this.logger.info("----------------指定通道:" + merchant.get("thirdChannels"));
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
            for (String channelId : channels
                    ) {
                Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, channelId);
                if(map.get(paymentType) != null && StringUtils.isNotBlank(map.get(paymentType).toString())
                        && map.get("channelType") != null && "1".equals(map.get("channelType").toString())){
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
