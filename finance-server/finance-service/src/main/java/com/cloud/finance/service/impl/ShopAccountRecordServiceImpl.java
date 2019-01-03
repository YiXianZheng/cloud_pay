package com.cloud.finance.service.impl;

import com.cloud.finance.common.dto.ChannelSummaryDto;
import com.cloud.finance.common.enums.AccountRecordStatusEnum;
import com.cloud.finance.common.enums.AccountRecordTypeEnum;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.dao.ShopAccountRecordDao;
import com.cloud.finance.po.ShopAccountRecord;
import com.cloud.finance.po.ShopPay;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopAccountRecordService;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * @Auther Toney
 * @Date 2018/7/29 17:52
 * @Description:
 */
@Service
public class ShopAccountRecordServiceImpl extends BaseMybatisServiceImpl<ShopAccountRecord, String, ShopAccountRecordDao> implements ShopAccountRecordService {

    @Autowired
    private ShopAccountRecordDao shopAccountRecordDao;
    @Autowired
    private RedisClient redisClient;

    @Override
    public void addRecord(Integer type, ShopPay shopPay, ShopRecharge shopRecharge, Integer status) {
        ShopAccountRecord shopAccountRecord = new ShopAccountRecord();
        String userId = null;
        if(shopPay != null) {
            userId = redisClient.Gethget(RedisConfig.MERCHANT_INFO_DB, shopPay.getMerchantCode(), "sysUserId");
        }else if(shopRecharge != null){
            userId = shopRecharge.getUserId();
        }
        if(StringUtils.isBlank(userId)){
            logger.info("[add account record error] userId is null");
            redisClient.SetHsetJedis(RedisConfig.ERROR_LOG_DB, "Add Account Record Error", DateUtil.DateToString(new Date(),
                    DateUtil.DATE_PATTERN_01), "type:"+ type + "; shopPayOrderNo:" + shopPay.getSysPayOrderNo() +
                    "; shopRechargeNo:" + shopRecharge.getRechargeNo() + "; status:" + status);
            return;
        }
        if(1 == type || 3 == type || 4 == type){//支付入账、冻结或者解冻
            shopAccountRecord.setSysUserId(userId);
            shopAccountRecord.setChannelId(shopPay.getThirdChannelId());
            shopAccountRecord.setType(type);
            shopAccountRecord.setUnionOrderNo(shopPay.getSysPayOrderNo());
            shopAccountRecord.setTotalAmount(shopPay.getMerchantPayMoney());
            shopAccountRecord.setPoundage(shopPay.getMerchantCostMoney());
            shopAccountRecord.setOwnAmount(SafeComputeUtils.sub(shopPay.getMerchantPayMoney(), shopPay.getMerchantCostMoney()));
            shopAccountRecord.setStatus(status);
            shopAccountRecord.preInsert(userId, shopPay.getPanId());
            shopAccountRecordDao.add(shopAccountRecord);
            logger.info("[add account record success] 账变类型：" + AccountRecordTypeEnum.getByCode(type) +
                    "；账变结果：" + AccountRecordStatusEnum.getByCode(status));
        }else if(2 == type){//代付出账
            shopAccountRecord.setSysUserId(userId);
            shopAccountRecord.setChannelId(shopRecharge.getThirdChannelId());
            shopAccountRecord.setType(type);
            shopAccountRecord.setUnionOrderNo(shopRecharge.getRechargeNo());
            shopAccountRecord.setTotalAmount(shopRecharge.getRechargeMoney());
            shopAccountRecord.setPoundage(shopRecharge.getRechargeRateMoney());
            shopAccountRecord.setOwnAmount(SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney()));
            shopAccountRecord.setStatus(status);
            shopAccountRecord.preInsert(userId, shopRecharge.getPanId());
            shopAccountRecordDao.add(shopAccountRecord);
            logger.info("[add account record success] 账变类型：" + AccountRecordTypeEnum.getByCode(type) +
                    "；账变结果：" + AccountRecordStatusEnum.getByCode(status));
        }
    }

    @Override
    public ReturnVo updateRecordStatus(String unionOrderNo, Integer status) {
        ShopAccountRecord shopAccountRecord = shopAccountRecordDao.getByUnionOrderNo(unionOrderNo);
        if(shopAccountRecord != null){
            if(AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_DOING.getCode() == shopAccountRecord.getStatus()){
                //账变处理中才可以操作
                shopAccountRecordDao.updateStatus(shopAccountRecord.getId(), status);
                return ReturnVo.returnSuccess();
            }else{
                logger.info("[update account record status error] 当前状态不可操作");
                redisClient.SetHsetJedis(RedisConfig.ERROR_LOG_DB, "Update Account Record Status Error", DateUtil.DateToString(new Date(),
                        DateUtil.DATE_PATTERN_01), "unionOrderNo:"+ unionOrderNo + "; status:" + status);
                return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前状态不可操作"));
            }
        }else {
            logger.info("[update account record status error] 账变记录不存在");
            redisClient.SetHsetJedis(RedisConfig.ERROR_LOG_DB, "Update Account Record Status Error", DateUtil.DateToString(new Date(),
                    DateUtil.DATE_PATTERN_01), "unionOrderNo:"+ unionOrderNo + "; status:" + status);
            return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "账变记录不存在"));
        }
    }

    @Override
    public List<ChannelSummaryDto> channelSummary(Date beginTime, Date endTime, String sysUserId) {
        return shopAccountRecordDao.channelSummary(sysUserId, beginTime, endTime);
    }

    @Override
    public void updateStep() {
        shopAccountRecordDao.updateStep();
    }

    @Override
    public void updateChannelId(String unionOrderNo, String channelId) {
        ShopAccountRecord shopAccountRecord = shopAccountRecordDao.getByUnionOrderNo(unionOrderNo);
        if(shopAccountRecord != null){
            if(StringUtils.isNotBlank(shopAccountRecord.getChannelId())){
                logger.info("[update channel id error] 只有通道ID为空的代付单才能进行此操作");
            }else{
                shopAccountRecordDao.updateChannelId(shopAccountRecord.getId(), channelId);
            }
        }else {
            logger.info("[update channel id error] 账变记录不存在");
        }
    }
}
