package com.cloud.finance.controller.api;

import CCBSign.RSASig;
import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.enums.PayStatusEnum;
import com.cloud.finance.common.service.MerchantPayService;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.finance.third.ainong.enums.RespCodeAliPayEnum;
import com.cloud.finance.third.ainong.enums.RespTradeStatusEnum;
import com.cloud.finance.third.ainong.utils.MD5Util;
import com.cloud.finance.third.ainong.vo.AilongDfRespData;
import com.cloud.finance.third.ainong.vo.H5NotifyData;
import com.cloud.finance.third.guanjun.utils.GJSignUtil;
import com.cloud.finance.third.hangzhou.utils.XmlUtil;
import com.cloud.finance.third.hankou.utils.HKUtil;
import com.cloud.finance.third.moshang.utils.MSSignUtil;
import com.cloud.finance.third.shtd1.util.MD5Utils;
import com.cloud.finance.third.xinfulai.util.XFLUtils;
import com.cloud.finance.third.xunjiefu.utils.XJFSignUtil;
import com.cloud.finance.third.yirongtong.utils.YRTSignUtil;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * ebank对外服务接口
 */
@RestController
@RequestMapping(value = "/api/pay/notify")
public class ThirdChannelNotifyController extends BaseController {

    @Autowired
    private ShopPayService shopPayService;
    @Autowired
    private MerchantPayService merchantPayService;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private RedisClient redisClient;

    @RequestMapping(value = "/ansytNotify")
    public void ansytNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[ainong AliH5 Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            String encryptData = request.getParameter("encryptData");
            String time = new SimpleDateFormat("yyyyMMdd HH:mm:ss").format(new Date());
            this.logger.info("[ainong AliH5 Notify ]..." + time + "tranData:" + encryptData);

            H5NotifyData qrNotifyDTO = JSONObject.parseObject(encryptData, H5NotifyData.class);
            String temp = qrNotifyDTO.getHead().getTraceId();
            if(StringUtils.isBlank(temp)){
                logger.info("[ainong AliH5 Notify ]... data exception");
                return;
            }
            String sysOrderNo = temp.substring(0,temp.length() - 1);
            ShopPay shopOrder = shopPayService.getBySysOrderNo(sysOrderNo);
            if (shopOrder == null) {
                this.logger.info("[ainong AliH5 Notify ]...回调找不到订单");
            }

            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto channelDto = ThirdChannelDto.map2Object(map);
            String respJsonSign = MD5Util.digest(qrNotifyDTO.sign(channelDto.getPayMd5Key()), "UTF-8");
            String respSign = request.getParameter("signData");
            //同步返回报文签名
            if (respSign.equals(respJsonSign)) {
                this.logger.info("[ainong AliH5 Notify resp code:" + qrNotifyDTO.getHead().getRespCode() + "===orderNo:" + qrNotifyDTO.getHead().getTraceId() + "]");

                if (RespCodeAliPayEnum.CODE_SUCCESS.getCode().equals(qrNotifyDTO.getHead().getRespCode())) {
                    // 支付成功

                    this.logger.info("[ainong AliH5 Notify ]...回调支付成功");
                    shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                    shopOrder.setPayCompleteTime(new Date());
                    shopOrder.setThirdChannelRespMsg(encryptData);
                    shopOrder.setThirdChannelNotifyFlag(1);
                    shopOrder.setThirdChannelOrderNo(qrNotifyDTO.getHead().getPlatformId());
                    shopOrder.setRemarks(qrNotifyDTO.getHead().getRespCode());
                    shopPayService.updateOrderStatus(shopOrder);
                    //通知商户
                    boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                    if (notifyResult) {
                        response.getWriter().write("000000");
                        this.logger.info("[source ainong AliH5 Notify]notify success:" + qrNotifyDTO.getHead().getTraceId());
                    } else {
                        this.logger.error("[source ainong AliH5 Notify]notify error:" + qrNotifyDTO.getHead().getTraceId());
                    }

                } else {
                    // 支付失败
                    shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                    shopOrder.setThirdChannelRespMsg(encryptData);
                    shopOrder.setThirdChannelNotifyFlag(1);
                    shopOrder.setThirdChannelOrderNo(qrNotifyDTO.getHead().getPlatformId());
                    shopPayService.updateOrderStatus(shopOrder);
                }
            }else{
                this.logger.info("[ainong AliH5 Notify ]...回调签名错误");
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @RequestMapping(value = "/analiCashNotify")
    public void analiCashNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[ainong cash Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");


            AilongDfRespData respData = JSONObject.parseObject(request.getParameter("encryptData"), AilongDfRespData.class);
            //同步返回签名
            String respSign = request.getParameter("signData");
            //同步返回报文签名
            String temp = respData.getHead().getTraceId();
            if(temp == null)
                return;
            String rechargeOrder = temp.substring(0,temp.length() - 1);
            ShopRecharge shopRecharge = shopRechargeService.getByRechargeNo(rechargeOrder);
            if(shopRecharge == null)
                return;
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopRecharge.getThirdChannelId());
            ThirdChannelDto channelDto = ThirdChannelDto.map2Object(map);
            String respJsonSign = MD5Util.digest(respData.doSign(channelDto.getPayMd5Key()), "UTF-8");
            if (!respSign.equals(respJsonSign)) {
                logger.error("【通道代付请求失败】回调签名错误");
                return;
            }

            if("000000".equals(respData.getHead().getRespCode())){
                logger.info("【通道代付成功】----");

                if(shopRecharge.getCompleteTime() == null){
                    shopRecharge.setRechargeStatus(1);
                    shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                    shopRecharge.setThirdChannelOrderNo(respData.getHead().getPlatformId());
                    shopRecharge.setThirdChannelRespMsg(request.getParameter("encryptData"));
                    shopRecharge.setCompleteTime(new Date());
                    shopRechargeService.rechargeSuccess(shopRecharge);

                    logger.info("【通道代付成功】---- " + respData.getHead().getRespMsg());
                }
                response.getWriter().write("000000");
            }else if("000001".equals(respData.getHead().getRespCode())){
                shopRecharge.setRechargeStatus(2);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(request.getParameter("encryptData"));
                shopRechargeService.rechargeFail(shopRecharge);

                logger.info("【通道】----" + respData.getHead().getRespMsg());
            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(request.getParameter("encryptData"));
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【通道代付失败】----> 代付结果状态错误：" + respData.getHead().getRespCode() + "--->"
                        + respData.getHead().getRespMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
        }
    }

    @RequestMapping(value = "/angateNotify")
    public void angateNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[ainong gate Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            this.logger.info("[ainong gate Notify params]...");
            Enumeration<String> paraNames = request.getParameterNames();
            JSONObject jsonObject = new JSONObject();
            for(Enumeration<String> e=paraNames;e.hasMoreElements();){
                String thisName=e.nextElement().toString();
                String thisValue=request.getParameter(thisName);
                this.logger.info("----------------key:"+thisName+"  ----  val:"+thisValue);
                jsonObject.put(thisName, thisValue);
            }
            String payTime = request.getParameter("pay_time");
            String mchId = request.getParameter("mch_id");
            String billno = request.getParameter("billno");
            String outTradeNo = request.getParameter("out_trade_no");
            String tradeStatus = request.getParameter("trade_status");
            String totalFee = request.getParameter("total_fee");
            String signType = request.getParameter("sign_type");
            String appid = request.getParameter("appid");
            String sign = request.getParameter("sign");

            this.logger.info("[ainong gate Notify ]..." + payTime );
            this.logger.info("[ainong gate Notify status:" + tradeStatus + "===orderNo:" + outTradeNo + "]");

            ShopPay shopOrder = shopPayService.getBySysOrderNo(outTradeNo);
            //回调成功
            if (shopOrder != null) {
                this.logger.info("[ainong gate Notify ]...回调支付成功");

                shopOrder.setThirdChannelRespMsg(RespTradeStatusEnum.getRespValByCode(tradeStatus));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(outTradeNo);
                shopOrder.setRemarks(jsonObject.toJSONString());

                if(RespTradeStatusEnum.CODE_SUCCESS.getCode().equals(tradeStatus)){
                    shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                    shopOrder.setPayCompleteTime(new Date());
                    shopPayService.updateOrderStatus(shopOrder);

                    //通知商户
                    response.getWriter().write("SUCCESS");
                    this.logger.info("[source ainong syt Notify]notify success:" + outTradeNo);
                }else if(RespTradeStatusEnum.CODE_PAYING.getCode().equals(tradeStatus)){
                    this.logger.info("[ainong gate Notify ]...付款中");
                }else{
                    this.logger.info("[ainong gate Notify ]...付款失败");
                }

            } else {
                this.logger.info("[ainong syt Notify ]...回调找不到订单");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上海通道1 支付请求回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/shtd1/aliNotify")
    public void shtd1aliNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[shanghai channel 1 AliH5 Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("sign");

            Map<String, String> params = new HashMap<>();
            params.put("orderId", request.getParameter("orderId"));
            params.put("amount", request.getParameter("amount"));
            params.put("pay_number", request.getParameter("pay_number"));
            params.put("respCode", request.getParameter("respCode"));
            params.put("respInfo", request.getParameter("respInfo"));
            params.put("gallery_number", request.getParameter("gallery_number"));
            params.put("status", request.getParameter("status"));


            ShopPay shopOrder = shopPayService.getBySysOrderNo(request.getParameter("pay_number"));
            if (shopOrder == null) {
                this.logger.info("[shanghai channel 1 AliH5 Notify ]...回调找不到订单");
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto channelDto = ThirdChannelDto.map2Object(map);

            //签名
            String signCheck = MD5Utils.getSignParam(params) + "&key=" + channelDto.getPayMd5Key();
            logger.info("[shanghai channel 1 before sign msg]:"+signCheck);
            signCheck = MD5Utils.getKeyedDigest(signCheck, "");
            logger.info("[shanghai channel 1 sign msg]:"+signCheck);

            if(!signCheck.equals(sign)){
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[shanghai channel 1 AliH5 Notify resp code:" + params.get("respCode") + "===orderNo:" + params.get("pay_number") + "]");

            if ("0000".equals(params.get("respCode"))) {
                // 支付成功
                if("TRADE_SUCCESS".equals(params.get("status"))) {
                    this.logger.info("[shanghai channel 1 AliH5 Notify ]...回调支付成功");
                    shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                    shopOrder.setPayCompleteTime(new Date());
                    shopOrder.setThirdChannelRespMsg(MD5Utils.getSignParam(params));
                    shopOrder.setThirdChannelNotifyFlag(1);
                    shopOrder.setThirdChannelOrderNo(params.get("orderId"));
                    shopOrder.setRemarks("respCode: " + params.get("respCode") + ";respInfo: " + params.get("respInfo"));
                    shopPayService.updateOrderStatus(shopOrder);
                    //通知商户
                    boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                    if (notifyResult) {
                        response.getWriter().write("SUCCESS");
                        this.logger.info("[shanghai channel 1 AliH5 Notify]notify success:" + params.get("pay_number"));
                    } else {
                        this.logger.error("[shanghai channel 1 AliH5 Notify]notify error:" + params.get("pay_number"));
                    }
                }else if("WAIT_BUYER_PAY".equals(params.get("status"))) {
                    this.logger.info("[shanghai channel 1 AliH5 Notify ]...回调支付，等待用户支付");
                }else {
                    // 支付失败
                    shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                    shopOrder.setThirdChannelRespMsg(MD5Utils.getSignParam(params));
                    shopOrder.setThirdChannelNotifyFlag(1);
                    shopOrder.setThirdChannelOrderNo(params.get("pay_number"));
                    shopPayService.updateOrderStatus(shopOrder);
                    this.logger.info("[shanghai channel 1 AliH5 Notify ]...回调支付，交易关闭");
                }

            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(MD5Utils.getSignParam(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("pay_number"));
                shopPayService.updateOrderStatus(shopOrder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上海通道1 代付请求异步通知
     * @param request
     * @param response
     */
    @RequestMapping(value = "/shtd1/aliCashNotify")
    public void shtd1aliCashNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[shanghai channel 1 cash Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("sign");

            Map<String, String> params = new HashMap<>();
            params.put("orderId", request.getParameter("orderId"));
            params.put("pay_number", request.getParameter("pay_number"));
            params.put("amount", request.getParameter("amount"));
            params.put("respCode", request.getParameter("respCode"));
            params.put("respInfo", request.getParameter("respInfo"));
            params.put("gallery_number", request.getParameter("gallery_number"));
            params.put("status", request.getParameter("status"));   //00 成功 01 处理中 02交易失败

            ShopRecharge shopRecharge = shopRechargeService.getByRechargeNo(request.getParameter("pay_number"));
            if(shopRecharge == null)
                return;
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopRecharge.getThirdChannelId());
            ThirdChannelDto channelDto = ThirdChannelDto.map2Object(map);

            //签名
            String signCheck = MD5Utils.getSignParam(params) + "&key=" + channelDto.getPayMd5Key();
            logger.info("[shanghai channel 1 before sign msg]:"+signCheck);
            signCheck = MD5Utils.getKeyedDigest(signCheck, "");
            logger.info("[shanghai channel 1 sign msg]:"+signCheck);

            if (!signCheck.equals(sign)) {
                logger.error("【通道代付请求失败】回调签名错误");
                return;
            }

            if("0000".equals(params.get("respCode"))){
                logger.info("【通道代付成功】----");

                if("00".equals(params.get("status"))){
                    shopRecharge.setRechargeStatus(1);
                    shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                    shopRecharge.setThirdChannelOrderNo(params.get("orderId"));
                    shopRecharge.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                    shopRecharge.setCompleteTime(new Date());
                    shopRechargeService.rechargeSuccess(shopRecharge);

                    logger.info("【通道代付成功】---- " + params.get("respInfo"));
                    response.getWriter().write("SUCCESS");
                }else{
                    logger.info("【通道代付失败】----  status: " + params.get("status") + "; msg: "+ params.get("respInfo"));
                }
            }else{
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【通道代付失败】----> 代付结果状态错误：" + params.get("respCode") + "--->" + params.get("respInfo"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("【通道代付请求异常】-------");
        }
    }

    /**
     * 金信 支付请求回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/jx/aliNotify")
    public void jinxinaliNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[jinxin AliH5 Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("sign");
            String orderNo = request.getParameter("attach");

            Map<String, String> params = new HashMap<>();
            params.put("memberid", request.getParameter("memberid"));
            params.put("orderid", request.getParameter("orderid"));
            params.put("amount", request.getParameter("amount"));
            params.put("datetime", request.getParameter("datetime"));
            params.put("returncode", request.getParameter("returncode"));
            params.put("transaction_id", request.getParameter("transaction_id"));


            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[jinxin AliH5 Notify ]...回调找不到订单");
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

            //签名
            String signMsg = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
            logger.info("[jinxin before sign msg]:"+signMsg);
            String signCheck = com.cloud.finance.third.jinxin.utils.MD5Util.md5(signMsg).toUpperCase();
            logger.info("[jinxin sign msg]:"+signCheck);

            if(!signCheck.equals(sign)){
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[jinxin AliH5 Notify resp code:" + params.get("returncode") + "===orderNo:" + orderNo + "]");

            if ("00".equals(params.get("returncode"))) {
                // 支付成功
                this.logger.info("[jinxin AliH5 Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(MD5Utils.getSignParam(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("transaction_id"));
                shopOrder.setRemarks(JSONObject.toJSONString(params));
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("OK");
                    this.logger.info("[jinxin AliH5 Notify]notify success:" + orderNo);
                } else {
                    this.logger.error("[jinxin AliH5 Notify]notify error:" + orderNo);
                }


            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(MD5Utils.getSignParam(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("transaction_id"));
                shopPayService.updateOrderStatus(shopOrder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 迅捷付 支付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/xjf/notify")
    public void xunjiefuNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[xunjiefu pay Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("signature");
            String orderNo = request.getParameter("orderNo");

            Map<String, String> params = new HashMap<>();
            params.put("transType", request.getParameter("transType"));
            params.put("productId", request.getParameter("productId"));
            params.put("merNo", request.getParameter("merNo"));
            params.put("orderDate", request.getParameter("orderDate"));
            params.put("orderNo", request.getParameter("orderNo"));
            params.put("transAmt", request.getParameter("transAmt"));
            params.put("serialId", request.getParameter("serialId"));
            params.put("respCode", request.getParameter("respCode"));
            params.put("respDesc", request.getParameter("respDesc"));


            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[xunjiefu pay Notify ]...回调找不到订单");
                return;
            }

            //签名
            String signMsg = ASCIISortUtil.buildSign(params, "=", "");
            logger.info("[xunjiefu before sign msg]:" + signMsg);
            boolean signCheck = XJFSignUtil.checkSign(signMsg, sign);
            logger.info("[xunjiefu sign result]:" + signCheck);

            if(!signCheck){
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[xunjiefu pay Notify resp code:" + params.get("respCode") + "===orderNo:" + orderNo + "]");

            if ("0000".equals(params.get("respCode"))) {
                // 支付成功
                this.logger.info("[xunjiefu pay Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(MD5Utils.getSignParam(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("serialId"));
                shopOrder.setRemarks(JSONObject.toJSONString(params));
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("success");
                    this.logger.info("[Notify merchant]notify success:" + orderNo);
                } else {
                    this.logger.error("[Notify merchant]notify error:" + orderNo);
                }
            } else if ("P000".equals(params.get("respCode")) || "P999".equals(params.get("respCode"))){
                // 交易处理中或结果未知  不做操作
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(MD5Utils.getSignParam(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("serialId"));
                shopPayService.updateOrderStatus(shopOrder);
                this.logger.info("[xunjiefu pay Notify ]...回调支付成功 - 支付失败");
                response.getWriter().write("success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 迅捷付 代付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/xjf/cashNotify")
    public void xunjiefuCashNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[xunjiefu cash Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("signature");
            String orderNo = request.getParameter("orderNo");

            Map<String, String> params = new HashMap<>();
            params.put("transType", request.getParameter("transType"));
            params.put("productId", request.getParameter("productId"));
            params.put("merNo", request.getParameter("merNo"));
            params.put("orderDate", request.getParameter("orderDate"));
            params.put("orderNo", request.getParameter("orderNo"));
            params.put("transAmt", request.getParameter("transAmt"));
            params.put("serialId", request.getParameter("serialId"));
            params.put("respCode", request.getParameter("respCode"));
            params.put("respDesc", request.getParameter("respDesc"));

            ShopRecharge shopRecharge = shopRechargeService.getByRechargeNo(orderNo);
            if (shopRecharge == null) {
                logger.error("【通道回调请求失败】回调找不到订单");
                return;
            }

            //签名
            String signMsg = ASCIISortUtil.buildSign(params, "=", "");
            logger.info("[xunjiefu before sign msg]:" + signMsg);
            boolean signCheck = XJFSignUtil.checkSign(signMsg, sign);
            logger.info("[xunjiefu sign result]:" + signCheck);

            if (!signCheck) {
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[xunjiefu cash Notify resp code:" + params.get("respCode") + "===orderNo:" + orderNo + "]");

            if ("0000".equals(params.get("respCode"))) {
                // 代付成功
                this.logger.info("[xunjiefu cash Notify ]...回调支付成功");
                shopRecharge.setRechargeStatus(1);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopRecharge.setCompleteTime(new Date());
                shopRechargeService.rechargeSuccess(shopRecharge);

                logger.info("【通道代付成功】---- " + params.get("respDesc"));
                response.getWriter().write("SUCCESS");

            } else if ("P000".equals(params.get("respCode")) || "P999".equals(params.get("respCode"))) {
                // 交易处理中或结果未知  不做操作
            } else {
                // 支付失败
                shopRecharge.setRechargeStatus(4);
                shopRecharge.setThirdChannelNotifyFlag(ShopRecharge.NOTIFY_FLAG_YES);
                shopRecharge.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopRechargeService.rechargeFail(shopRecharge);
                logger.info("【通道代付失败】----> 代付结果状态错误：" + params.get("respCode") + "--->" + params.get("respDesc"));
                response.getWriter().write("SUCCESS");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 陌上支付 支付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/ms/notify")
    public void msNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[moshang pay Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("sign");
            String orderNo = request.getParameter("orderid");

            Map<String, String> params = new HashMap<>();
            params.put("merchantid", request.getParameter("merchantid"));
            params.put("orderid", request.getParameter("orderid"));
            params.put("opstate", request.getParameter("opstate"));
            params.put("ovalue", request.getParameter("ovalue"));
            params.put("sysorderid", request.getParameter("sysorderid"));

            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[moshang pay Notify ]...回调找不到订单");
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

            //签名
            String signCheck = MSSignUtil.obaopayBankMd5Sign(params.get("merchantid"),params.get("orderid"),params.get("opstate"),
                    params.get("ovalue"),params.get("sysorderid"),thirdChannelDto.getPayMd5Key());
            logger.info("[moshang sign result]:"+signCheck);

            if(StringUtils.isBlank(sign) || !sign.equals(signCheck)){
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[moshang pay Notify resp code:" + params.get("opstate") + "===orderNo:" + orderNo + "]");

            if ("0".equals(params.get("opstate"))) {
                // 支付成功
                this.logger.info("[moshang pay Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("sysorderid"));
                shopOrder.setRemarks(params.get("msg"));
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("SUCCESS");
                    this.logger.info("[Notify merchant]notify success:" + orderNo);
                } else {
                    this.logger.error("[Notify merchant]notify error:" + orderNo);
                }
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("sysorderid"));
                shopOrder.setRemarks(params.get("msg"));
                shopPayService.updateOrderStatus(shopOrder);
                this.logger.info("[moshang pay Notify ]...回调支付成功 - 支付失败");
                response.getWriter().write("SUCCESS");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 易融通支付 支付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/yrt/notify")
    public void yrtNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[yirongtong pay Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("sign");
            String orderNo = request.getParameter("ordernumber");

            Map<String, String> params = new HashMap<>();
            params.put("merchantid", request.getParameter("partner"));
            params.put("orderid", request.getParameter("ordernumber"));
            params.put("opstate", request.getParameter("orderstatus"));
            params.put("ovalue", request.getParameter("paymoney"));
            params.put("sysorderid", request.getParameter("sysnumber"));

            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[yirongtong pay Notify ]...回调找不到订单");
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

            //签名
            String signCheck = YRTSignUtil.callbackMd5Sign(params.get("merchantid"), params.get("orderid"), params.get("opstate"),
                                    params.get("ovalue"), thirdChannelDto.getPayMd5Key());

            logger.info("[yirongtong sign result]:" + signCheck);

            if(StringUtils.isBlank(sign) || !sign.equals(signCheck)){
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[yirongtong pay Notify resp code:" + params.get("opstate") + "===orderNo:" + orderNo + "]");

            if ("1".equals(params.get("opstate"))) {
                // 支付成功
                this.logger.info("[yirongtong pay Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelOrderNo(params.get("sysorderid"));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setRemarks(params.get("msg"));
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("ok");
                    this.logger.info("[Notify merchant]notify success:" + orderNo);
                } else {
                    this.logger.error("[Notify merchant]notify error:" + orderNo);
                }
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelOrderNo(params.get("sysorderid"));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setRemarks(params.get("msg"));
                shopPayService.updateOrderStatus(shopOrder);
                this.logger.info("[yirongtong pay Notify ]...回调支付成功 - 支付失败");
                response.getWriter().write("ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 酷宝享支付 支付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/kbx/notify")
    public void kbxNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[kubaoxiang pay Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("sign");
            String orderNo = request.getParameter("ordernumber");

            Map<String, String> params = new HashMap<>();
            params.put("merchantid", request.getParameter("partner"));
            params.put("orderid", request.getParameter("ordernumber"));
            params.put("opstate", request.getParameter("orderstatus"));
            params.put("ovalue", request.getParameter("paymoney"));
            params.put("sysorderid", request.getParameter("sysnumber"));

            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[yirongtong pay Notify ]...回调找不到订单");
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

            //签名
            String signCheck = YRTSignUtil.callbackMd5Sign(params.get("merchantid"), params.get("orderid"), params.get("opstate"),
                    params.get("ovalue"), thirdChannelDto.getPayMd5Key());

            logger.info("[yirongtong sign result]:" + signCheck);

            if(StringUtils.isBlank(sign) || !sign.equals(signCheck)){
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[yirongtong pay Notify resp code:" + params.get("opstate") + "===orderNo:" + orderNo + "]");

            if ("1".equals(params.get("opstate"))) {
                // 支付成功
                this.logger.info("[yirongtong pay Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("sysorderid"));
                shopOrder.setRemarks(params.get("msg"));
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("ok");
                    this.logger.info("[Notify merchant]notify success:" + orderNo);
                } else {
                    this.logger.error("[Notify merchant]notify error:" + orderNo);
                }
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("sysorderid"));
                shopOrder.setRemarks(params.get("msg"));
                shopPayService.updateOrderStatus(shopOrder);
                this.logger.info("[yirongtong pay Notify ]...回调支付成功 - 支付失败");
                response.getWriter().write("ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 安银付支付 支付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/ayf/notify")
    public void ayfNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[anyinfu pay Notify]..." + request.getInputStream());

            InputStream inputStream = request.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            StringBuffer sb = new StringBuffer();
            String s;

            while ((s = in.readLine()) != null) {
                sb.append(s);
            }

            in.close();
            inputStream.close();

            Map<String, String> respMap = XmlUtil.xmlToMap(String.valueOf(sb));
            logger.info("解析xml转成map： " + respMap);

            String sign = respMap.get("sign");
            String orderNo = respMap.get("out_trade_no");
            String tradeType = respMap.get("trade_type");

            logger.info("支付方式：" + tradeType);

            Map<String, String> params = new HashMap<>();

            if (respMap.get("status").equals("0") && respMap.get("result_code").equals("0")) {
                params.put("version", respMap.get("version"));
                params.put("charset", respMap.get("charset"));
                params.put("sign_type", respMap.get("sign_type"));
                params.put("status", respMap.get("status"));
                params.put("result_code", respMap.get("result_code"));
                params.put("nonce_str", respMap.get("nonce_str"));
                params.put("mch_id", respMap.get("mch_id"));
                params.put("out_trade_no", respMap.get("out_trade_no"));
                params.put("trade_type", tradeType);
                params.put("pay_result", respMap.get("pay_result"));
                params.put("transaction_id", respMap.get("transaction_id"));
                params.put("total_fee", respMap.get("total_fee"));
                params.put("fee_type", respMap.get("fee_type"));
                params.put("time_end", respMap.get("time_end"));
            }

            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[anyinfu pay Notify ]...回调找不到订单");
                return ;
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

            //签名
            String signCheck = HKUtil.generateMd5Sign(params, thirdChannelDto.getPayMd5Key());
            logger.info("[anyinfu sign result]:" + signCheck);

            if(StringUtils.isBlank(sign) || !sign.equals(signCheck)){
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[anyinfu pay Notify resp code:" + params.get("result_code") + "===orderNo:" + orderNo + "]");

            logger.info("平台订单号" + respMap.get("transaction_id"));
            if ("0".equals(params.get("pay_result")) && "0".equals(params.get("status"))) {
                // 支付成功
                this.logger.info("[anyinfu pay Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(respMap.get("out_trade_no"));
                shopOrder.setRemarks(respMap.get("message"));
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("SUCCESS");
                    this.logger.info("[Notify merchant]notify success:" + orderNo);
                } else {
                    this.logger.error("[Notify merchant]notify error:" + orderNo);
                }
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(respMap.get("out_trade_no"));
                shopOrder.setRemarks(respMap.get("message"));
                shopPayService.updateOrderStatus(shopOrder);
                this.logger.info("[anyinfu pay Notify ]...回调支付成功 - 支付失败");
                response.getWriter().write("SUCCESS");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("[订单回调通知异常]------");
        }
    }

    /**
     * 物流支付 支付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/wl/notify")
    public void wlNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[wuliu pay Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("SIGN");

            /** 支付请求参数 **/
            String POSID = request.getParameter("POSID");              //商户柜台代码
            String BRANCHID = request.getParameter("BRANCHID");          //分行代码
            String ORDERID = request.getParameter("ORDERID");   //MERCHANTID + "_" + tmpNo.subSequence(tmpNo.length()-14, tmpNo.length());//定单号 (最长30位,商户代码(15位)+自定义字符串(不超过15位))
            String PAYMENT= request.getParameter("PAYMENT");   //付款金额
            String CURCODE = request.getParameter("CURCODE");                                    //币种 01－人民币
            String ACC_TYPE =  request.getParameter("ACC_TYPE");                               //交易码  由建行统一分配为530550
            String SUCCESS = request.getParameter("SUCCESS");
            String TYPE = request.getParameter("TYPE");
            String REFERER = request.getParameter("REFERER");
            String CLIENTIP = request.getParameter("CLIENTIP");
            String REMARK1 = request.getParameter("REMARK1");                                    //备注一
            String REMARK2 = request.getParameter("REMARK2");                                    //备注二
            String ACCDATE = request.getParameter("ACCDATE");   //系统记账日期，为空不验签
            String USRMSG = request.getParameter("USRMSG");
            String ERRMSG = request.getParameter("ERRMSG");
            String USRINFO = request.getParameter("USRINFO");
            String DISCOUNT = request.getParameter("DISCOUNT");


            StringBuffer tmp = new StringBuffer(); //验签字段

            tmp.append("POSID=");
            tmp.append(POSID);
            tmp.append("&BRANCHID=");
            tmp.append(BRANCHID);
            tmp.append("&ORDERID=");
            tmp.append(ORDERID);
            tmp.append("&PAYMENT=");
            tmp.append(PAYMENT);
            tmp.append("&CURCODE=");
            tmp.append(CURCODE);
            tmp.append("&REMARK1=");
            tmp.append(REMARK1);
            tmp.append("&REMARK2=");
            tmp.append(REMARK2);
            tmp.append("&ACC_TYPE=");
            tmp.append(ACC_TYPE);
            tmp.append("&SUCCESS=");
            tmp.append(SUCCESS);
            if(StringUtils.isNotBlank(TYPE)) {
                tmp.append("&TYPE=");
                tmp.append(TYPE);
            }
            if(StringUtils.isNotBlank(REFERER)) {
                tmp.append("&REFERER=");
                tmp.append(REFERER);
            }
            if(StringUtils.isNotBlank(CLIENTIP)) {
                tmp.append("&CLIENTIP=");
                tmp.append(CLIENTIP);
            }
            if(StringUtils.isNotBlank(ACCDATE)) {
                tmp.append("&ACCDATE=");
                tmp.append(ACCDATE);
            }
            if(StringUtils.isNotBlank(USRMSG)) {
                tmp.append("&USRMSG=");
                tmp.append(USRMSG);
            }
            if(StringUtils.isNotBlank(ERRMSG)) {
                tmp.append("&ERRMSG=");
                tmp.append(ERRMSG);
            }
            if(StringUtils.isNotBlank(USRINFO)) {
                tmp.append("&USRINFO=");
                tmp.append(USRINFO);
            }
            if(StringUtils.isNotBlank(DISCOUNT)) {
                tmp.append("&DISCOUNT=");
                tmp.append(DISCOUNT);
            }

            if(StringUtils.isBlank(ORDERID)){
                logger.error("【通道回调请求失败】回调ORDERID 为空");
                return;
            }
            String orderNo = "E18" + ORDERID.split("_")[1];
            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[wuliu pay Notify ]...回调找不到订单");
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);


            String key = thirdChannelDto.getPayMd5Key();
            String PUB32TR2= key.substring(key.length()-30,key.length());//字段为对应柜台的公钥后30位

            tmp.append("&PUB=");
            tmp.append(PUB32TR2);

            //签名
            String signCheck = tmp.toString();
            logger.info("[wuliu before sign msg]:" + signCheck);

            RSASig rsaSig = new RSASig();
            if(rsaSig.verifySigature(sign, signCheck)){
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[wuliu pay Notify resp code:" + SUCCESS + "===orderNo:" + orderNo + "]");

            if ("Y".equalsIgnoreCase(SUCCESS)) {
                // 支付成功
                this.logger.info("[wuliu pay Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(tmp.toString());
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(ORDERID);
                shopOrder.setRemarks("支付成功");
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("SUCCESS");
                    this.logger.info("[Notify merchant]notify success:" + orderNo);
                } else {
                    this.logger.error("[Notify merchant]notify error:" + orderNo);
                }
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(tmp.toString());
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(ORDERID);
                shopOrder.setRemarks("支付失败");
                shopPayService.updateOrderStatus(shopOrder);
                this.logger.info("[wuliu pay Notify ]...回调支付成功 - 支付失败");
                response.getWriter().write("SUCCESS");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 鑫富莱支付 支付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/xfl/notify")
    public void xflNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[xinfulai pay Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("signature");
            String orderNo = request.getParameter("merOrderId");

            Map<String, String> params = new HashMap<>();
            params.put("merchantId", request.getParameter("merchantId"));
            params.put("merOrderId", request.getParameter("merOrderId"));
            params.put("respCode", request.getParameter("respCode"));
            params.put("respMsg", request.getParameter("respMsg"));
            params.put("txnAmt", request.getParameter("txnAmt"));

            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[xinfulai pay Notify ]...回调找不到订单");
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

            //签名
            String signCheck = XFLUtils.signMap(params, thirdChannelDto.getPayMd5Key());

            logger.info("[xinfulai sign result]:" + signCheck);

            if(StringUtils.isBlank(sign) || !sign.equals(signCheck)){
                logger.error("【通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[xinfulai pay Notify resp code:" + params.get("respCode") + "====orderNo:" + orderNo + "]");

            if ("1001".equals(params.get("respCode"))) {
                // 支付成功
                this.logger.info("[xinfulai pay Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("merOrderId"));
                shopOrder.setRemarks(params.get("respMsg"));
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("ok");
                    this.logger.info("[Notify merchant]notify success:" + orderNo);
                } else {
                    this.logger.error("[Notify merchant]notify error:" + orderNo);
                }
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setRemarks(params.get("msg"));
                shopOrder.setThirdChannelOrderNo(params.get("merOrderId"));
                shopPayService.updateOrderStatus(shopOrder);
                this.logger.info("[xinfulai pay Notify ]...回调支付成功 - 支付失败");
                response.getWriter().write("ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 中华 支付请求回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/zhNotify")
    public void zhonghuaNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[zhonghua AliH5 Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String orderId = request.getParameter("resqn");
            String tempPayMoney = request.getParameter("pay_amount");
            String thirdChannelOrderId = request.getParameter("trade_no");
            String status = request.getParameter("status");
            String account = request.getParameter("account");
            String remark = request.getParameter("remark");
            String mer_sign = request.getParameter("mer_sign");

            StringBuilder respInfo = new StringBuilder();
            respInfo.append("{status:").append(status).append(",");
            respInfo.append("account:").append(account).append(",");
            respInfo.append("resqn:").append(orderId).append(",");
            respInfo.append("trade_no:").append(thirdChannelOrderId).append(",");
            respInfo.append("pay_amount:").append(tempPayMoney).append(",");
            respInfo.append("remark:").append(remark).append(",");
            respInfo.append("mer_sign:").append(mer_sign).append(",");

            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderId);
            if (shopOrder == null) {
                this.logger.info("[zhonghua AliH5 Notify ]...回调找不到订单");
                return;
            }

            Double payMoney = SafeComputeUtils.div(Double.parseDouble(tempPayMoney), 100D);
            if(payMoney.doubleValue() != shopOrder.getMerchantPayMoney()){
                logger.error("【通道回调请求异常】回调支付金额不一致");
                return;
            }

            this.logger.info("[zhonghua AliH5 Notify resp code:" + status + "===orderNo:" + orderId + "]");

            if ("200".equals(status)) {
                // 支付成功
                this.logger.info("[zhonghua AliH5 Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(respInfo.toString());
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(thirdChannelOrderId);
                shopOrder.setRemarks("respCode: " + status);
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("success");
                    this.logger.info("[zhonghua AliH5 Notify]notify success:" + orderId);
                } else {
                    this.logger.error("[zhonghua AliH5 Notify]notify error:" + orderId);
                }
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(respInfo.toString());
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(thirdChannelOrderId);
                shopPayService.updateOrderStatus(shopOrder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 冠军支付 支付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/gj/aliNotify")
    public void gjNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[guanjun pay Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String sign = request.getParameter("signature");
            String orderNo = request.getParameter("merOrderId");

            Map<String, String> params = new HashMap<>();
            params.put("merId", request.getParameter("merId"));
            params.put("merOrderId", request.getParameter("merOrderId"));
            params.put("orderId", request.getParameter("orderId"));
            params.put("currency", request.getParameter("currency"));
            params.put("success", request.getParameter("success") + "");
            params.put("txnAmt", request.getParameter("txnAmt"));
            params.put("attach", request.getParameter("attach"));

            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[guanjun pay Notify ]...回调找不到订单");
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

            //签名
            String signCheck = GJSignUtil.signData(params, thirdChannelDto.getPayMd5Key());

            logger.info("[guanjun sign result]:" + signCheck);

            if(StringUtils.isBlank(sign) || !sign.equals(signCheck)){
                logger.error("【冠军支付通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[xinfulai pay Notify resp code:" + params.get("respCode") + "====orderNo:" + orderNo + "]");

            if ("true".equals(params.get("success"))) {
                // 支付成功
                this.logger.info("[xinfulai pay Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("merOrderId"));
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("success");
                    this.logger.info("[Notify merchant]notify success:" + orderNo);
                } else {
                    this.logger.error("[Notify merchant]notify error:" + orderNo);
                }
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(params));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(params.get("merOrderId"));
                shopOrder.setRemarks(params.get("msg"));
                shopPayService.updateOrderStatus(shopOrder);
                this.logger.info("[xinfulai pay Notify ]...回调支付成功 - 支付失败");
                response.getWriter().write("ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 云极支付 支付回调
     * @param request
     * @param response
     */
    @RequestMapping(value = "/yjf/notify")
    public void yjfNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            this.logger.info("[yunji pay Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");

            String result = request.getParameter("result");
            logger.info("[yunji pay notify result] : " + result);
            Map<String, String> respMap = JSONObject.parseObject(result, HashMap.class);
            logger.info("[yunji pay notify respMap]: " + respMap);

            String orderNo = respMap.get("orderID");
            String status = respMap.get("status");
            String money = respMap.get("money");

            String sign = respMap.get("sign");

            ShopPay shopOrder = shopPayService.getBySysOrderNo(orderNo);
            if (shopOrder == null) {
                this.logger.info("[guanjun pay Notify ]...回调找不到订单");
                return;
            }
            Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
            ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

            String key = thirdChannelDto.getPayMd5Key();
            String signBefore = key + orderNo + money + status;
            //签名
            String signCheck = com.cloud.sysconf.common.utils.MD5Util.md5(signBefore);

            logger.info("[yunji sign result]:" + signCheck);

            if(StringUtils.isBlank(sign) || !sign.equals(signCheck)){
                logger.error("【云极付支付通道回调请求失败】回调签名错误");
                return;
            }

            this.logger.info("[yunji pay Notify ====orderNo:" + orderNo + "]");

            if ("TRADE_FINISHED".equals(respMap.get("status"))) {
                // 支付成功
                this.logger.info("[xinfulai pay Notify ]...回调支付成功");
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                shopOrder.setPayCompleteTime(new Date());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(respMap));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(orderNo);
                shopPayService.updateOrderStatus(shopOrder);
                //通知商户
                boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                if (notifyResult) {
                    response.getWriter().write("success");
                    this.logger.info("[Notify merchant]notify success:" + orderNo);
                } else {
                    this.logger.error("[Notify merchant]notify error:" + orderNo);
                }
            } else {
                // 支付失败
                shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_WAIT.getStatus());
                shopOrder.setThirdChannelRespMsg(JSONObject.toJSONString(respMap));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(respMap.get("merOrderId"));
                shopOrder.setRemarks(respMap.get("msg"));
                shopPayService.updateOrderStatus(shopOrder);
                this.logger.info("[yunji pay Notify ]...回调支付 - 支付失败");
                response.getWriter().write("success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
