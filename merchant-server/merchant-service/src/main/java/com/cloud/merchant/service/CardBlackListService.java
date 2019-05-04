package com.cloud.merchant.service;

import com.cloud.merchant.po.CardBlackList;
import com.cloud.sysconf.common.basePDSC.BaseMybatisService;

/**
 * @description: ${description}
 * @author: zyx
 * @create: 2019-05-04 14:46
 **/
public interface CardBlackListService extends BaseMybatisService<CardBlackList, String> {

    boolean checkCardSafe(String bankCardHolder);
}
