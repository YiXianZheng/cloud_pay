package com.cloud.finance.common.service.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Auther Toney
 * @Date 2018/9/14 15:56
 * @Description: 所有支付通道都需要再次注册
 */
@Component
public class PayServiceFactory {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("AinongPayService")
    BasePayService ainongPayService;
    @Autowired
    @Qualifier("XinbaoPayService")
    BasePayService xinbaoPayService;
    @Autowired
    @Qualifier("HcPayService")
    BasePayService hcPayService;
    @Autowired
    @Qualifier("Shtd1PayService")
    BasePayService shtd1PayService;
    @Autowired
    @Qualifier("JinxinPayService")
    BasePayService jinxinPayService;
    @Autowired
    @Qualifier("XunjiefuPayService")
    BasePayService xunjiefuPayService;
    @Autowired
    @Qualifier("MoshangPayService")
    BasePayService moshangPayService;
    @Autowired
    @Qualifier("HangzhouPayService")
    BasePayService hangzhouPayService;
    @Autowired
    @Qualifier("YirongtongPayService")
    BasePayService yirongtongPayService;
    @Autowired
    @Qualifier("KubaoxiangPayService")
    BasePayService kubaoxiangPayService;
    @Autowired
    @Qualifier("BeijingPayService")
    BasePayService beijingPayService;
    @Autowired
    @Qualifier("HankouPayService")
    BasePayService hankouPayService;
    @Autowired
    @Qualifier("AnyinfuPayService")
    BasePayService anyinfuPayService;
    @Autowired
    @Qualifier("WuliuPayService")
    BasePayService wuliuPayService;
    @Autowired
    @Qualifier("XinfulaiPayService")
    BasePayService xinfulaiPayService;
    @Autowired
    @Qualifier("ZhonghuaPayService")
    BasePayService zhonghuaPayService;
    @Autowired
    @Qualifier("GuanjunPayService")
    BasePayService guanjunPayService;
    @Autowired
    @Qualifier("YunjifuPayService")
    BasePayService yunjifuPayService;


    public BasePayService getPayment(String channelCode){
        if("ainong_pay".equals(channelCode)){
            logger.info("pay chose channel ainong ");
            return ainongPayService;
        }else if("xinbao_pay".equals(channelCode)){
            logger.info("pay chose channel xinbao ");
            return xinbaoPayService;
        }else if("hc_pay".equals(channelCode)){
            logger.info("pay chose channel hc ");
            return hcPayService;
        }else if("shtd1_pay".equals(channelCode)){
            logger.info("pay chose channel shanghai channel 1 ");
            return shtd1PayService;
        }else if("jinxin_pay".equals(channelCode)){
            logger.info("pay chose channel jinxin ");
            return jinxinPayService;
        }else if("xunjiefu_pay".equals(channelCode)){
            logger.info("pay chose channel xunjiefu ");
            return xunjiefuPayService;
        }else if("moshang_pay".equals(channelCode)){
            logger.info("pay chose channel moshang ");
            return moshangPayService;
        }else if("hangzhou_pay".equals(channelCode)){
            logger.info("pay chose channel hangzhou ");
            return hangzhouPayService;
        }else if("yirongtong_pay".equals(channelCode)){
            logger.info("pay chose channel yirongtong ");
            return yirongtongPayService;
        }else if("kubaoxiang_pay".equals(channelCode)){
            logger.info("pay chose channel kubaoxiang");
            return kubaoxiangPayService;
        }else if("beijing_pay".equals(channelCode)){
            logger.info("pay chose channel beijing ");
            return beijingPayService;
        }else if("hankou_pay".equals(channelCode)){
            logger.info("pay chose channel hankou ");
            return hankouPayService;
        }else if("anyinfu_pay".equals(channelCode)){
            logger.info("pay chose channel anyinfu ");
            return anyinfuPayService;
        }else if("wuliu_pay".equals(channelCode)){
            logger.info("pay chose channel wuliu ");
            return wuliuPayService;
        }else if("xinfulai_pay".equals(channelCode)){
            logger.info("pay chose channel xinfulai ");
            return xinfulaiPayService;
        }else if("zhonghua_pay".equals(channelCode)){
            logger.info("pay chose channel zhonghua");
            return zhonghuaPayService;
        }else if("guanjun_pay".equals(channelCode)){
            logger.info("pay chose channel guanjunPayService");
            return guanjunPayService;
        }else if("yunjifu_pay".equals(channelCode)){
            logger.info("pay chose channel yunjifuPayService");
            return yunjifuPayService;
        }else{
            return null;
        }
    }
}
