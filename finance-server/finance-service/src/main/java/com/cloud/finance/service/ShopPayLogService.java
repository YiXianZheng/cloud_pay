package com.cloud.finance.service;

import com.cloud.finance.po.ShopPayLog;
import com.cloud.sysconf.common.basePDSC.BaseMybatisService;

/**
 * ShopPay的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface ShopPayLogService extends BaseMybatisService<ShopPayLog, String> {

    /**
     * 统计历史库中的数据并将统计数据缓存到Redis中
     */
    void countHistory();

    /**
     * 通过商户号和商户订单号获取订单信息
     * @param merchantCode
     * @param merchantOrderNo
     * @return
     */
    ShopPayLog getByMerchantCodeAndOrderNo(String merchantCode, String merchantOrderNo);
}
