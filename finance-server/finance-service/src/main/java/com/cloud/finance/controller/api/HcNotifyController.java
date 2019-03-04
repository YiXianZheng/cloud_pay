package com.cloud.finance.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.enums.PayStatusEnum;
import com.cloud.finance.common.service.MerchantPayService;
import com.cloud.finance.common.vo.pay.mes.MesPayNotifyData;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.ainong.enums.RespTradeStatusEnum;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

/**
 * hc对外服务接口
 */
@RestController
@RequestMapping(value = "/api/hcpay/notify")
public class HcNotifyController extends BaseController {

    @Autowired
    private ShopPayService shopPayService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private MerchantPayService merchantPayService;

    @RequestMapping(value = "")
    public void hcNotify(HttpServletRequest request, HttpServletResponse response) {
        try {
            logger.info("[hc pay Notify]...");
            this.printAcceptValue(request);
            response.setContentType("text/html;charset=UTF-8");
            logger.info("[hc pay Notify params]...");
            Enumeration<String> paraNames = request.getParameterNames();
            JSONObject jsonObject = new JSONObject();
            for(; paraNames.hasMoreElements();){
                String thisName = paraNames.nextElement();
                String thisValue = request.getParameter(thisName);
                logger.info("----------------key: " + thisName + "  ----  val: " + thisValue);
                jsonObject.put(thisName, thisValue);
            }
            String assCode = request.getParameter("assCode");                   // 商户号
            String assPayOrderNo = request.getParameter("assPayOrderNo");       // 商户订单号
            String sysPayOrderNo = request.getParameter("sysPayOrderNo");       // 平台订单号
            String assPayMoney = request.getParameter("assPayMoney");           // 商户支付金额
            String assPayMessage = request.getParameter("assPayMessage");       // 商户支付信息
            String succTime = request.getParameter("succTime");                 // 成功时间
            String respCode = request.getParameter("respCode");                 // 返回状态码
            String respMsg = request.getParameter("respMsg");                   // 返回信息
            String sign = request.getParameter("sign");                         // 签名结果

            if(StringUtils.isBlank(sign)){
                response.getWriter().write("签名不能为空");
                return;
            }

            ShopPay shopOrder = shopPayService.getBySysOrderNo(assPayOrderNo);
            if (shopOrder != null) {
                logger.info("hc pay notify channelId:" + shopOrder.getThirdChannelId());
                Map<String, String> channelInfo = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopOrder.getThirdChannelId());
                String merchantMd5Key = channelInfo.get("payMd5Key");
                MesPayNotifyData res = new MesPayNotifyData(assCode, assPayOrderNo,sysPayOrderNo, assPayMoney, assPayMessage, succTime,respCode, respMsg, merchantMd5Key);

                if(!sign.equals(res.getSign())){
                    response.getWriter().write("验签错误");
                    return;
                }

                this.logger.info("[hc pay Notify ]...回调支付成功");

                shopOrder.setThirdChannelRespMsg(RespTradeStatusEnum.getRespValByCode(respCode));
                shopOrder.setThirdChannelNotifyFlag(1);
                shopOrder.setThirdChannelOrderNo(assPayOrderNo);
                shopOrder.setRemarks(jsonObject.toJSONString());

                if(RespTradeStatusEnum.CODE_SUCCESS.getCode().equals(respCode)){
                    shopOrder.setPayStatus(PayStatusEnum.PAY_STATUS_ALREADY.getStatus());
                    shopOrder.setPayCompleteTime(new Date());
                    shopPayService.updateOrderStatus(shopOrder);

                    //通知商户
                    boolean notifyResult = merchantPayService.notifyAssWithMd5Key(shopOrder);
                    if (notifyResult) {
                        response.getWriter().write("SUCCESS");
                        this.logger.info("[source hc pay Notify]notify success:" + shopOrder.getMerchantNotifyUrl());
                    } else {
                        this.logger.error("[source hc pay  Notify]notify error:" + shopOrder.getSysPayOrderNo());
                    }

                }else if(RespTradeStatusEnum.CODE_PAYING.getCode().equals(respCode)){
                    this.logger.info("[hc pay Notify ]...付款中");
                }else{
                    this.logger.info("[hc pay Notify ]...付款失败");
                }

            } else {
                this.logger.info("[hc pay Notify ]...回调找不到订单");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
