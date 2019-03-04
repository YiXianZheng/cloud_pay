package com.cloud.finance.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cloud.agent.provider.AgentUserProvider;
import com.cloud.finance.common.dto.AccountDto;
import com.cloud.finance.common.dto.AccountInfoDto;
import com.cloud.finance.common.dto.RedisFinanceDto;
import com.cloud.finance.common.dto.UpdateSecurityCode;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.dao.ShopAccountDao;
import com.cloud.finance.dao.ShopPayFrozenDao;
import com.cloud.finance.po.ShopAccount;
import com.cloud.finance.service.FinanceService;
import com.cloud.finance.service.ShopAccountService;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.utils.Util;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @Auther Toney
 * @Date 2018/7/29 17:52
 * @Description:
 */
@Service
public class ShopAccountServiceImpl extends BaseMybatisServiceImpl<ShopAccount, String, ShopAccountDao> implements ShopAccountService {

    @Autowired
    private FinanceService financeService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private ShopAccountDao shopAccountDao;
    @Autowired
    private MerchantUserProvider merchantUserProvider;
    @Autowired
    private AgentUserProvider agentUserProvider;
    @Autowired
    private ShopPayFrozenDao shopPayFrozenDao;

    @Override
    public ShopAccount getByUserId(String sysUserId) {
        return shopAccountDao.getByUserId(sysUserId);
    }


    @Override
    public ShopAccount initAccount(HeaderInfoDto headerInfoDto) {
        Double frozenMoney = 0D;
        String userCode=null;
        Integer type = null;
        if(StringUtils.isNotBlank(headerInfoDto.getAgentUser())){
            frozenMoney = shopPayFrozenDao.countFrozen(null, headerInfoDto.getAgentUser());
            ApiResponse apiResponse = agentUserProvider.detailById(headerInfoDto.getAgentUser());
            if (apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())) {
                Map agentInfo = Util.toHashMap((LinkedHashMap) apiResponse.getData());
                userCode = agentInfo.get("agentCode").toString();
            } else {
                return null;
            }
            type = 2;
        }else if(StringUtils.isNotBlank(headerInfoDto.getMerchantUser())){
            frozenMoney = shopPayFrozenDao.countFrozen(headerInfoDto.getMerchantUser(), null);
            ApiResponse apiResponse = merchantUserProvider.detailById(headerInfoDto.getMerchantUser());
            if(apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())){
                Map merchantInfo = Util.toHashMap((LinkedHashMap) apiResponse.getData());
                userCode = merchantInfo.get("merchantCode").toString();
            }else{
                return null;
            }
            type = 3;
        }
        financeService.initOverview(type, userCode);

        Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, userCode);
        RedisFinanceDto redisFinanceDto = RedisFinanceDto.map2Object(map);
        Double totalCharge = SafeComputeUtils.add(redisFinanceDto.getHistoryTotalCharge(),
                SafeComputeUtils.add(redisFinanceDto.getTotalCharge(),redisFinanceDto.getDailyTotalCharge()));

        ShopAccount shopAccount = new ShopAccount();
        shopAccount.setSysUserId(headerInfoDto.getCurUserId());
        shopAccount.setTotalMoney(totalCharge);
        shopAccount.setSecurityCode(StringUtil.getRandom(6));
        shopAccount.setStatus(ShopAccount.STATUS_COMMON);

        AccountDto accountDto = shopRechargeService.countTotalRecharge(headerInfoDto.getCurUserId());

        shopAccount.setUsableMoney(SafeComputeUtils.sub(SafeComputeUtils.sub(
                SafeComputeUtils.sub(totalCharge, accountDto.getAmount()), accountDto.getFrozenAmount()),frozenMoney));
        shopAccount.setFrozenMoney(SafeComputeUtils.add(accountDto.getFrozenAmount(), frozenMoney));
        shopAccount.setRechargeMoney(accountDto.getAmount());

        shopAccountDao.add(shopAccount);

        return shopAccount;
    }

    @Override
    public int frozenAccount(String userId, Double frozenMoney) {
        return shopAccountDao.frozenAccount(userId, frozenMoney);
    }

    @Override
    public int updateRecharge(String sysUserId, Double rechargeMoney) {

        return shopAccountDao.updateRecharge(sysUserId, rechargeMoney);
    }

    @Transactional
    @Override
    public ReturnVo getAccount(HeaderInfoDto headerInfoDto) {
        try {
            AccountInfoDto accountInfoDto = shopAccountDao.getAccount(headerInfoDto.getCurUserId());
            if(accountInfoDto==null){
                this.initAccount(headerInfoDto);
            }else{
                Double frozenMoney = 0D;

                String userCode=null;
                Integer type = null;
                if(StringUtils.isNotBlank(headerInfoDto.getAgentUser())){
                    frozenMoney = shopPayFrozenDao.countFrozen(null, headerInfoDto.getAgentUser());
                    ApiResponse apiResponse = agentUserProvider.detailById(headerInfoDto.getAgentUser());
                    if (apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())) {
                        Map agentInfo = Util.toHashMap((LinkedHashMap) apiResponse.getData());
                        userCode = agentInfo.get("agentCode").toString();
                    } else {
                        return null;
                    }
                    type = 2;
                }else if(StringUtils.isNotBlank(headerInfoDto.getMerchantUser())){
                    frozenMoney = shopPayFrozenDao.countFrozen(headerInfoDto.getMerchantUser(), null);
                    ApiResponse apiResponse = merchantUserProvider.detailById(headerInfoDto.getMerchantUser());
                    if(apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())){
                        Map merchantInfo = Util.toHashMap((LinkedHashMap) apiResponse.getData());
                        userCode = merchantInfo.get("merchantCode").toString();
                    }else{
                        return null;
                    }
                    type = 3;
                }

                financeService.initOverview(type, userCode);

                Map<String, String> map = redisClient.Gethgetall(RedisConfig.ORDER_COUNT_DB, userCode);
                RedisFinanceDto redisFinanceDto = RedisFinanceDto.map2Object(map);
                Double totalCharge = SafeComputeUtils.add(redisFinanceDto.getHistoryTotalCharge(),
                        SafeComputeUtils.add(redisFinanceDto.getTotalCharge(),redisFinanceDto.getDailyTotalCharge()));

                ShopAccount shopAccount = shopAccountDao.getByUserId(headerInfoDto.getCurUserId());

                AccountDto accountDto = shopRechargeService.countTotalRecharge(headerInfoDto.getCurUserId());

                shopAccount.setUsableMoney(SafeComputeUtils.sub(SafeComputeUtils.sub(
                        SafeComputeUtils.sub(totalCharge, accountDto.getAmount()), accountDto.getFrozenAmount()),frozenMoney));

                shopAccount.setFrozenMoney(SafeComputeUtils.add(accountDto.getFrozenAmount(), frozenMoney));
                shopAccount.setRechargeMoney(accountDto.getAmount());
                shopAccount.setTotalMoney(SafeComputeUtils.sub(totalCharge, accountDto.getAmount()));
                shopAccountDao.updateAccountInfo(shopAccount);

                accountInfoDto = shopAccountDao.getAccount(headerInfoDto.getCurUserId());
            }
            return ReturnVo.returnSuccess(JSONObject.toJSON(accountInfoDto));
        }catch (Exception e){
            return ReturnVo.returnError(ResponseCode.Base.ERROR);
        }
    }

    @Override
    public ShopAccount getAccountByMerchantCode(String merchangCode) {
        return shopAccountDao.getAccountByMerchantCode(merchangCode);
    }

    @Override
    public void updateAccountInfo(ShopAccount shopAccount) {
        shopAccountDao.updateAccountInfo(shopAccount);
    }

    @Override
    public ReturnVo updateSecurityCode(UpdateSecurityCode securityCode, String curUserId) {
        ReturnVo returnVo = new ReturnVo();
        // 通过用户id获取ShopAccount
        ShopAccount account = shopAccountDao.getByUserId(curUserId);
        // 验证旧安全码是否正确
        if (account.getSecurityCode() == null || !securityCode.getOldCode().equals(account.getSecurityCode())) {
            returnVo.code = ReturnVo.FAIL;
            returnVo.responseCode = ResponseCode.SecurityCode.OLD_CODE_ERR;
            return returnVo;
        }
        for (int i = 0; i < securityCode.getNewCode().length(); i++) {
            if (!Character.isDigit(securityCode.getNewCode().charAt(i))) {
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.SecurityCode.CODE_FORMAT_ERR;
                return returnVo;
            }
        }
        if (!securityCode.getNewCode().equals(securityCode.getReNewCode())) {
            returnVo.code = ReturnVo.FAIL;
            returnVo.responseCode = ResponseCode.SecurityCode.NEW_CODE_ERR;
            return returnVo;
        }
        if (securityCode.getNewCode().length() != 6) {
            returnVo.code = ReturnVo.FAIL;
            returnVo.responseCode = ResponseCode.SecurityCode.CODE_LENGTH_ERR;
            return returnVo;
        }

        shopAccountDao.updateSecurityCode(curUserId, securityCode.getNewCode());

        returnVo.code = ReturnVo.SUCCESS;
        return returnVo;
    }
}
