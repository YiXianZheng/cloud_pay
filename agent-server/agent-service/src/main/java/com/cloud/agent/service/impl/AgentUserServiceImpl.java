package com.cloud.agent.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cloud.agent.common.dto.*;
import com.cloud.agent.common.enums.AgentTypeEnum;
import com.cloud.agent.dao.AgentPayChannelDao;
import com.cloud.agent.dao.AgentUserDao;
import com.cloud.agent.po.AgentPayChannel;
import com.cloud.agent.po.AgentUser;
import com.cloud.agent.service.AgentUserService;
import com.cloud.agent.utils.AgentUtil;
import com.cloud.finance.common.enums.SysPaymentTypeEnum;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.enums.RoleTypeEnum;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.redis.lockutil.DistributedLockHandler;
import com.cloud.sysconf.common.redis.lockutil.Lock;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.MyBeanUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
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

import java.util.*;


/**
 * @Auther Toney
 * @Date 2018/7/29 17:52
 * @Description:
 */
@Service
public class AgentUserServiceImpl extends BaseMybatisServiceImpl<AgentUser, String, AgentUserDao> implements AgentUserService {

    @Autowired
    private AgentUserDao agentUserDao;
    @Autowired
    private SysUserProvider sysUserProvider;
    @Autowired
    private AgentPayChannelDao agentPayChannelDao;
    @Autowired
    private DistributedLockHandler distributedLockHandler;
    @Autowired
    private RedisClient redisClient;
    private RedisAgentInfoDto agentInfoDto;

    @Override
    @Transactional(rollbackFor=Exception.class)
    public ReturnVo addOrUpdate(AgentFormDto agentFormDto, HeaderInfoDto headerInfoDto) {

        ReturnVo returnVo = new ReturnVo();
        SysUserProviderDto sysUserProviderDto = new SysUserProviderDto();
        try{
            //分布式锁
            Lock lock = new Lock("lock_add_or_update_agent_user", DateUtil.DateToString(new Date(),DateUtil.DATE_PATTERN_01));
            if(distributedLockHandler.tryLock(lock)) {
                //1，新增管理账号
                sysUserProviderDto.setLoginName(agentFormDto.getLoginName());
                sysUserProviderDto.setName(agentFormDto.getAgentName());
                sysUserProviderDto.setOptUser(headerInfoDto.getCurUserId());
                sysUserProviderDto.setPanId(headerInfoDto.getPanId());
                sysUserProviderDto.setRoleType(RoleTypeEnum.ROLE_DEFAULT_AGENT.getCode());
                ApiResponse apiResponse = sysUserProvider.addNewUser(sysUserProviderDto);

                if(apiResponse == null || !apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode()+"")){
                    returnVo.code = ReturnVo.FAIL;
                    returnVo.responseCode = ResponseCode.getByCode(apiResponse.getCode());
                    return returnVo;
                }
                Map<String, String> map = (Map<String, String>) apiResponse.getData();

                //2，保存代理
                AgentUser agentUser = new AgentUser();

                String agentCode = AgentUtil.getAgentCode(redisClient);

                BeanUtils.copyProperties(agentFormDto, agentUser);
                agentUser.setAgentCode(agentCode);
                if(StringUtils.isNotBlank(agentUser.getAgentType())){
                    String code = agentUser.getAgentType();
                    String type = AgentTypeEnum.getAgentNameByCode(code);
                    agentUser.setRemarks(type);
                }
                agentUser.initStatus();
                if (map.get("id") != null) {
                    agentUser.setSysUserId(map.get("id").toString());
                    sysUserProviderDto.setId(map.get("id").toString());
                }

                agentUser.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                agentUserDao.add(agentUser);

                //3，保存支付通道费率
                if (agentFormDto != null && agentFormDto.getChannelRates() != null) {

                    List<PayChannelRateDto> channelRateDtos = agentFormDto.getChannelRates();

                    List<AgentPayChannel> list = new ArrayList<>();
                    for (PayChannelRateDto channelRate : channelRateDtos
                            ) {
                        AgentPayChannel agentPayChannel = new AgentPayChannel();
                        agentPayChannel.setAgentRate(channelRate.getRate());
                        agentPayChannel.setAgentUser(agentUser.getId());
                        agentPayChannel.setChannelCode(channelRate.getCode());
                        agentPayChannel.setUsable(1);
                        agentPayChannel.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());

                        agentPayChannelDao.add(agentPayChannel);
                    }
                }

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
            List<AgentUser> list = this.queryForPage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            List<AgentUserListDto> agentList = this.initSysUserInfo(list);

            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object= JSONArray.toJSON(agentList);
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
            List<AgentUserListDto> agentList = initSysUserInfo(pageResult.getData());

            pageResult.setData(agentList);
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(pageResult);
        }catch (Exception e){
            returnVo.code = ReturnVo.ERROR;
            returnVo.responseCode = ResponseCode.Base.ERROR;
        }
        return returnVo;
    }

    /**
     * 初始化代理商的管理账号信息
     * @param agentUserList
     * @return
     */
    private List<AgentUserListDto> initSysUserInfo(List<AgentUser> agentUserList){
        List<AgentUserListDto> agentList = new ArrayList<>();
        for (AgentUser agent : agentUserList) {
            AgentUserListDto agentUserListDto = new AgentUserListDto();
            BeanUtils.copyProperties(agent, agentUserListDto);

            ApiResponse apiResponse = sysUserProvider.userInfo(agent.getSysUserId());
            Map<String, Object> map = (Map<String, Object>) apiResponse.getData();
            SysUserInfoDto sysUserInfoDto = new SysUserInfoDto();
            MyBeanUtil.transMap2Bean(map, sysUserInfoDto);
            agentUserListDto.setSysUserLoginName(sysUserInfoDto.getLoginName());
            agentUserListDto.setSysUserName(sysUserInfoDto.getName());

            agentList.add(agentUserListDto);
        }
        return agentList;
    }

    @Override
    public ReturnVo delete(String id, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        AgentUser agentUser = agentUserDao.getById(id);
        if(AgentUser.DEL_FLAG_COMMON.equals(agentUser.getDelFlag())){
            agentUser.setDelFlag(AgentUser.DEL_FLAG_ALREADY);
            agentUser.preUpdate(headerInfoDto.getCurUserId());

            if(agentUserDao.delAgent(agentUser) > 0){
                ApiResponse apiResponse = sysUserProvider.deleteUser(agentUser.getSysUserId(), headerInfoDto.getCurUserId());
                if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())) {
                    returnVo.code = ReturnVo.SUCCESS;
                }else{
                    agentUser.setDelFlag(AgentUser.DEL_FLAG_COMMON);
                    agentUserDao.delAgent(agentUser);
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
    @Transactional(rollbackFor=Exception.class)
    public ReturnVo auth(String id, Integer authStatus, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        AgentUser agentUser = agentUserDao.getById(id);
        if (agentUser != null && AgentUser.AUTH_STATUS_WAIT == agentUser.getAuthStatus()) {
            if (AgentUser.AUTH_STATUS_SUCCESS == authStatus || AgentUser.AUTH_STATUS_FAIL == authStatus) {
                agentUser.setAuthStatus(authStatus);
                agentUser.preUpdate(headerInfoDto.getCurUserId());
                if(agentUserDao.updateStatus(agentUser) >0){
                    ApiResponse apiResponse = sysUserProvider.updateLoginFlag(agentUser.getSysUserId(),
                            SysUserProviderDto.LOGIN_FLAG_YES, headerInfoDto.getCurUserId());
                    if((ResponseCode.Base.SUCCESS.getCode()+"").equals(apiResponse.getCode())) {
                        returnVo.code = ReturnVo.SUCCESS;
                    }else{
                        agentUser.setAuthStatus(AgentUser.AUTH_STATUS_WAIT);
                        agentUserDao.updateStatus(agentUser);
                    }
                }else{
                    returnVo.code = ReturnVo.SUCCESS;
                    returnVo.responseCode = ResponseCode.UNINTENDED_RESULT.NO_DATA_UPDATE;
                }

            } else {
                returnVo.responseCode = ResponseCode.Parameter.ILLEGAL;
            }
            returnVo.responseCode = ResponseCode.UNINTENDED_RESULT.UPDATE_OBJECT_UNEXPECT;
        }

        return returnVo;
    }

    @Override
    public ReturnVo detail(String id) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        AgentUser agentUser = agentUserDao.getById(id);
        if(AgentUser.DEL_FLAG_COMMON.equals(agentUser.getDelFlag())){
            AgentInfoDto agentInfoDto = new AgentInfoDto();
            BeanUtils.copyProperties(agentUser, agentInfoDto);

            List<AgentPayChannel> channels = agentPayChannelDao.channelRates(agentUser.getId(), null);
            List<PayChannelRateDto> payChannels = new ArrayList<>();
            if(channels != null && channels.size()>0){
                for (AgentPayChannel mc: channels
                        ) {
                    PayChannelRateDto payChannelRateDto = new PayChannelRateDto();
                    payChannelRateDto.setCode(mc.getChannelCode());
                    payChannelRateDto.setRate(mc.getAgentRate());
                    payChannelRateDto.setUsable(mc.getUsable());
                    payChannels.add(payChannelRateDto);
                }
                agentInfoDto.setChannels(payChannels);
            }
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(agentInfoDto);
        }else{
            returnVo.responseCode = ResponseCode.UNINTENDED_RESULT.AGENT_NOT_EXIST;
        }

        return returnVo;
    }

    @Override
    public ReturnVo optStatus(String id, Integer optStatus, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        AgentUser agentUser = agentUserDao.getById(id);
        if(agentUser != null &&AgentUser.AUTH_STATUS_WAIT == agentUser.getAuthStatus()){
            if(AgentUser.OPT_STATUS_COMMON == optStatus || AgentUser.OPT_STATUS_FROZEN == optStatus){
                agentUser.setOptStatus(optStatus);
                agentUser.preUpdate(headerInfoDto.getCurUserId());
                agentUserDao.updateStatus(agentUser);

                returnVo.code = ReturnVo.SUCCESS;
            }else{
                returnVo.responseCode = ResponseCode.Parameter.ILLEGAL;
            }
            returnVo.responseCode = ResponseCode.UNINTENDED_RESULT.UPDATE_OBJECT_UNEXPECT;
        }

        return returnVo;
    }

    @Override
    public ReturnVo cashStatus(String id, Integer cashStatus, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        AgentUser agentUser = agentUserDao.getById(id);
        if(agentUser != null &&AgentUser.AUTH_STATUS_WAIT == agentUser.getAuthStatus()){
            if(AgentUser.CASH_STATUS_COMMON == cashStatus || AgentUser.CASH_STATUS_FROZEN == cashStatus){
                agentUser.setCashStatus(cashStatus);
                agentUser.preUpdate(headerInfoDto.getCurUserId());
                agentUserDao.updateStatus(agentUser);

                returnVo.code = ReturnVo.SUCCESS;
            }else{
                returnVo.responseCode = ResponseCode.Parameter.ILLEGAL;
            }
            returnVo.responseCode = ResponseCode.UNINTENDED_RESULT.UPDATE_OBJECT_UNEXPECT;
        }

        return returnVo;
    }

    @Override
    public ReturnVo getActiveAgent() {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        List<AgentUser> list = agentUserDao.getActiveAgent();
        List<Map<String, Object>> agents = new ArrayList<>();
        for (AgentUser agent:list
             ) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", agent.getId());
            map.put("name", agent.getAgentName());

            agents.add(map);
        }

        returnVo.code = ReturnVo.SUCCESS;
        returnVo.object = JSONArray.toJSON(agents);

        return returnVo;
    }

    @Override
    public ReturnVo initAgentToRedis(String agentCode) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        AgentUser agentUser = agentUserDao.getByCode(agentCode);
        if(agentUser != null){
            RedisAgentInfoDto agentInfoDto = new RedisAgentInfoDto();
            BeanUtils.copyProperties(agentUser, agentInfoDto);

            ApiResponse apiResponse = sysUserProvider.userInfo(agentUser.getSysUserId());

            if(apiResponse != null && apiResponse.getCode().equals(ResponseCode.Base.SUCCESS.getCode()+"")){
                Map<String, Object> respMap = (Map<String, Object>) apiResponse.getData();
                if(respMap.get("loginName") != null){
                    agentInfoDto.setSysUserLoginName(respMap.get("loginName").toString());
                }
                if(respMap.get("name") != null){
                    agentInfoDto.setSysUserName(respMap.get("name").toString());
                }
            }

            String channels = agentPayChannelDao.getChannelsToStr(agentUser.getId());

            if(StringUtils.isNotBlank(channels))
                agentInfoDto.setActivePayChannels(channels);

            AgentPayChannel aliH5Jump = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.ALI_H5_JUMP.getValue());
            if(aliH5Jump != null)
                agentInfoDto.setAli_h5_wake(aliH5Jump.getAgentRate());

            AgentPayChannel aliQrCode = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.ALI_QR_CODE.getValue());
            if(aliQrCode != null)
                agentInfoDto.setAli_qrcode(aliQrCode.getAgentRate());

            AgentPayChannel aliSelfPay = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.ALI_SELF_PAY.getValue());
            if(aliSelfPay != null)
                agentInfoDto.setAli_self_wap(aliSelfPay.getAgentRate());

            AgentPayChannel qqH5Wake = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.QQ_H5_JUMP.getValue());
            if(qqH5Wake != null)
                agentInfoDto.setQq_h5_wake(qqH5Wake.getAgentRate());

            AgentPayChannel qqQrCode = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.QQ_QR_CODE.getValue());
            if(qqQrCode != null)
                agentInfoDto.setQq_qrcode(qqQrCode.getAgentRate());

            AgentPayChannel qqSelfWap = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.QQ_SELF_PAY.getValue());
            if(qqSelfWap != null)
                agentInfoDto.setQq_self_wap(qqSelfWap.getAgentRate());

            AgentPayChannel jdH5Jump = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.JD_H5_JUMP.getValue());
            if(jdH5Jump != null)
                agentInfoDto.setJd_h5_wake(jdH5Jump.getAgentRate());

            AgentPayChannel jdQrCode = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.JD_QR_CODE.getValue());
            if(jdQrCode != null)
                agentInfoDto.setJd_qrcode(jdQrCode.getAgentRate());

            AgentPayChannel jdSelfPay = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.JD_SELF_PAY.getValue());
            if(jdSelfPay != null)
                agentInfoDto.setJd_self_wap(jdSelfPay.getAgentRate());

            AgentPayChannel gateH5 = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.GATE_H5.getValue());
            if(gateH5 != null)
                agentInfoDto.setGate_h5(gateH5.getAgentRate());

            AgentPayChannel gateQrCode = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.GATE_QR_CODE.getValue());
            if(gateQrCode != null)
                agentInfoDto.setGate_qrcode(gateQrCode.getAgentRate());

            AgentPayChannel gateWebDirect = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.GATE_WEB_DIRECT.getValue());
            if(gateWebDirect != null)
                agentInfoDto.setGate_web_direct(gateWebDirect.getAgentRate());

            AgentPayChannel gateWebSyt = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.GATE_WEB_SYT.getValue());
            if(gateWebSyt != null)
                agentInfoDto.setGate_web_syt(gateWebSyt.getAgentRate());

            AgentPayChannel sytAllIn = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.SYT_ALL_IN.getValue());
            if(sytAllIn != null)
                agentInfoDto.setSyt_all_in(sytAllIn.getAgentRate());

            AgentPayChannel wxH5Jump = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.WX_H5_JUMP.getValue());
            if(wxH5Jump != null)
                agentInfoDto.setWx_h5_wake(wxH5Jump.getAgentRate());

            AgentPayChannel wxQrCode = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.WX_QR_CODE.getValue());
            if(wxQrCode != null)
                agentInfoDto.setWx_qrcode(wxQrCode.getAgentRate());

            AgentPayChannel wxSelfPay = agentPayChannelDao.channelRate(agentUser.getId(), SysPaymentTypeEnum.WX_SELF_PAY.getValue());
            if(wxSelfPay != null)
                agentInfoDto.setWx_self_wap(wxSelfPay.getAgentRate());

            redisClient.SetHsetJedis(RedisConfig.AGENT_INFO_DB, agentCode, MyBeanUtil.transBean2Map2(agentInfoDto));

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

        AgentUser agentUser = agentUserDao.getByCode(code);
        if(AgentUser.DEL_FLAG_COMMON.equals(agentUser.getDelFlag())){
            AgentInfoDto agentInfoDto = new AgentInfoDto();
            BeanUtils.copyProperties(agentUser, agentInfoDto);
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(agentUser);
        }else{
            returnVo.responseCode = new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(),
                    "商户不存在或已被删除");
        }

        return returnVo;
    }

    @Override
    public ReturnVo channelRate(String agentUser, String channelCode) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;

        AgentPayChannel agentPayChannel = agentPayChannelDao.channelRate(agentUser, channelCode);
        if(AgentPayChannel.DEL_FLAG_COMMON.equals(agentPayChannel.getDelFlag())
                || AgentPayChannel.USABLE_YES == agentPayChannel.getUsable()){
            AgentChannelDto agentInfoDto = new AgentChannelDto();
            BeanUtils.copyProperties(agentPayChannel, agentInfoDto);
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(agentUser);
        }else{
            returnVo.responseCode = new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(),
                    "该商户未配置此通道或不可用");
        }

        return returnVo;
    }
}
