package com.cloud.merchant.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloud.agent.provider.AgentUserProvider;
import com.cloud.finance.common.enums.SysPaymentTypeEnum;
import com.cloud.merchant.common.dto.*;
import com.cloud.merchant.common.enums.MerchantTypeEnum;
import com.cloud.merchant.common.utils.EncodeUtils;
import com.cloud.merchant.dao.MerchantPayChannelDao;
import com.cloud.merchant.dao.MerchantUserDao;
import com.cloud.merchant.dao.SysUserBankDao;
import com.cloud.merchant.po.MerchantPayChannel;
import com.cloud.merchant.po.MerchantUser;
import com.cloud.merchant.po.SysUserBank;
import com.cloud.merchant.service.MerchantUserService;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.enums.DictEnum;
import com.cloud.sysconf.common.enums.RoleTypeEnum;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.redis.lockutil.DistributedLockHandler;
import com.cloud.sysconf.common.redis.lockutil.Lock;
import com.cloud.sysconf.common.utils.*;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.utils.page.PageResult;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysuser.common.DTO.SysUserInfoDto;
import com.cloud.sysuser.common.DTO.SysUserProviderDto;
import com.cloud.sysuser.provider.SysUserProvider;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @Auther Toney
 * @Date 2018/7/29 17:52
 * @Description:
 */
@Service
public class MerchantUserServiceImpl extends BaseMybatisServiceImpl<MerchantUser, String, MerchantUserDao> implements MerchantUserService {

    @Autowired
    private MerchantUserDao merchantUserDao;
    @Autowired
    private SysUserProvider sysUserProvider;
    @Autowired
    private MerchantPayChannelDao merchantPayChannelDao;
    @Autowired
    private DistributedLockHandler distributedLockHandler;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private AgentUserProvider agentUserProvider;
    @Autowired
    private SysUserBankDao sysUserBankDao;

    @Override
    @Transactional(rollbackFor=Exception.class)
    public ReturnVo addMerchant(MerchantFormDto merchantFormDto, HeaderInfoDto headerInfoDto) {

        ReturnVo returnVo = new ReturnVo();
        SysUserProviderDto sysUserProviderDto = new SysUserProviderDto();
        try{
            //分布式锁
            Lock lock = new Lock("lock_add_or_update_merchant_user", DateUtil.DateToString(new Date(),DateUtil.DATE_PATTERN_01));
            if(distributedLockHandler.tryLock(lock)) {
                //1，新增管理账号
                sysUserProviderDto.setLoginName(merchantFormDto.getLoginName());
                sysUserProviderDto.setName(merchantFormDto.getMerchantName());
                sysUserProviderDto.setPassword(merchantFormDto.getPassword());
                sysUserProviderDto.setPanId(headerInfoDto.getPanId());
                sysUserProviderDto.setOptUser(headerInfoDto.getCurUserId());
                sysUserProviderDto.setRoleType(RoleTypeEnum.ROLE_DEFAULT_MERCHANT.getCode());
                ApiResponse apiResponse = sysUserProvider.addNewUser(sysUserProviderDto);

                if(apiResponse == null || !apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode()+"")){
                    returnVo.code = ReturnVo.FAIL;
                    returnVo.responseCode = ResponseCode.getByCode(apiResponse.getCode());
                    return returnVo;
                }
                Map<String, String> map = (Map<String, String>) apiResponse.getData();

                //2，保存商户
                String dailyLimit = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, DictEnum.DAILY_LIMIT.getCode());
                MerchantUser merchantUser = new MerchantUser();
                merchantUser.setDailyLimit(Double.parseDouble(dailyLimit));

                BeanUtils.copyProperties(merchantFormDto, merchantUser);
                if(StringUtils.isNotBlank(merchantUser.getMerchantType())){
                    String code = merchantUser.getMerchantType();
                    String type = MerchantTypeEnum.getMerchantNameByCode(code);
                    merchantUser.setRemarks(type);
                }
                String merchantCode = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_17)+StringUtil.getRandom(3);
                String md5Source = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_17)+StringUtil.getRandom(3);
                String md5Key = EncodeUtils.md5Encode(md5Source);
                merchantUser.setMerchantCode(merchantCode);
                merchantUser.setMd5Key(md5Key);
                merchantUser.setMd5Source(md5Source);
                merchantUser.setDailyLimit((double) 1000000);
                merchantUser.setThirdChannels("999");

                merchantUser.initStatus();
                if (map.get("id") != null) {
                    merchantUser.setSysUserId(map.get("id"));
                    sysUserProviderDto.setId(map.get("id"));
                }

                merchantUser.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                merchantUserDao.add(merchantUser);

                //3，保存支付通道费率
                if (merchantFormDto.getChannelRates() != null) {

                    List<PayChannelRateDto> channelRateDtos = merchantFormDto.getChannelRates();

                    for (PayChannelRateDto channelRate : channelRateDtos) {
                        MerchantPayChannel merchantPayChannel = new MerchantPayChannel();
                        merchantPayChannel.setAgentRate(channelRate.getRate());
                        merchantPayChannel.setMerchantUser(merchantUser.getId());
                        merchantPayChannel.setChannelCode(channelRate.getCode());
                        merchantPayChannel.setUsable(1);
                        merchantPayChannel.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());

                        merchantPayChannelDao.add(merchantPayChannel);
                    }
                }

                // 通过商户名获取商户详情
                MerchantUser merchantUser1 = merchantUserDao.getByName(merchantFormDto.getMerchantName());

                // 添加银行卡
                SysUserBank sysUserBank = new SysUserBank();
                sysUserBank.setSysUserId(merchantUser1.getSysUserId());
                sysUserBank.setBankName(merchantUser1.getBankName());
                sysUserBank.setBankBranchName(merchantUser1.getBankBranchName());
                sysUserBank.setBankCardHolder(merchantUser1.getBankCardHolder());
                sysUserBank.setBankCardNo(merchantUser1.getBankCardNo());
                sysUserBank.setBankProvince(merchantUser1.getBankProvince());
                sysUserBank.setBankCity(merchantUser1.getBankCity());
                sysUserBank.setCardStatus(1);
                sysUserBank.initData();
                sysUserBank.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                sysUserBankDao.add(sysUserBank);

                returnVo.code = ReturnVo.SUCCESS;
                returnVo.responseCode = ResponseCode.Base.SUCCESS;
            }
        }catch (Exception e){
            e.printStackTrace();

            //添加失败撤销新增的管理账号
            sysUserProvider.addNewUserCancel(sysUserProviderDto);

            returnVo.code = ReturnVo.ERROR;
            returnVo.responseCode = ResponseCode.Base.API_ERR;

            throw e;
        }
        return returnVo;
    }

    @Override
    public ReturnVo listForPage(PageQuery pageQuery, HeaderInfoDto headerInfoDto) {

        ReturnVo returnVo = new ReturnVo();
        try {
            List<MerchantUser> list = this.queryForPage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            List<MerchantUserListDto> merchantList = this.initSysUserInfo(list);

            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object= JSONArray.toJSON(merchantList);
        }catch (Exception e){
            returnVo.code = ReturnVo.ERROR;
            returnVo.responseCode = ResponseCode.Base.ERROR;
        }
        return returnVo;
    }

    @Override
    public ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto) {

        ReturnVo returnVo = new ReturnVo();
        try {
            PageResult pageResult = this.queryForTablePage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            List<MerchantUserListDto> merchantList = initSysUserInfo(pageResult.getData());

            pageResult.setData(merchantList);
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(pageResult);
        }catch (Exception e){
            returnVo.code = ReturnVo.ERROR;
            returnVo.responseCode = ResponseCode.Base.ERROR;
        }
        return returnVo;
    }

    /**
     * 初始化商户的管理账号信息
     * @param merchantUserList
     * @return
     */
    private List<MerchantUserListDto> initSysUserInfo(List<MerchantUser> merchantUserList){
        List<MerchantUserListDto> merchantList = new ArrayList<>();
        for (MerchantUser merchant : merchantUserList) {
            MerchantUserListDto merchantUserListDto = new MerchantUserListDto();
            BeanUtils.copyProperties(merchant, merchantUserListDto);

            ApiResponse apiResponse = sysUserProvider.userInfo(merchant.getSysUserId());
            Map<String, Object> map = (Map<String, Object>) apiResponse.getData();
            SysUserInfoDto sysUserInfoDto = new SysUserInfoDto();
            MyBeanUtil.transMap2Bean(map, sysUserInfoDto);
            merchantUserListDto.setSysUserLoginName(sysUserInfoDto.getLoginName());
            merchantUserListDto.setSysUserName(sysUserInfoDto.getName());

            merchantList.add(merchantUserListDto);
        }
        return merchantList;
    }

    @Override
    public ReturnVo delete(String id, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        MerchantUser merchantUser = merchantUserDao.getById(id);
        if(MerchantUser.DEL_FLAG_COMMON.equals(merchantUser.getDelFlag())){
            merchantUser.setDelFlag(MerchantUser.DEL_FLAG_ALREADY);
            merchantUser.preUpdate(headerInfoDto.getCurUserId());

            if(merchantUserDao.delMerchant(merchantUser) > 0){
                ApiResponse apiResponse = sysUserProvider.deleteUser(merchantUser.getSysUserId(), headerInfoDto.getCurUserId());
                if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())) {
                    returnVo.code = ReturnVo.SUCCESS;
                }else{
                    merchantUser.setDelFlag(MerchantUser.DEL_FLAG_COMMON);
                    merchantUserDao.delMerchant(merchantUser);
                }
            }else{
                returnVo.code = ReturnVo.SUCCESS;
                returnVo.responseCode = ResponseCode.UNINTENDED_RESULT.NO_DATA_UPDATE;
            }

            returnVo.code = ReturnVo.SUCCESS;
        }
        return returnVo;
    }

    @Override
    public ReturnVo detail(String id) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        MerchantUser merchantUser = merchantUserDao.getById(id);
        if(merchantUser != null && MerchantUser.DEL_FLAG_COMMON.equals(merchantUser.getDelFlag())){
            MerchantInfoDto merchantInfoDto = new MerchantInfoDto();
            BeanUtils.copyProperties(merchantUser, merchantInfoDto);

            List<MerchantPayChannel> channels = merchantPayChannelDao.channelRates(merchantUser.getId(), null);
            List<PayChannelRateDto> payChannels = new ArrayList<>();
            if(channels != null && channels.size()>0){
                for (MerchantPayChannel mc: channels
                     ) {
                    PayChannelRateDto payChannelRateDto = new PayChannelRateDto();
                    payChannelRateDto.setCode(mc.getChannelCode());
                    payChannelRateDto.setRate(mc.getAgentRate());
                    payChannelRateDto.setUsable(mc.getUsable());
                    payChannels.add(payChannelRateDto);
                }
                merchantInfoDto.setChannels(payChannels);
            }
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(merchantInfoDto);
        }else{
            returnVo.responseCode = new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(),
                    "商户不存在或已被删除",
                    "Merchant does not exist or has been deleted");
        }

        return returnVo;
    }

    @Override
    public ReturnVo detailByUserId(String sysUserId) {

        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        MerchantUser merchantUser = merchantUserDao.getByUserId(sysUserId);
        if(merchantUser != null && MerchantUser.DEL_FLAG_COMMON.equals(merchantUser.getDelFlag())){
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(merchantUser);
        }else{
            returnVo.responseCode = new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(),
                    "商户不存在或已被删除",
                    "Merchant does not exist or has been deleted");
        }
        return returnVo;
    }

    @Override
    public ReturnVo optStatus(String id, Integer optStatus, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        MerchantUser merchantUser = merchantUserDao.getById(id);
        if(MerchantUser.OPT_STATUS_COMMON == optStatus || MerchantUser.OPT_STATUS_FROZEN == optStatus){
            merchantUser.setOptStatus(optStatus);
            merchantUser.preUpdate(headerInfoDto.getCurUserId());
            merchantUserDao.updateStatus(merchantUser);

            returnVo.code = ReturnVo.SUCCESS;
        }else{
            returnVo.responseCode = ResponseCode.Parameter.ILLEGAL;
        }

        return returnVo;
    }

    @Override
    public ReturnVo cashStatus(String id, Integer cashStatus, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        MerchantUser merchantUser = merchantUserDao.getById(id);
        if(MerchantUser.CASH_STATUS_COMMON == cashStatus || MerchantUser.CASH_STATUS_FROZEN == cashStatus){
            merchantUser.setCashStatus(cashStatus);
            merchantUser.preUpdate(headerInfoDto.getCurUserId());
            merchantUserDao.updateStatus(merchantUser);

            returnVo.code = ReturnVo.SUCCESS;
        }else{
            returnVo.responseCode = ResponseCode.Parameter.ILLEGAL;
        }

        return returnVo;
    }

    @Override
    public ReturnVo initMerchantToRedis(String merchantCode) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        MerchantUser merchantUser = merchantUserDao.getByCode(merchantCode);
        if(merchantUser != null){
            RedisMerchantInfoDto merchantInfoDto = new RedisMerchantInfoDto();
            BeanUtils.copyProperties(merchantUser, merchantInfoDto);

            ApiResponse apiResponse = sysUserProvider.userInfo(merchantUser.getSysUserId());
            logger.info("[get sysUserInfo result] " + JSONObject.toJSONString(apiResponse));
            if(apiResponse != null && apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode()+"")){
                Map<String, Object> respMap = (Map<String, Object>) apiResponse.getData();
                if(respMap.get("loginName") != null){
                    merchantInfoDto.setSysUserLoginName(respMap.get("loginName").toString());
                }
                if(respMap.get("name") != null){
                    merchantInfoDto.setSysUserName(respMap.get("name").toString());
                }
            }

            String channels = merchantPayChannelDao.getChannelsToStr(merchantUser.getId());

            if(StringUtils.isNotBlank(channels))
                merchantInfoDto.setActivePayChannels(channels);

            MerchantPayChannel aliH5Jump = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.ALI_H5_JUMP.getValue());
            if(aliH5Jump != null)
                merchantInfoDto.setAli_h5_wake(aliH5Jump.getAgentRate());

            MerchantPayChannel aliQrCode = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.ALI_QR_CODE.getValue());
            if(aliQrCode != null)
                merchantInfoDto.setAli_qrcode(aliQrCode.getAgentRate());

            MerchantPayChannel aliSelfPay = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.ALI_SELF_PAY.getValue());
            if(aliSelfPay != null)
                merchantInfoDto.setAli_self_wap(aliSelfPay.getAgentRate());

            MerchantPayChannel qqH5Wake = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.QQ_H5_JUMP.getValue());
            if(qqH5Wake != null)
                merchantInfoDto.setQq_h5_wake(qqH5Wake.getAgentRate());

            MerchantPayChannel qqQrCode = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.QQ_QR_CODE.getValue());
            if(qqQrCode != null)
                merchantInfoDto.setQq_qrcode(qqQrCode.getAgentRate());

            MerchantPayChannel qqSelfWap = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.QQ_SELF_PAY.getValue());
            if(qqSelfWap != null)
                merchantInfoDto.setQq_self_wap(qqSelfWap.getAgentRate());

            MerchantPayChannel jdH5Jump = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.JD_H5_JUMP.getValue());
            if(jdH5Jump != null)
                merchantInfoDto.setJd_h5_wake(jdH5Jump.getAgentRate());

            MerchantPayChannel jdQrCode = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.JD_QR_CODE.getValue());
            if(jdQrCode != null)
                merchantInfoDto.setJd_qrcode(jdQrCode.getAgentRate());

            MerchantPayChannel jdSelfPay = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.JD_SELF_PAY.getValue());
            if(jdSelfPay != null)
                merchantInfoDto.setJd_self_wap(jdSelfPay.getAgentRate());

            MerchantPayChannel gateH5 = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.GATE_H5.getValue());
            if(gateH5 != null)
                merchantInfoDto.setGate_h5(gateH5.getAgentRate());

            MerchantPayChannel gateQrCode = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.GATE_QR_CODE.getValue());
            if(gateQrCode != null)
                merchantInfoDto.setGate_qrcode(gateQrCode.getAgentRate());

            MerchantPayChannel gateWebDirect = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.GATE_WEB_DIRECT.getValue());
            if(gateWebDirect != null)
                merchantInfoDto.setGate_web_direct(gateWebDirect.getAgentRate());

            MerchantPayChannel gateWebSyt = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.GATE_WEB_SYT.getValue());
            if(gateWebSyt != null)
                merchantInfoDto.setGate_web_syt(gateWebSyt.getAgentRate());

            MerchantPayChannel sytAllIn = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.SYT_ALL_IN.getValue());
            if(sytAllIn != null)
                merchantInfoDto.setSyt_all_in(sytAllIn.getAgentRate());

            MerchantPayChannel wxH5Jump = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.WX_H5_JUMP.getValue());
            if(wxH5Jump != null)
                merchantInfoDto.setWx_h5_wake(wxH5Jump.getAgentRate());

            MerchantPayChannel wxQrCode = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.WX_QR_CODE.getValue());
            if(wxQrCode != null)
                merchantInfoDto.setWx_qrcode(wxQrCode.getAgentRate());

            MerchantPayChannel wxSelfPay = merchantPayChannelDao.channelRate(merchantUser.getId(), SysPaymentTypeEnum.WX_SELF_PAY.getValue());
            if(wxSelfPay != null)
                merchantInfoDto.setWx_self_wap(wxSelfPay.getAgentRate());

            ApiResponse apiResponse1 = agentUserProvider.detailById(merchantUser.getAgentId());
            if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse1.getCode())){
                Map<String, Object> agent = (Map<String, Object>) apiResponse1.getData();
                merchantInfoDto.setAgentCode(agent.get("agentCode").toString());
            }

            redisClient.SetHsetJedis(RedisConfig.MERCHANT_INFO_DB, merchantCode, MyBeanUtil.transBean2Map2(merchantInfoDto));
            logger.info("set redis info from db "+ RedisConfig.MERCHANT_INFO_DB);
            returnVo.code = ReturnVo.SUCCESS;
        }else{
            returnVo.responseCode = new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), "商户不存在");
        }

        return returnVo;
    }

    @Override
    public ReturnVo getByCode(String code) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        MerchantUser merchantUser = merchantUserDao.getByCode(code);
        if(MerchantUser.DEL_FLAG_COMMON.equals(merchantUser.getDelFlag())){
            MerchantInfoDto merchantInfoDto = new MerchantInfoDto();
            BeanUtils.copyProperties(merchantUser, merchantInfoDto);
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(merchantUser);
        }else{
            returnVo.responseCode = new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(),
                    "商户不存在或已被删除");
        }

        return returnVo;
    }

    @Override
    public ReturnVo channelRate(String merchantUser, String channelCode) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        MerchantPayChannel merchantPayChannel = merchantPayChannelDao.channelRate(merchantUser, channelCode);
        if(MerchantPayChannel.DEL_FLAG_COMMON.equals(merchantPayChannel.getDelFlag())
                || MerchantPayChannel.USABLE_YES == merchantPayChannel.getUsable()){
            MerchantChannelDto merchantInfoDto = new MerchantChannelDto();
            BeanUtils.copyProperties(merchantPayChannel, merchantInfoDto);
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(merchantUser);
        }else{
            returnVo.responseCode = new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(),
                    "该商户未配置此通道或不可用");
        }

        return returnVo;
    }

    @Override
    public ReturnVo payStatus(String id, Integer payStatus, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        MerchantUser merchantUser = merchantUserDao.getById(id);
        if(MerchantUser.PAY_STATUS_COMMON == payStatus || MerchantUser.PAY_STATUS_FROZEN == payStatus){
            merchantUser.setPayStatus(payStatus);
            merchantUser.preUpdate(headerInfoDto.getCurUserId());
            merchantUserDao.updateStatus(merchantUser);

            returnVo.code = ReturnVo.SUCCESS;
        }else{
            returnVo.responseCode = ResponseCode.Parameter.ILLEGAL;
        }

        return returnVo;
    }

    @Override
    public ReturnVo updateMerchant(MerchantUpdateFormDto merchantUpdateFormDto, HeaderInfoDto headerInfoDto) {
        try {
            MerchantUser merchantUser = new MerchantUser();
            BeanUtils.copyProperties(merchantUpdateFormDto, merchantUser);
            merchantUser.preUpdate(headerInfoDto.getCurUserId());
            merchantUserDao.update(merchantUser);
            return ReturnVo.returnSuccess();
        }catch (Exception e){
            return ReturnVo.returnSuccess();
        }
    }

    @Override
    public ReturnVo updateChannelRate(String merchantUser, String channelCode, Double agentRate, Integer usable, HeaderInfoDto headerInfoDto) {
        try {
            merchantPayChannelDao.updateChannelRate(merchantUser, channelCode, agentRate, usable, headerInfoDto.getCurUserId(), new Date());
            return ReturnVo.returnSuccess();
        }catch (Exception e){
            return ReturnVo.returnSuccess();
        }
    }
}
