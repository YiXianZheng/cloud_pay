package com.cloud.finance.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.utils.ASCIISortUtil;
import com.cloud.finance.common.utils.PostUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.finance.third.ainong.utils.Base64Util;
import com.cloud.finance.third.ainong.utils.MD5Util;
import com.cloud.finance.third.ainong.vo.H5ReqData;
import com.cloud.finance.third.ainong.vo.HeadReqData;
import com.cloud.finance.third.hangzhou.utils.SignUtil;
import com.cloud.finance.third.hankou.utils.HKUtil;
import com.cloud.finance.third.kubaoxiang.utils.Md5SignUtil;
import com.cloud.finance.third.shtd1.util.MD5Utils;
import com.cloud.finance.third.wuliu.utils.WuliuMD5;
import com.cloud.finance.third.xunjiefu.utils.XJFSignUtil;
import com.cloud.finance.third.yirongtong.vo.H5RequestData;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * 请求跳转页
 */
@Controller
@RequestMapping(value = "/d8")
public class DirectPayController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(DirectPayController.class);

	@Autowired
	private RedisClient redisClient;
	@Autowired
	private ShopPayService shopPayService;
	@Autowired
    private SysBankProvider sysBankProvider;

	private String getBaseNotifyUrl(){
		return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
	}

    /**
     * 迅捷付
     * @param sysPayOrderNo
     * @param request
     * @param response
     * @param model
     * @throws Exception
     */
    @RequestMapping("/xjf_{sysPayOrderNo}.html")
    public void xunjiefu(@PathVariable("sysPayOrderNo") String sysPayOrderNo, HttpServletRequest request, HttpServletResponse response,
                        Model model) throws Exception {

        logger.info("...[xunjiefu gate direct pay] pay order action...");
        ShopPay od = shopPayService.getBySysOrderNo(sysPayOrderNo);
        Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, od.getThirdChannelId());
        ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

		//请求参数
		String version="1.0.0";     //版本号
		String transType = "SALES"; //业务类型
		String productId = "0003";  //产品类型
		String merNo = thirdChannelDto.getMerchantId();//商户号
        String orderDate = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_11);//订单日期
        String orderNo = od.getSysPayOrderNo();
		String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();//通知地址
		String returnUrl = od.getMerchantReturnUrl();//回调地址
		String transAmt = SafeComputeUtils.numberFormate2(SafeComputeUtils.multiply(od.getMerchantPayMoney(), 100D));
		String salesType = "0";     //随机类型
        //非必填或某些场景下必填
		String bankCode = "";
        if(od.getBankCode() != null && !od.getBankCode().equals(od.getChannelTypeCode())) {
            logger.info("【支付请求】 bank code :" + od.getBankCode());
            ApiResponse apiResponse = sysBankProvider.toChannelCode(od.getBankCode(), thirdChannelDto.getId());
            logger.info("【支付请求】 bank response :" + apiResponse);
            if ((ResponseCode.Base.SUCCESS.getCode() + "").equals(apiResponse.getCode())) {
                bankCode = apiResponse.getData().toString();
            } else {
                logger.error("【支付请求失败】不支持的银行");
                return;
            }
        }
		String commodityName=od.getMerchantGoodsTitle();    //产品名称
		String commodityDetail=od.getMerchantGoodsDesc();   //产品详情

		Map<String, String> params = new HashMap<>();
		if(StringUtils.isNotBlank(version)) {
			params.put("version", version);
		}
		if(StringUtils.isNotBlank(transType)) {
			params.put("transType", transType);
		}
		if(StringUtils.isNotBlank(productId)) {
			params.put("productId", productId);
		}
		if(StringUtils.isNotBlank(merNo)) {
			params.put("merNo", merNo);
		}
		if(StringUtils.isNotBlank(orderDate)) {
			params.put("orderDate", orderDate);
		}
		if(StringUtils.isNotBlank(orderNo)) {
			params.put("orderNo", orderNo);
		}
		if(StringUtils.isNotBlank(notifyUrl)) {
			params.put("notifyUrl", notifyUrl);
		}
        if(StringUtils.isNotBlank(returnUrl)) {
            params.put("returnUrl", returnUrl);
        }
        if(StringUtils.isNotBlank(transAmt)) {
            params.put("transAmt", transAmt);
        }
        if(StringUtils.isNotBlank(salesType)) {
            params.put("salesType", salesType);
        }
        if(StringUtils.isNotBlank(bankCode)) {
            params.put("bankCode", bankCode);
        }
        if(StringUtils.isNotBlank(commodityName)) {
            params.put("commodityName", commodityName);
        }
        if(StringUtils.isNotBlank(commodityDetail)) {
            params.put("commodityDetail", commodityDetail);
        }

		//签名
		String stringSignTemp = ASCIISortUtil.buildSign(params, "=", "");
		logger.info("[xunjiefu before sign msg]:"+ stringSignTemp );
		if(StringUtils.isBlank(stringSignTemp)){
            logger.error("【支付请求失败】签名为空");
            return;
        }
		String signStr = XJFSignUtil.doSign(stringSignTemp);
		logger.info("[xunjiefu sign msg]:"+signStr);

		params.put("signature", signStr);

        String html = createAutoFormHtml(thirdChannelDto.getPayUrl(), params, "UTF-8", "POST");
        logger.info("...[xunjiefu alih5 pay] html:" + html);
        PrintWriter out = null;
        try {
            response.setCharacterEncoding("utf-8");
            response.setContentType("text/html;charset=utf-8");
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.print(html);
            out.flush();
        } finally {
            out.close();
        }
    }

	/**
	 * 爱农 H5
	 * @param sysPayOrderNo
	 * @param response
	 */
	@RequestMapping("/an_{sysPayOrderNo}.html")
	public void ainong(@PathVariable("sysPayOrderNo") String sysPayOrderNo, HttpServletResponse response) {

		logger.info("...[ainong ali h5  pay] pay order action...");
		ShopPay shopPayDto = shopPayService.getBySysOrderNo(sysPayOrderNo);
		Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopPayDto.getThirdChannelId());
		ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

		Date time = new Date();
		//请求参数
		/**** 请求参数 ****/
		//请求头
		//合作方标识号
		String partnerNo = thirdChannelDto.getMerchantId();
		//版本
		String version = "1.0.0";
		//字符集
		String charset = "UTF-8";
		//合作方类型
		String  partnerType = "OUTER";
		//交易代码
		String txnCode = "102004";
		//交易跟踪号
		String traceId = shopPayDto.getSysPayOrderNo()+"0";
		//请求日期  格式为yyyyMMdd
		String reqDate = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_11);
		//请求时间  格式为yyyyMMddHHmmss
		String reqTime = DateUtil.DateToString(time, DateUtil.DATE_PATTERN_18);

		//交易请求参数
		//交易金额  单位元
		String amount = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());
		//
		String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
		//
		String returnUrl = shopPayDto.getMerchantReturnUrl();
		//
		String body = "pay action";

		H5ReqData qrReqDTO = new H5ReqData();
		HeadReqData headReqDTO = new HeadReqData();
		headReqDTO.setPartnerNo(partnerNo);
		headReqDTO.setVersion(version);
		headReqDTO.setCharset(charset);
		headReqDTO.setPartnerType(partnerType);
		headReqDTO.setTxnCode(txnCode);
		headReqDTO.setTraceId(traceId);
		headReqDTO.setReqDate(reqDate);
		headReqDTO.setReqTime(reqTime);
		qrReqDTO.setAmount(amount);
		qrReqDTO.setBody(body);
		qrReqDTO.setNotifyUrl(notifyUrl);
		qrReqDTO.setHead(headReqDTO);

		//签名
		String signJson = qrReqDTO.text(thirdChannelDto.getPayMd5Key());

		logger.info("[ailong before sign msg]:"+signJson);
		String sign = MD5Util.digest(signJson, "UTF-8");
		logger.info("[ailong sign msg]:"+sign);

		Map<String, String> method = new HashMap<>();
		String jsonObject = JSONObject.toJSONString(qrReqDTO);
		method.put("encryptData", jsonObject);
		method.put("signData", sign);
		method.put("partnerNo", partnerNo);
		logger.info("pay post params == >  encryptData:"+ jsonObject + "; signData:" + sign);

		String html = createAutoFormHtml(thirdChannelDto.getPayUrl(), method, "UTF-8", "GET");
		logger.info("...[ainong alih5 pay] html:" + html);
		PrintWriter out = null;
		try {
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.print(html);
			out.flush();
		} finally {
			out.close();
		}
	}

	/**
	 * 易融通 H5
	 * @param sysPayOrderNo
	 * @param response
	 */
	@RequestMapping("/yrt_{sysPayOrderNo}.html")
	public void yirongtong(@PathVariable("sysPayOrderNo") String sysPayOrderNo, HttpServletResponse response) {

		logger.info("...[yirongtong ali h5  pay] pay order action...");
		ShopPay shopPayDto = shopPayService.getBySysOrderNo(sysPayOrderNo);
		Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopPayDto.getThirdChannelId());
		ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

		//请求参数
		//版本
		String version = "3.0";
		// 接口名称
		String method = "Gt.online.interface";
		//合作方标识号
		String partner = thirdChannelDto.getMerchantId();
		// 银行类型
		String banktype = "ALIPAYWAP";
		//交易金额  单位元
		String paymoney = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());
		// 平台订单号
		String ordernumber = shopPayDto.getSysPayOrderNo();
		// 下行异步通知
		String callbackurl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();

		Map<String, String> params = new HashMap<>();
		params.put("version", version);
		params.put("method", method);
		params.put("partner", partner);
		params.put("banktype", banktype);
		params.put("paymoney", paymoney);
		params.put("ordernumber", ordernumber);
		params.put("callbackurl", callbackurl);

		//签名
		String signJson = H5RequestData.text(params, thirdChannelDto.getPayMd5Key());

		logger.info("[yirongtong before sign msg]:" + signJson);
		String sign = MD5Util.digest(signJson, "UTF-8");
		logger.info("[yirongtong sign msg]:" + sign);

		params.put("sign", sign);

		logger.info("pay post params == >  encryptData:"+ params + "; signData:" + sign);

		String html = createAutoFormHtml(thirdChannelDto.getPayUrl(), params, "UTF-8", "POST");
		logger.info("...[yirongtong alih5 pay] html:" + html);
		PrintWriter out = null;
		try {
			response.setContentType("text/html;charset=utf-8");
			response.setCharacterEncoding("utf-8");
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			out.print(html);
			out.flush();
		} finally {
			out.close();
		}
	}

	/**
	 * 酷宝享 H5
	 * @param sysPayOrderNo
	 * @param response
	 */
	@RequestMapping("/kbx_{sysPayOrderNo}.html")
	public void kubaoxiang(@PathVariable("sysPayOrderNo") String sysPayOrderNo, HttpServletResponse response) {

		logger.info("...[kubaoxiang ali h5  pay] pay order action...");
		ShopPay shopPayDto = shopPayService.getBySysOrderNo(sysPayOrderNo);
		Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopPayDto.getThirdChannelId());
		ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

		// 上游商户账号
		String account = thirdChannelDto.getMerchantId();
		// 系统订单号
		String resqn = shopPayDto.getSysPayOrderNo();
		// 支付金额（单位：元）
		String pay_amount = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());
		// 通知地址
		String notify_url = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
		// 支付方式     1：支付宝   2：微信
		String pay_way = "1";

		Map<String, String> params = new HashMap<>();
		params.put("pay_way", pay_way);
		if (StringUtils.isNotBlank(account)) {
			params.put("account", account);
		}
		if (StringUtils.isNotBlank(resqn)) {
			params.put("resqn", resqn);
		}
		if (StringUtils.isNotBlank(pay_amount)) {
			params.put("pay_amount", pay_amount);
		}
		if (StringUtils.isNotBlank(notify_url)) {
			params.put("notify_url", notify_url);
		}

		logger.info("[kubaoxiang sign before msg]:signMsg: " + params);
		String signStr = "";

		try {
			signStr = Md5SignUtil.generateMd5Sign(params, thirdChannelDto.getPayMd5Key());
			logger.info("[kubaoxiang sign msg]:signMsg: " + signStr);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("---- sign exception ----");
		}
		params.put("sign", signStr);

		logger.info("pay post params == >  encryptData:"+ params + "; signData:" + signStr);

		String html = createAutoFormHtml(thirdChannelDto.getPayUrl(), params, "UTF-8", "POST");
		logger.info("...[kubaoxiang alih5 pay] html:" + html);
		PrintWriter out = null;
		try {
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		try {
			out.print(html);
			out.flush();
		} finally {
			out.close();
		}
	}

	/**
	 * 北京 H5
	 * @param sysPayOrderNo
	 * @param response
	 */
	@RequestMapping("/bj_{sysPayOrderNo}.html")
	public void beijing(@PathVariable("sysPayOrderNo") String sysPayOrderNo, HttpServletResponse response) {

		logger.info("...[beijing ali h5  pay] pay order action...");
		ShopPay shopPayDto = shopPayService.getBySysOrderNo(sysPayOrderNo);
		Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopPayDto.getThirdChannelId());
		ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

		Map<String, String> params = new HashMap<>();

        // 商户ID
        String merchantNo = thirdChannelDto.getMerchantId();
        // 订单号
        String merOrder = shopPayDto.getSysPayOrderNo();
        // 商品名
        String productName = shopPayDto.getMerchantGoodsTitle() + shopPayDto.getSysPayOrderNo();
        //支付金额
        String tradeAmount = String.valueOf(shopPayDto.getMerchantPayMoney());
        // 异步通知地址
        String downCallBackUrl = thirdChannelDto.getNotifyUrl();
        // 交易方式
        String bizType = "ALIPAY";

		params.put("bizType", bizType);
		if(StringUtils.isNotBlank(merchantNo)) {
			params.put("merchantNo", merchantNo);
		}
		if(StringUtils.isNotBlank(merOrder)) {
			params.put("merOrder", merOrder);
		}
		if(StringUtils.isNotBlank(productName)) {
			params.put("productName", productName);
		}
		if(StringUtils.isNotBlank(tradeAmount)) {
			params.put("tradeAmount", tradeAmount);
		}
		if(StringUtils.isNotBlank(downCallBackUrl)) {
			params.put("downCallBackUrl", downCallBackUrl);
		}

		String sign = null;
		try {
			sign = SignUtil.generateSignature(params, thirdChannelDto.getPayMd5Key());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[beijing channel sign exception]");
		}
		logger.info("[beijing sign msg]:signMsg:" + sign);

		String postJsonStr = JSONObject.toJSONString(params);
		Map<String, String> postParams = new HashMap<>();
		postParams.put("jsonData", postJsonStr);
		postParams.put("sign", sign);

		logger.info("pay post params == >  jsonData:" + postParams + "; signData:" + sign);

		String html = createAutoFormHtml(thirdChannelDto.getPayUrl(), postParams, "UTF-8", "POST");
		logger.info("...[beijing alih5 pay] html:" + html);
		PrintWriter out = null;
		try {
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("beijing write html exception");
		}
        if (out != null) {
            try {
                out.print(html);
                out.flush();
            } finally {
                out.close();
            }
        }
	}

	/**
	 * 物流支付
	 * @param sysPayOrderNo
	 * @param response
	 */
	@RequestMapping("/wl_{sysPayOrderNo}.html")
	public void wuliu(@PathVariable("sysPayOrderNo") String sysPayOrderNo, HttpServletResponse response) {

		logger.info("...[wuliu h5 pay] pay order action...");
		ShopPay od = shopPayService.getBySysOrderNo(sysPayOrderNo);
		Map<String, String> channelMap = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, od.getThirdChannelId());
		ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(channelMap);

        /** 支付请求参数 **/
        String MERCHANTID = thirdChannelDto.getMerchantId();    //商户代码
        String POSID = thirdChannelDto.getAppId();              //商户柜台代码
        String BRANCHID = thirdChannelDto.getAppKey();          //分行代码
        String tmpNo = od.getSysPayOrderNo();
        String ORDERID = MERCHANTID + "_" + tmpNo.subSequence(tmpNo.length()-14, tmpNo.length());//定单号 (最长30位,商户代码(15位)+自定义字符串(不超过15位))
        String PAYMENT= SafeComputeUtils.numberFormate(od.getMerchantPayMoney());   //付款金额
        String CURCODE="01";                                    //币种 01－人民币
        String TXCODE = "530550";                               //交易码  由建行统一分配为530550
        String RETURNTYPE="2";                                  //返回类型 0或空：返回页面二维
        //        1：返回JSON格式【二维码信息串】
        //        2：返回聚合扫码页面二维码
        //        3：返回聚合扫码JSON格式【二维码信息串】
        //        聚合扫码只能上送2或3
        String REMARK1 = "";                                    //备注一
        String REMARK2 = "";                                    //备注二
        String TIMEOUT = "";                                    //订单超时时间

        String key = thirdChannelDto.getPayMd5Key();
        String PUB32TR2= key.substring(key.length()-30,key.length());//字段为对应柜台的公钥后30位

        StringBuffer tmp = new StringBuffer(); //验签字段
        tmp.append("MERCHANTID=");
        tmp.append(MERCHANTID);
        tmp.append("&POSID=");
        tmp.append(POSID);
        tmp.append("&BRANCHID=");
        tmp.append(BRANCHID);
        tmp.append("&ORDERID=");
        tmp.append(ORDERID);
        tmp.append("&PAYMENT=");
        tmp.append(PAYMENT);
        tmp.append("&CURCODE=");
        tmp.append(CURCODE);
        tmp.append("&TXCODE=");
        tmp.append(TXCODE);
        tmp.append("&REMARK1=");
        tmp.append(REMARK1);
        tmp.append("&REMARK2=");
        tmp.append(REMARK2);
        tmp.append("&RETURNTYPE=");
        tmp.append(RETURNTYPE);
        tmp.append("&TIMEOUT=");
        tmp.append(TIMEOUT);
        tmp.append("&PUB=");
        tmp.append(PUB32TR2);

        //签名
        String sign = tmp.toString();
        logger.info("[wuliu before sign msg]:" + sign);
        sign = WuliuMD5.md5Str(tmp.toString());
        logger.info("[wuliu sign msg]:"+sign);

        Map map = new HashMap();
        map.put("CCB_IBSVersion","V6");	//必输项
        map.put("MERCHANTID",MERCHANTID);
        map.put("BRANCHID",BRANCHID);
        map.put("POSID",POSID);
        map.put("ORDERID",ORDERID);
        map.put("PAYMENT",PAYMENT);
        map.put("CURCODE",CURCODE);
        map.put("TXCODE",TXCODE);
        map.put("REMARK1",REMARK1);
        map.put("REMARK2",REMARK2);
        map.put("RETURNTYPE",RETURNTYPE);
        map.put("TIMEOUT",TIMEOUT);
        map.put("MAC",WuliuMD5.md5Str(tmp.toString()));

        String baowen = MD5Utils.getSignParam(map);
        logger.info("[wuliu request params]:"+baowen);

		PrintWriter out = null;

        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html;charset=utf-8");
		try {
            out = response.getWriter();

            String html = createAutoFormHtml(thirdChannelDto.getPayUrl(), map, "UTF-8", "POST");
            logger.info("...[xunjiefu alih5 pay] html:" + html);

            out.print(html);
            out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.close();
		}

	}

	/**
	 * 中华 H5
	 * @param sysPayOrderNo
	 * @param response
	 */
	@RequestMapping("/zh_{sysPayOrderNo}.html")
	public void zhonghua(@PathVariable("sysPayOrderNo") String sysPayOrderNo, HttpServletResponse response) {

		logger.info("...[zhonghua pay] pay order action...");
		ShopPay shopPayDto = shopPayService.getBySysOrderNo(sysPayOrderNo);
		Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopPayDto.getThirdChannelId());
		ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

		/*** 参数 ***/
        //商户的账号
        String account = thirdChannelDto.getMerchantId();
        //商户系统订单号
        String resqn = shopPayDto.getSysPayOrderNo();
        //订单金额  以“元”为单位，仅允许两位小数，必须大于零
        String pay_amount = SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());
        //支付结果通知地址
        String notify_url = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
        //支付方式
        String pay_way = "5";


        Map<String, String> params = new HashMap<>();
        params.put("account", account);
        params.put("resqn", resqn);
        params.put("pay_amount", pay_amount);
        params.put("notify_url", notify_url);
        params.put("pay_way", pay_way);
        params.put("key", thirdChannelDto.getPayMd5Key());

        //签名
        String sign = ASCIISortUtil.buildSign(params, "=", "");
        logger.info("[zhonghua  before sign msg]:"+sign);
        sign = com.cloud.sysconf.common.utils.MD5Util.md5(sign);
        logger.info("[zhonghua  sign msg]:"+sign);

        params.put("sign", sign);

		String html = createAutoFormHtml(thirdChannelDto.getPayUrl(), params, "UTF-8", "POST");
		logger.info("...[zhonghua pay] html:" + html);
		PrintWriter out = null;
		try {
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.print(html);
			out.flush();
		} finally {
			out.close();
		}
	}

	/**
	 * 云极 快捷
	 * @param sysPayOrderNo
	 * @param response
	 */
	@RequestMapping("/yj_{sysPayOrderNo}.html")
	public void yunji(@PathVariable("sysPayOrderNo") String sysPayOrderNo, HttpServletResponse response) {

		ShopPay shopPayDto = shopPayService.getBySysOrderNo(sysPayOrderNo);
		String orderPayType = shopPayDto.getChannelTypeCode();
		logger.info("...[yunji " + orderPayType + " pay] pay order action...");
		Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopPayDto.getThirdChannelId());
		ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);
		Map<String, String> params = new HashMap<>();

		String mchNo = thirdChannelDto.getMerchantId();
		String orderID = shopPayDto.getSysPayOrderNo();
		String money = String.valueOf(shopPayDto.getMerchantPayMoney());
		String body = "电子产品";
		String payType = "qpay";
		String notifyUrl = getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl();
		String callbackurl = "http://www.baidu.com";
		String clientip = "127.0.0.1";
		String bank_id = "ccb";

		params.put("bank_id", bank_id);
		params.put("mchNo", mchNo);
		params.put("orderID", orderID);
		params.put("money", money);
		params.put("body", body);
		params.put("payType", payType);
		params.put("notifyUrl", Base64Util.encode(notifyUrl));
		params.put("callbackurl", Base64Util.encode(callbackurl));
		params.put("clientip", clientip);

		String signBefore = ASCIISortUtil.buildSign(params, "=", "&key=" + thirdChannelDto.getPayMd5Key());
		logger.info("【yunji channel sign before msg】: " + signBefore);
		String sign = com.cloud.sysconf.common.utils.MD5Util.md5(signBefore);
		logger.info("【yunji channel sign msg】: " + sign);
		params.put("sign", sign);

		String jsonStr = JSONObject.toJSONString(params);
		String requestBody = "requestBody=" + jsonStr;

		logger.info("[requestBody:] " + requestBody);
		String postResp = PostUtils.sendPost(thirdChannelDto.getPayUrl(), requestBody);
		if (StringUtils.isBlank(postResp)) {
			logger.error("[yunji pay url create fail]");
			return;
		}
		String html = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/></head><body>" +
				postResp +
				"</body>" +
				"</html>";

		PrintWriter out = null;
		try {
			response.setContentType("text/html;charset=utf-8");
			response.setCharacterEncoding("utf-8");
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.print(html);
			out.flush();
		} finally {
			out.close();
		}
	}

	/**
	 * 汉口 H5
	 * @param sysPayOrderNo
	 * @param response
	 */
	@RequestMapping("/hk_{sysPayOrderNo}.html")
	public void hankou(@PathVariable("sysPayOrderNo") String sysPayOrderNo, HttpServletResponse response) {

		logger.info("...[hankou pay] pay order action...");
		ShopPay shopPayDto = shopPayService.getBySysOrderNo(sysPayOrderNo);
		Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopPayDto.getThirdChannelId());
		ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

		Map<String, String> params = new HashMap<>();
		params.put("pay_memberid", thirdChannelDto.getMerchantId());
		params.put("pay_applydate", DateUtil.getSystemTime(DateUtil.DATE_PATTERN_01));
		params.put("pay_amount", String.valueOf(shopPayDto.getMerchantPayMoney()));
		params.put("pay_notifyurl", getBaseNotifyUrl() + thirdChannelDto.getNotifyUrl());
		params.put("pay_orderid", shopPayDto.getSysPayOrderNo());
		params.put("pay_callbackurl", "http://www.baidu.com");
		params.put("pay_bankcode", "903");

		logger.info("[hankou before sign msg]: " + params);
		// 签名   key不参与排序
		String sign = null;
		try {
			sign = HKUtil.generateMd5Sign(params, thirdChannelDto.getPayMd5Key());
		} catch (Exception e) {
			logger.info("【hankou pay sign exception】");
		}
		logger.info("[hankou sign str]: " + sign);
		params.put("pay_md5sign", sign);

		String html = createAutoFormHtml(thirdChannelDto.getPayUrl(), params, "UTF-8", "POST");
		logger.info("...[hankou pay] html:" + html);
		PrintWriter out = null;
		try {
			response.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");
			out = response.getWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.print(html);
			out.flush();
		} finally {
			out.close();
		}
	}

	/**
	 * 功能：前台交易构造HTTP POST自动提交表单<br>
	 *
	 * @param reqUrl
	 *            表单提交地址<br>
	 * @param hiddens
	 *            以MAP形式存储的表单键值<br>
	 * @param encoding
	 *            上送请求报文域encoding字段的值<br>
	 * @return 构造好的HTTP POST交易表单<br>
	 */
	public static String createAutoFormHtml(String reqUrl, Map<String, String> hiddens, String encoding, String method) {
		StringBuffer sf = new StringBuffer();
		 sf.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + encoding + "\"/></head><body>");
		 sf.append("<form id = \"pay_form\" action=\"" + reqUrl + "\" method=\""+ method +"\" enctype=\"application/x-www-form-urlencoded\">");
		if (null != hiddens && 0 != hiddens.size()) {
			Set<Map.Entry<String, String>> set = hiddens.entrySet();
			Iterator<Map.Entry<String, String>> it = set.iterator();

			while (it.hasNext()) {
				Map.Entry<String, String> ey = it.next();
				String key = ey.getKey();
				String value = ey.getValue();
				sf.append("<input type=\"hidden\" name=\"" + key + "\" id=\"" + key + "\" value='" + value + "'/>");//此处单引号慎改，for 爱农
			}
		}
		 sf.append("</form>");
		 sf.append("</body>");
		 sf.append("<script type=\"text/javascript\">");
		 sf.append("document.all.pay_form.submit();");
		 sf.append("</script>");
		 sf.append("</html>");
		return sf.toString();
	}

}
