package com.cloud.merchant.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.provider.FinanceProvider;
import com.cloud.merchant.common.dto.SysUserBankDto;
import com.cloud.merchant.dao.MerchantUserDao;
import com.cloud.merchant.dao.SysUserBankDao;
import com.cloud.merchant.po.MerchantUser;
import com.cloud.merchant.po.SysUserBank;
import com.cloud.merchant.service.CardBlackListService;
import com.cloud.merchant.service.SysUserBankService;
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
import com.cloud.sysconf.common.vo.ReturnVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SysUserBankImpl extends BaseMybatisServiceImpl<SysUserBank, String, SysUserBankDao> implements SysUserBankService {

    @Autowired
    private SysUserBankDao sysUserBankDao;
    @Autowired
    private FinanceProvider financeProvider;
    @Autowired
    private MerchantUserDao merchantUserDao;
    @Autowired
    private CardBlackListService cardBlackListService;
    @Autowired
    private RedisClient redisClient;

    @Override
    public ReturnVo addBankCard(SysUserBankDto sysUserBankDto, HeaderInfoDto headerInfoDto) {

        ReturnVo returnVo = new ReturnVo();
        try {
            SysUserBank sysUserBank = new SysUserBank();
            logger.info("开户人：" + sysUserBankDto.getBankCardHolder());
            // 检查卡号是否为空
            if (StringUtil.isEmpty(sysUserBankDto.getBankCardNo())) {
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.bankcard.BANKCARD_NOT_EXIST;
                return returnVo;
            }
            // 检查持卡人是否在黑名单
            // false表示黑名单
            if (cardBlackListService.checkCardSafe(sysUserBankDto.getBankCardHolder())) {
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.bankcard.BANKCARD_IN_BLACKLIST;
                return returnVo;
            }
            // 检查银行卡是否已绑定
            if (sysUserBankDao.getByCardNo(headerInfoDto.getCurUserId(), sysUserBankDto.getBankCardNo()) != null) {
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.bankcard.BANKCARD_EXIST;
                return returnVo;
            }
            BeanUtils.copyProperties(sysUserBankDto, sysUserBank);

            // 商户系统绑定
            if (HeaderInfoDto.AUTH_MERCHANT_SYSTEM.equals(headerInfoDto.getAuth())) {
                sysUserBank.setSysUserId(headerInfoDto.getCurUserId());
                sysUserBank.preInsert(sysUserBank.getSysUserId(), headerInfoDto.getPanId());
            }
            // 每个商户限制绑定银行卡数量
            if (checkCardNum(sysUserBank.getSysUserId())) {
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.bankcard.BANKCARD_NUM_PASS;
                return returnVo;
            }
            sysUserBank.setCardStatus(0);
            sysUserBank.initData();
            sysUserBankDao.add(sysUserBank);

            returnVo.code = ReturnVo.SUCCESS;
            returnVo.responseCode = ResponseCode.Base.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            returnVo.code = ReturnVo.ERROR;
            returnVo.responseCode = ResponseCode.Base.API_ERR;
        }
        return returnVo;
    }

    @Override
    public ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto) {
        try {
            Map<String, Object> params = pageQuery.getParams();
            Iterator keys = params.keySet().iterator();
            String key;
            while (keys.hasNext()) {
                key = (String) keys.next();
                if (key.equals("merchantName")) {
                    logger.info("根据商户编号查询");
                    MerchantUser merchantUser = merchantUserDao.getByName(params.get("merchantName").toString());
                    params.put("sysUserId", merchantUser.getSysUserId());
                    pageQuery.setParams(params);
                }
            }
            PageResult pageResult = this.queryForTablePage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            return ReturnVo.returnSuccess(ResponseCode.Base.SUCCESS, JSONObject.toJSON(pageResult));
        }catch (Exception e){
            e.printStackTrace();
            return ReturnVo.returnFail();
        }
    }

    @Override
    public ReturnVo getByCardNo(String userId, String bankCardNo) {

        ReturnVo returnVo = new ReturnVo();
        try {
            returnVo.object = sysUserBankDao.getByCardNo(userId, bankCardNo);
            returnVo.code = ReturnVo.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            returnVo.code = ReturnVo.ERROR;
        }
        return returnVo;
    }

    @Override
    public void summaryPaid(String userId) {

        // 查询所有银行卡
        List<SysUserBank> list = sysUserBankDao.getByUserId(userId);
        // 遍历银行卡列表
        String today = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_02);
        logger.info("统计时间：" + today);
        for (SysUserBank sysUserBank : list) {
            // 统计银行卡号的下发数据（查shop_recharge表）
            if (StringUtil.isEmpty(sysUserBank.getBankCardNo())) {
                continue;
            }
            ApiResponse response = financeProvider.summaryPaid(sysUserBank.getSysUserId(), sysUserBank.getBankCardNo(), today);
            if (response.getCode().equals(ResponseCode.Base.SUCCESS.getCode() + "")) {
                Map<String, Object> map = (Map<String, Object>) response.getData();
                logger.info("请求结果：" + map);
                sysUserBank.setDailyMoney((Double) map.get("dailyMoney"));
                sysUserBank.setTotalMoney((Double) map.get("totalMoney"));
                sysUserBank.setDailyRecharge((Integer) map.get("dailyRecharge"));
                sysUserBank.setTotalRecharge((Integer) map.get("totalRecharge"));
                sysUserBank.setUpdateBy(userId);

                sysUserBankDao.updateInfo(sysUserBank);
            }
        }
    }

    private boolean checkCardNum(String sysUserId) {

        boolean isPass = false;
        try {
            PageQuery pageQuery = new PageQuery();
            Map<String, Object> params = new HashMap<>();
            params.put("sysUserId", sysUserId);
            pageQuery.setParams(params);
            PageResult pageResult = this.queryForTablePage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            int cardNum = pageResult.getData().size();
            String sysConf = redisClient.Gethget(RedisConfig.THIRD_PAY_CHANNEL, "sys_dict", "CARD_NUM");
            logger.info("redis银行卡数量限制：" + sysConf);
            if (cardNum > Integer.parseInt(sysConf)) {
                isPass = true;
            }
            return isPass;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}
