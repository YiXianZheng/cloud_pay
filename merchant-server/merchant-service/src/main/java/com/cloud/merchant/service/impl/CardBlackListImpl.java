package com.cloud.merchant.service.impl;

import com.cloud.merchant.dao.CardBlackListDao;
import com.cloud.merchant.po.CardBlackList;
import com.cloud.merchant.service.CardBlackListService;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: ${description}
 * @author: zyx
 * @create: 2019-05-04 14:48
 **/
@Service
public class CardBlackListImpl extends BaseMybatisServiceImpl<CardBlackList, String, CardBlackListDao> implements CardBlackListService {

    private Logger logger = LoggerFactory.getLogger(CardBlackListImpl.class);
    @Autowired
    private CardBlackListDao cardBlackListDao;

    @Override
    public boolean checkCardSafe(String bankCardHolder) {

        logger.info("开户人：" + bankCardHolder);
        CardBlackList cardBlackList = cardBlackListDao.getByHolder(bankCardHolder);
        // cardBlackList == null 表示黑名单列表不存在该持卡人
        return cardBlackList != null;
    }
}
