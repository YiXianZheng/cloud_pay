package com.cloud.finance.third.wuliu.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.service.base.BasePayService;
import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.service.ShopPayService;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.provider.SysBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Auther Toney
 * @Date 2018/11/01 14:12
 * @Description:
 */
@Service("WuliuPayService")
public class WuliuPayService implements BasePayService {
    private static Logger logger = LoggerFactory.getLogger(WuliuPayService.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopPayService payService;
    @Autowired
    private SysBankProvider sysBankProvider;

    private String getBasePayUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "PAY_BASE_URL");
    }

    private String getBaseNotifyUrl(){
        return redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "NOTIFY_BASE_URL");
    }

    @Override
    public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {

        logger.info("[wuliu qrcode params]:channelId:"+thirdChannelDto.getId()+ ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());

        String actionRespCode = SysPayResultConstants.SUCCESS_MAKE_ORDER+"";
        String actionRespMessage = "生成跳转地址成功";

        String actionRespUrl = getBasePayUrl() + "/d8/wl_" + shopPayDto.getSysPayOrderNo() + ".html";
        String channelPayOrderNo = shopPayDto.getSysPayOrderNo();
        MidPayCreateResult payCreateResult = new MidPayCreateResult();

        payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
        payCreateResult.setResultCode(actionRespCode);
        payCreateResult.setResultMessage(actionRespMessage);
        payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
        payCreateResult.setChannelOrderNo(channelPayOrderNo);
        payCreateResult.setPayUrl(actionRespUrl);

        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        return  null;
//        logger.info("[wuliu pay create params]:channelId:" + thirdChannelDto.getId() + ", sysOrderNo:" + shopPayDto.getSysPayOrderNo());
//
//        MidPayCreateResult payCreateResult = new MidPayCreateResult();
//        payCreateResult.setStatus("error");
//
//        /** 支付请求参数 **/
//        String MERCHANTID = thirdChannelDto.getMerchantId();    //商户代码
//        String POSID = thirdChannelDto.getAppId();              //商户柜台代码
//        String BRANCHID = thirdChannelDto.getAppKey();          //分行代码
//        String tmpNo = shopPayDto.getSysPayOrderNo();
//        String ORDERID = MERCHANTID + "_" + tmpNo.subSequence(tmpNo.length()-14, tmpNo.length());//定单号 (最长30位,商户代码(15位)+自定义字符串(不超过15位))
//        String PAYMENT= SafeComputeUtils.numberFormate(shopPayDto.getMerchantPayMoney());   //付款金额
//        String CURCODE="01";                                    //币种 01－人民币
//        String TXCODE = "530550";                               //交易码  由建行统一分配为530550
//        String RETURNTYPE="2";                                  //返回类型 0或空：返回页面二维
//                                                                //        1：返回JSON格式【二维码信息串】
//                                                                //        2：返回聚合扫码页面二维码
//                                                                //        3：返回聚合扫码JSON格式【二维码信息串】
//                                                                //        聚合扫码只能上送2或3
//        String REMARK1 = "";                                    //备注一
//        String REMARK2 = "";                                    //备注二
//        String TIMEOUT = "";                                    //订单超时时间
//
//        String key = thirdChannelDto.getPayMd5Key();
//        String PUB32TR2= key.substring(key.length()-30,key.length());//字段为对应柜台的公钥后30位
//
//        StringBuffer tmp = new StringBuffer(); //验签字段
//        tmp.append("MERCHANTID=");
//        tmp.append(MERCHANTID);
//        tmp.append("&POSID=");
//        tmp.append(POSID);
//        tmp.append("&BRANCHID=");
//        tmp.append(BRANCHID);
//        tmp.append("&ORDERID=");
//        tmp.append(ORDERID);
//        tmp.append("&PAYMENT=");
//        tmp.append(PAYMENT);
//        tmp.append("&CURCODE=");
//        tmp.append(CURCODE);
//        tmp.append("&TXCODE=");
//        tmp.append(TXCODE);
//        tmp.append("&REMARK1=");
//        tmp.append(REMARK1);
//        tmp.append("&REMARK2=");
//        tmp.append(REMARK2);
//        tmp.append("&RETURNTYPE=");
//        tmp.append(RETURNTYPE);
//        tmp.append("&TIMEOUT=");
//        tmp.append(TIMEOUT);
//        tmp.append("&PUB=");
//        tmp.append(PUB32TR2);
//
//        //签名
//        String sign = tmp.toString();
//        logger.info("[wuliu before sign msg]:" + sign);
//        sign = WuliuMD5.md5Str(tmp.toString());
//        logger.info("[wuliu sign msg]:"+sign);
//
//        Map map = new HashMap();
//        map.put("CCB_IBSVersion","V6");	//必输项
//        map.put("MERCHANTID",MERCHANTID);
//        map.put("BRANCHID",BRANCHID);
//        map.put("POSID",POSID);
//        map.put("ORDERID",ORDERID);
//        map.put("PAYMENT",PAYMENT);
//        map.put("CURCODE",CURCODE);
//        map.put("TXCODE",TXCODE);
//        map.put("REMARK1",REMARK1);
//        map.put("REMARK2",REMARK2);
//        map.put("RETURNTYPE",RETURNTYPE);
//        map.put("TIMEOUT",TIMEOUT);
//        map.put("MAC",WuliuMD5.md5Str(tmp.toString()));
//
//        String baowen = MD5Utils.getSignParam(map);
//        logger.info("[wuliu request params]:"+baowen);
//
//        try {
//            String jsonStr = PostUtils.jsonPost(thirdChannelDto.getPayUrl(), map);
//            if(StringUtils.isEmpty(jsonStr)){
//                logger.error("【通道支付请求请求结果为空】");
//                payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");
//
//                return payCreateResult;
//            }
//            logger.info("pay post result == > "+ jsonStr);
//
//            PayURLVo payURLVo = JSONObject.parseObject(jsonStr, PayURLVo.class);
//            logger.info("pay url == > "+ payURLVo.getPAYURL());
//
//            String jsonStr2 = GetUtils.sendGetMethodForCharset(payURLVo.getPAYURL(), null, "UTF-8");
//            logger.info("pay get result == > "+ jsonStr2);
//            QrURLVo qrURLVo = JSONObject.parseObject(jsonStr, QrURLVo.class);
//
//            if("true".equals(qrURLVo.getSUCCESS())){
//                payService.updateThirdInfo(shopPayDto.getSysPayOrderNo(), thirdChannelDto.getId());
//                payCreateResult.setStatus("true");
//                payCreateResult.setResultCode(SysPayResultConstants.SUCCESS_MAKE_ORDER+"");
//                payCreateResult.setResultMessage("成功生成跳转地址");
//                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
//                payCreateResult.setChannelOrderNo("");
//                payCreateResult.setPayUrl(qrURLVo.getQRURL());
//                logger.info("【通道支付请求成功】------- 成功生成支付链接");
//            }else{
//                payCreateResult.setStatus("false");
//                payCreateResult.setResultMessage("生成跳转地址失败");
//                payCreateResult.setSysOrderNo(shopPayDto.getSysPayOrderNo());
//                logger.error("【通道支付请求失败】------- 生成跳转地址失败");
//            }
//            return payCreateResult;
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error("【通道支付请求异常】-------");
//            payCreateResult.setResultCode(SysPayResultConstants.ERROR_SYS_PARAMS+"");
//        }
//        return payCreateResult;
    }

    @Override
    public MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCreateResult createQuickJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;

    }

    @Override
    public MidPayCreateResult createSytAllIn(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MidPayCheckResult checkOrderResult(ThirdChannelDto thirdChannelDto, ShopPay shopPay) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createSysPayOrderId(String channelId, String assId, String assPayOrderNo) {
        String tm = System.currentTimeMillis() + "";// 获取系统毫秒时间戳
        // 4位+13位+3位随机数
        String sysPayOrderNo = channelId + tm + StringUtil.getRandom(3);
        return sysPayOrderNo;
    }

    @Override
    public ChannelAccountData queryAccount(ThirdChannelDto thirdChannelDto) {
        // TODO Auto-generated method stub
        return null;
    }
}
