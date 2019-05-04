package com.cloud.finance.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cloud.agent.common.dto.AgentInfoDto;
import com.cloud.agent.provider.AgentUserProvider;
import com.cloud.finance.common.dto.AccountDto;
import com.cloud.finance.common.dto.RedisFinanceDto;
import com.cloud.finance.common.enums.AccountRecordStatusEnum;
import com.cloud.finance.common.enums.AccountRecordTypeEnum;
import com.cloud.finance.common.enums.RechargeStatusEnum;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.dao.ShopRechargeDao;
import com.cloud.finance.po.ShopAccount;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.FinanceService;
import com.cloud.finance.service.ShopAccountRecordService;
import com.cloud.finance.service.ShopAccountService;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.merchant.common.dto.MerchantInfoDto;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.utils.page.PageResult;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ResultVo;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/7/29 17:52
 * @Description:
 */
@Service
public class ShopRechargeServiceImpl extends BaseMybatisServiceImpl<ShopRecharge, String, ShopRechargeDao> implements ShopRechargeService {

    @Autowired
    private ShopRechargeDao shopRechargeDao;
    @Autowired
    private AgentUserProvider agentUserProvider;
    @Autowired
    private MerchantUserProvider merchantUserProvider;
    @Autowired
    private ShopAccountService accountService;
    @Autowired
    private FinanceService financeService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopAccountRecordService shopAccountRecordService;

    @Transactional
    @Override
    public ResultVo checkAccount(CashReqData cashReqData, HeaderInfoDto headerInfoDto) {

        Integer rechargeRateType = null;
        Double rechargeRate = null;
        Double rechargeMoney = null;

        ShopAccount shopAccount = accountService.getByUserId(headerInfoDto.getCurUserId());
        if(shopAccount == null){
            accountService.initAccount(headerInfoDto);
        }
        if(shopAccount.getUsableMoney()<cashReqData.getAmount()){
            return new ResultVo("1", "账户余额不足");
        }
        if(ShopAccount.STATUS_FROZEN == shopAccount.getStatus()){
            return new ResultVo("1", "账户已被冻结，暂不可代付");
        }
        if(StringUtils.isEmpty(cashReqData.getKey())) {
            return new ResultVo("1", "安全码不能为空");
        }
        if(!cashReqData.getKey().equals(shopAccount.getSecurityCode())){
            return new ResultVo("1", "安全码错误");
        }
        String bankNo = cashReqData.getBankNo();
        ApiResponse apiResponse = merchantUserProvider.getBankBin(bankNo);
        if (!apiResponse.getCode().equals(ResponseCode.Base.SUCCESS + "")) {
            return new ResultVo("1", "银行卡不存在");
        }
        String json = JSONObject.toJSONString(apiResponse.getData());
        Map<Object, Object> params = MapUtils.json2Map(json);
        if ("0".equals(params.get("cardStatus").toString())) {
            return new ResultVo("1", "银行卡未审核");
        }

        //判断是否可以申请代付
        if(StringUtils.isNotBlank(headerInfoDto.getAgentUser())){
            ApiResponse response = agentUserProvider.detailById(headerInfoDto.getAgentUser());
            if((ResponseCode.Base.SUCCESS.getCode()+"").equals(response.getCode())){
                Map agentInfoDto = (HashMap) response.getData();
                if(!(AgentInfoDto.CASH_STATUS_COMMON+"").equals(agentInfoDto.get("cashStatus").toString())){
                    //不可代付
                    return new ResultVo("1", "商户代付权限被关闭");
                }else{
                    rechargeRateType = Integer.parseInt(agentInfoDto.get("commissionType").toString());
                    if(AgentInfoDto.COMMISSION_TYPE_FIXED == rechargeRateType){
                        rechargeMoney = Double.parseDouble(agentInfoDto.get("commissionCharge").toString());
                    }else{
                        rechargeRate = Double.parseDouble(agentInfoDto.get("commissionCharge").toString());
                        rechargeMoney = SafeComputeUtils.multiply(cashReqData.getAmount(), rechargeRate);
                    }
                }
            }
        }else if(StringUtils.isNotBlank(headerInfoDto.getMerchantUser())){
            ApiResponse response = merchantUserProvider.detailById(headerInfoDto.getMerchantUser());

            if((ResponseCode.Base.SUCCESS.getCode()+"").equals(response.getCode())){
                Map merchantInfoDto = (HashMap) response.getData();
                logger.info("商户信息：" + merchantInfoDto);
                if(!(MerchantInfoDto.CASH_STATUS_COMMON+"").equals(merchantInfoDto.get("cashStatus").toString())){
                    //不可代付
                    return new ResultVo("1", "商户代付权限被关闭");
                }else{
                    rechargeRateType = Integer.parseInt(merchantInfoDto.get("commissionType").toString());
                    if(AgentInfoDto.COMMISSION_TYPE_FIXED == rechargeRateType){
                        rechargeMoney = Double.parseDouble(merchantInfoDto.get("commissionCharge").toString());
                    }else{
                        rechargeRate = Double.parseDouble(merchantInfoDto.get("commissionCharge").toString());
                        rechargeMoney = SafeComputeUtils.multiply(cashReqData.getAmount(), rechargeRate);
                    }

                    // 获取限制次数
                    String rechargeLimit = StringUtil.isBlank(merchantInfoDto.get("rechargeLimit").toString()) ? "0" : merchantInfoDto.get("rechargeLimit").toString();
                    logger.info("限制笔数：" + rechargeLimit);
                    // 获取状态为未审核的代付笔数
                    int rechargeNum = shopRechargeDao.getByMerId(headerInfoDto.getMerchantUser());
                    logger.info("代付笔数：" + rechargeNum);
                    if (rechargeNum >= Integer.parseInt(rechargeLimit)) {
                        logger.error("商户代付未审核笔数为：" + rechargeNum + "，限制笔数为：" + rechargeLimit);
                        return new ResultVo("1", "商户代付笔数已限制");
                    }
                }
            }
        }

        //生成代付单
        ShopRecharge shopRecharge = new ShopRecharge();
        shopRecharge.setUserId(headerInfoDto.getCurUserId());
        shopRecharge.setAgentUser(headerInfoDto.getAgentUser());
        shopRecharge.setMerchantUser(headerInfoDto.getMerchantUser());
        shopRecharge.setRechargeNo("DF"+ DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13));
        shopRecharge.setRechargeMoney(cashReqData.getAmount());

        shopRecharge.setRechargeRateType(rechargeRateType);
        shopRecharge.setRechargeRate(rechargeRate);
        shopRecharge.setRechargeRateMoney(rechargeMoney);

        shopRecharge.setBankAccount(cashReqData.getBankAccount());
        shopRecharge.setBankCode(cashReqData.getBankCode());
        shopRecharge.setBankNo(cashReqData.getBankNo());
        shopRecharge.setBankBin(cashReqData.getBin());
        shopRecharge.setBankSubbranch(cashReqData.getSubbranch());
        shopRecharge.setProvince(cashReqData.getProvince());
        shopRecharge.setCity(cashReqData.getCity());

        shopRecharge.setRechargeStatus(0);
        shopRecharge.setCompleteTime(null);
        shopRecharge.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());

        shopRechargeDao.add(shopRecharge);

        accountService.frozenAccount(headerInfoDto.getCurUserId(), cashReqData.getAmount());

        shopAccountRecordService.addRecord(AccountRecordTypeEnum.ACCOUNT_RECORD_TYPE_RECHARGE.getCode(), null,
                shopRecharge, AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_DOING.getCode());

        return new ResultVo("0", "成功生成平台代付单", shopRecharge);
    }

    @Override
    public ShopRecharge getByRechargeNo(String rechargeNo) {
        return shopRechargeDao.getByRechargeNo(rechargeNo);
    }

    @Override
    public AccountDto countTotalRecharge(String sysUserId) {
        return shopRechargeDao.countTotalRecharge(sysUserId);
    }

    @Override
    public ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto) {
        try {
            PageResult pageResult = this.queryForTablePage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, JSONObject.toJSON(pageResult));
        }catch (Exception e){
            return ReturnVo.returnFail();
        }
    }

    @Transactional
    @Override
    public int rechargeSuccess(ShopRecharge shopRecharge) {
        accountService.updateRecharge(shopRecharge.getUserId(), shopRecharge.getRechargeMoney());
        logger.info("[代付成功] RechargeNo: "+shopRecharge.getRechargeNo());
        shopAccountRecordService.updateChannelId(shopRecharge.getRechargeNo(), shopRecharge.getThirdChannelId());
        logger.info("[更新账变明细的通道ID] RechargeNo: "+shopRecharge.getRechargeNo() + "; channelId: "+shopRecharge.getThirdChannelId());
        shopAccountRecordService.updateRecordStatus(shopRecharge.getRechargeNo(), AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_SUCCESS.getCode());
        logger.info("更新账变明细的状态  --> " + AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_FAIL.getCode());
        return shopRechargeDao.rechargeSuccess(shopRecharge);
    }

    @Override
    public void updateThirdInfo(ShopRecharge shopRecharge) {
        shopRechargeDao.updateThirdInfo(shopRecharge);
    }

    @Transactional
    @Override
    public int rechargeFail(ShopRecharge shopRecharge) {
        accountService.frozenAccount(shopRecharge.getUserId(), - shopRecharge.getRechargeMoney());
        logger.info("[代付失败] RechargeNo: "+shopRecharge.getRechargeNo());
        shopAccountRecordService.updateChannelId(shopRecharge.getRechargeNo(), shopRecharge.getThirdChannelId());
        logger.info("[更新账变明细的通道ID] RechargeNo: "+shopRecharge.getRechargeNo() + "; channelId: "+shopRecharge.getThirdChannelId());
        shopAccountRecordService.updateRecordStatus(shopRecharge.getRechargeNo(), AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_FAIL.getCode());
        logger.info("更新账变明细的状态  --> " + AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_FAIL.getCode());
        return shopRechargeDao.rechargeFail(shopRecharge);
    }

    @Transactional
    @Override
    public ResultVo checkAccountForAPI(String merCode, Double amount, String key, String bankAccount, String bankCode, String bankNo,
                                       String bankSubbranch, String bin, String province, String city) {
        Integer rechargeRateType = null;
        Double rechargeRate = null;
        Double rechargeMoney = null;
        String sysUserId = null;

        ShopAccount shopAccount = accountService.getAccountByMerchantCode(merCode);
        boolean isNew = false;
        if(shopAccount == null) {
            isNew = true;
        }
        financeService.initOverview(3, merCode);
        String merId = redisClient.Gethget(RedisConfig.MERCHANT_INFO_DB, merCode, "id");
        ApiResponse response = merchantUserProvider.detailByCode(merCode);
        if((ResponseCode.Base.SUCCESS.getCode()+"").equals(response.getCode())){
            Map merchantInfoDto = (HashMap) response.getData();
            if(!(MerchantInfoDto.CASH_STATUS_COMMON+"").equals(merchantInfoDto.get("cashStatus").toString())){
                //不可代付
                logger.error("商户代付权限被关闭");
                return new ResultVo("1", "商户代付权限被关闭");
            }else{
                sysUserId = merchantInfoDto.get("sysUserId").toString();
                rechargeRateType = Integer.parseInt(merchantInfoDto.get("commissionType").toString());
                if(AgentInfoDto.COMMISSION_TYPE_FIXED == rechargeRateType){
                    rechargeMoney = Double.parseDouble(merchantInfoDto.get("commissionCharge").toString());
                }else{
                    rechargeRate = Double.parseDouble(merchantInfoDto.get("commissionCharge").toString());
                    rechargeMoney = SafeComputeUtils.multiply(amount, rechargeRate);
                }
            }

            // 获取限制次数
            String rechargeLimit = merchantInfoDto.get("rechargeLimit").toString();
            rechargeLimit = StringUtil.isEmpty(rechargeLimit) ? "0" : rechargeLimit;
            logger.info("限制笔数：" + rechargeLimit);
            // 获取状态为未审核的代付笔数
            int rechargeNum = shopRechargeDao.getByMerId(merId);
            logger.info("代付笔数：" + rechargeNum);
            if (rechargeNum >= Integer.parseInt(rechargeLimit)) {
                logger.error("商户代付未审核笔数为：" + rechargeNum + "，限制笔数为：" + rechargeLimit);
                return new ResultVo("1", "商户代付笔数已限制");
            }
        }

        Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, merCode);
        RedisFinanceDto redisFinanceDto = RedisFinanceDto.map2Object(map);
        Double totalCharge = SafeComputeUtils.add(redisFinanceDto.getHistoryTotalCharge(),
                SafeComputeUtils.add(redisFinanceDto.getTotalCharge(),redisFinanceDto.getDailyTotalCharge()));

        shopAccount = new ShopAccount();
        shopAccount.setSysUserId(sysUserId);
        shopAccount.setTotalMoney(totalCharge);
        shopAccount.setSecurityCode(StringUtil.getRandom(6));
        shopAccount.setStatus(ShopAccount.STATUS_COMMON);

        AccountDto accountDto = shopRechargeDao.countTotalRecharge(sysUserId);
        shopAccount.setUsableMoney(SafeComputeUtils.sub(SafeComputeUtils.sub(totalCharge, accountDto.getAmount()), accountDto.getFrozenAmount()));
        shopAccount.setFrozenMoney(accountDto.getFrozenAmount());
        shopAccount.setRechargeMoney(accountDto.getAmount());

        try{
            if(isNew) {
                accountService.add(shopAccount);
            }else{
                accountService.updateAccountInfo(shopAccount);
            }
        }catch (Exception e){
            logger.info("账户异常");
            return new ResultVo("1", "账户异常");
        }

        if(shopAccount.getUsableMoney()<amount){
            return new ResultVo("1", "账户余额不足");
        }
        if(ShopAccount.STATUS_FROZEN == shopAccount.getStatus()){
            return new ResultVo("1", "账户已被冻结，暂不可代付");
        }
        if(StringUtils.isEmpty(key)) {
            return new ResultVo("1", "安全码不能为空");
        }
        if(!key.equals(shopAccount.getSecurityCode())){
            return new ResultVo("1", "安全码错误");
        }

        //生成代付单
        ShopRecharge shopRecharge = new ShopRecharge();
        shopRecharge.setUserId(shopAccount.getSysUserId());
        shopRecharge.setAgentUser(null);
        shopRecharge.setMerchantUser(merId);
        shopRecharge.setRechargeNo("DF"+ DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13));
        shopRecharge.setRechargeMoney(amount);

        shopRecharge.setRechargeRateType(rechargeRateType);
        shopRecharge.setRechargeRate(rechargeRate);
        shopRecharge.setRechargeRateMoney(rechargeMoney);

        shopRecharge.setBankAccount(bankAccount);
        shopRecharge.setBankCode(bankCode);
        shopRecharge.setBankNo(bankNo);
        shopRecharge.setBankSubbranch(bankSubbranch);
        shopRecharge.setBankBin(bin);
        shopRecharge.setProvince(province);
        shopRecharge.setCity(city);

        shopRecharge.setRechargeStatus(0);
        shopRecharge.setCompleteTime(null);
        shopRecharge.preInsert(shopAccount.getSysUserId(), "0000");

        shopRechargeDao.add(shopRecharge);

        accountService.frozenAccount(sysUserId, amount);

        shopAccountRecordService.addRecord(AccountRecordTypeEnum.ACCOUNT_RECORD_TYPE_RECHARGE.getCode(), null,
                shopRecharge, AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_DOING.getCode());

        return new ResultVo("0", "成功生成平台代付单", shopRecharge);
    }

    @Override
    public ShopRecharge apiCreate(ShopRecharge shopRecharge) {
        String rechargeNo = "DF"+ DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_13);
        shopRecharge.setRechargeNo(rechargeNo);
        shopRecharge.setRemarks("手动补单");
        shopRecharge.setRechargeStatus(1);
        shopRecharge.setCompleteTime(new Date());
        shopRechargeDao.add(shopRecharge);
        return shopRechargeDao.getByRechargeNo(rechargeNo);
    }

    @Override
    public ReturnVo updateRechargeStatus(ShopRecharge shopRecharge) {
        if(shopRechargeDao.updateRechargeStatus(shopRecharge)>0){
            if(RechargeStatusEnum.CASH_STATUS_REJECT.getStatus().equals(shopRecharge.getRechargeStatus())){
                shopAccountRecordService.updateRecordStatus(shopRecharge.getRechargeNo(), AccountRecordStatusEnum.ACCOUNT_RECORD_STATUS_FAIL.getCode());
            }
            return ReturnVo.returnSuccess();
        }else{
            return ReturnVo.returnFail();
        }
    }

    @Override
    public ReturnVo updateBankInfo(ShopRecharge shopRecharge) {
        if(shopRechargeDao.updateBankInfo(shopRecharge)>0){
            return ReturnVo.returnSuccess();
        }else{
            return ReturnVo.returnFail();
        }
    }
}
