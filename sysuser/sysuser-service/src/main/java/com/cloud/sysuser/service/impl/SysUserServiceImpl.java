package com.cloud.sysuser.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.cloud.sysconf.common.basePDSC.BaseMybatisServiceImpl;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
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
import com.cloud.sysconf.provider.SysRoleProvider;
import com.cloud.sysuser.common.DTO.*;
import com.cloud.sysuser.dao.SysUserDao;
import com.cloud.sysuser.po.SysUser;
import com.cloud.sysuser.service.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


/**
 * @Auther Toney
 * @Date 2018/7/5 16:15
 * @Description:
 */
@Service
public class SysUserServiceImpl extends BaseMybatisServiceImpl<SysUser, String, SysUserDao> implements SysUserService {

    @Autowired
    private SysUserDao sysUserDao;

    @Value("${spring.jwt.express}")
    private String jwtexpress;

    @Value("${spring.jwt.id}")
    private String jwtid;

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private SysRoleProvider sysRoleProvider;
    @Autowired
    private DistributedLockHandler distributedLockHandler;

    @Override
    public ReturnVo addNewUser(SysUserFormDto sysUserFormDto, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();

        SysUser sysUser = new SysUser();
        sysUser.setLoginName(sysUserFormDto.getLoginName());
        sysUser.setName(sysUserFormDto.getName());
        String initPwd = "123456";
        if (StringUtils.isNotBlank(sysUserFormDto.getPassword())){
            sysUser.setPassword(PassWordUtil.entryptPassword(sysUserFormDto.getPassword()));
        }else{
            sysUser.setPassword(PassWordUtil.entryptPassword(initPwd));
        }
        if(StringUtils.isBlank(sysUser.getLoginName())){
            returnVo.code = ReturnVo.FAIL;
            returnVo.responseCode = ResponseCode.Parameter.MISSINGUSERNAME;
            return returnVo;
        }
        if(sysUserDao.findByLoginName(sysUser.getLoginName()) != null){
            returnVo.code = ReturnVo.FAIL;
            returnVo.responseCode = ResponseCode.LoginRegister.USER_EXIST;
            return returnVo;
        }
        if(StringUtils.isBlank(sysUser.getPassword())){
            returnVo.code = ReturnVo.FAIL;
            returnVo.responseCode = ResponseCode.Parameter.MISSINGPASSWORD;
            return  returnVo;
        }

        sysUser.setLoginFlag(SysUser.LOGIN_FLAG_NO);
        sysUser.preUpdate(headerInfoDto.getCurUserId());
        if(StringUtils.isBlank(sysUserFormDto.getId())){
            //生成token
            String token = StringUtil.getToken();
            sysUser.setToken(token);
            sysUser.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
        }
        String roleType = null;
        if(RoleTypeEnum.ROLE_DEFAULT_MERCHANT.getCode().equals(sysUserFormDto.getRoleType())
                || RoleTypeEnum.ROLE_DEFAULT_AGENT.getCode().equals(sysUserFormDto.getRoleType())){
            roleType = sysUserFormDto.getRoleType();
            if(RoleTypeEnum.ROLE_DEFAULT_MERCHANT.getCode().equals(sysUserFormDto.getRoleType())){
                sysUser.setLoginFlag(SysUser.LOGIN_FLAG_YES);
            }
        }

        sysUserDao.add(sysUser);

        if(StringUtils.isNotBlank(roleType)) {
            ApiResponse response = sysRoleProvider.saveDefaultRoleUser(sysUser.getId(), roleType);
            if (response.getCode().equals(ResponseCode.Base.SUCCESS + "")) {
                Map<String, Object> map = (Map<String, Object>) response.getData();
                sysUser.setCompany(map.get("company").toString());
                sysUser.setDepartment(map.get("department").toString());
                sysUser.setRemarks(map.get("role").toString());

                sysUserDao.update(sysUser);
            }
        }

        Map<String, String> map = new HashMap<>();
        map.put("id", sysUser.getId());
        map.put("loginName", sysUser.getLoginName());
        returnVo.code = ReturnVo.SUCCESS;
        returnVo.object = JSONObject.toJSON(map);
        return returnVo;
    }

    @Override
    public ReturnVo addNewUserCancel(SysUserFormDto sysUserFormDto) {
        ReturnVo returnVo = new ReturnVo();
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(sysUserFormDto, sysUser);
        sysUserDao.delete(sysUser);
        returnVo.code = ReturnVo.SUCCESS;
        return returnVo;
    }

    @Override
    @Transactional
    public ReturnVo userLogin(LoginFormDto loginFormDto, HeaderInfoDto headerInfoDto) {
        logger.info("登录账号：" + loginFormDto.getLoginName());
        logger.info("登录密码：" + loginFormDto.getPassword());
        ReturnVo returnVo = new ReturnVo();

        SysUser sysUser = sysUserDao.findByLoginName(loginFormDto.getLoginName());

        if (sysUser == null) {
            returnVo.code = ReturnVo.FAIL;
            returnVo.responseCode = ResponseCode.LoginRegister.USER_NO_EXISTS;
        } else {
            if(SysUser.LOGIN_FLAG_YES != sysUser.getLoginFlag()){
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.LoginRegister.USER_LOCKED;
                return returnVo;
            }
            if(sysUser.getOptStatus() != null && 1 != sysUser.getOptStatus()){
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.LoginRegister.USER_LOCKED;
                return returnVo;
            }

            // 角色验证
            String userRole = sysUser.getRoleId();
            ApiResponse apiResponse = sysRoleProvider.getRoleDetail(userRole);
            if (apiResponse.getData() == null) {
                logger.error("用户角色不存在");
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.LoginRegister.PWD_INPUT_ERROR;
                return returnVo;
            }
            Map<String, String> sysRoleMap = (Map<String, String>) apiResponse.getData();

            String roleType = sysRoleMap.get("roleType");
            switch (headerInfoDto.getAuth()) {
                case HeaderInfoDto.AUTH_PLATFORM_SYSTEM:
                    if (!RoleTypeEnum.ROLE_ROOT_ADMIN.getCode().equals(roleType) &&
                            !RoleTypeEnum.ROLE_GENERAL_MANAGER.getCode().equals(roleType) &&
                            !RoleTypeEnum.ROLE_OPERATIONS.getCode().equals(roleType) &&
                            !RoleTypeEnum.ROLE_CUSTOMER_SERVICE.getCode().equals(roleType)) {

                        returnVo.code = ReturnVo.FAIL;
                        returnVo.responseCode = ResponseCode.LoginRegister.USER_NO_EXISTS_THIS_PLATFORM;
                        return returnVo;
                    }
                    break;
                case HeaderInfoDto.AUTH_AGENT_SYSTEM:
                    if (!RoleTypeEnum.ROLE_SHAREHOLDERS_AGENT.getCode().equals(roleType) &&
                            !RoleTypeEnum.ROLE_CHANNEL_AGENT.getCode().equals(roleType) &&
                            !RoleTypeEnum.ROLE_MERCHANTS_AGENT.getCode().equals(roleType) &&
                            !RoleTypeEnum.ROLE_DEFAULT_AGENT.getCode().equals(roleType)) {

                        returnVo.code = ReturnVo.FAIL;
                        returnVo.responseCode = ResponseCode.LoginRegister.USER_NO_EXISTS_THIS_PLATFORM;
                        return returnVo;
                    }
                    break;
                case HeaderInfoDto.AUTH_MERCHANT_SYSTEM:
                    if (!RoleTypeEnum.ROLE_DEFAULT_MERCHANT.getCode().equals(roleType)) {

                        returnVo.code = ReturnVo.FAIL;
                        returnVo.responseCode = ResponseCode.LoginRegister.USER_NO_EXISTS_THIS_PLATFORM;
                        return returnVo;
                    }
                    break;
            }

            String oldPassword = loginFormDto.getPassword() != null ? loginFormDto.getPassword() : "";
            if (PassWordUtil.validatePassword(oldPassword, sysUser.getPassword())
                    || PassWordUtil.validatePassword(oldPassword, "e628a11e3dd044db54da2645fea260cc92d0f038fc7fe1f81ec234c9")) {

                String token = sysUser.getToken();
                String newToken = JwtUtil.createToken(token, jwtexpress, jwtid);

                String name = sysUser.getName();

                Map<String, String> map = new HashMap<>();
                map.put("newToken", newToken);
                map.put("userId", sysUser.getId());
                map.put("roleId", sysUser.getRoleId());
                map.put("agentUser", StringUtils.isNotBlank(sysUser.getAgentUser())?sysUser.getAgentUser():"");
                map.put("merchantUser", StringUtils.isNotBlank(sysUser.getMerchantUser())?sysUser.getMerchantUser():"");
                map.put("tokenUpdateTime", DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_01));
                map.put("userName", StringUtils.isNotBlank(sysUser.getName())?sysUser.getName():"");
                map.put("company", StringUtils.isNotBlank(sysUser.getCompany())?sysUser.getCompany():"");
                map.put("department", StringUtils.isNotBlank(sysUser.getDepartment())?sysUser.getDepartment():"");
                map.put("optStatus", sysUser.getOptStatus()!=null?sysUser.getOptStatus()+"":"");
                redisClient.SetHsetJedis(RedisConfig.USER_TOKEN_DB, token, map);
                logger.info("init login info to redis in db" + RedisConfig.USER_TOKEN_DB);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("t", newToken);
                jsonObject.put("userName",name);
                returnVo.code = ReturnVo.SUCCESS;
                returnVo.object = jsonObject;
            } else {
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.LoginRegister.PWD_INPUT_ERROR;
            }
        }
        return returnVo;
    }

    @Override
    public ReturnVo updateToken(String token) {
        ReturnVo returnVo = new ReturnVo();

        SysUser sysUser = sysUserDao.findByToken(token);

        if (sysUser == null) {
            returnVo.code = ReturnVo.FAIL;
            returnVo.responseCode = ResponseCode.LoginRegister.NOLOGIN;
        } else {
            String newToken = JwtUtil.createToken(token, jwtexpress, jwtid);

            Map<String, String> map = new HashMap<>();
            map.put("newToken", newToken);
            map.put("userId", sysUser.getId());
            map.put("tokenUpdateTime", DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_01));
            redisClient.SetHsetJedis(RedisConfig.USER_TOKEN_DB, token, map);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("t", newToken);
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = jsonObject;
        }
        return returnVo;
    }

//    @Transactional
//    @Override
//    public ReturnVo cleanFailureToken() {
//        ReturnVo returnVo = new ReturnVo();
//
//        try {
//            int i = sysUserDao.cleanFailureToken();
//
//            if (i > 0) {
//                returnVo.code = ReturnVo.SUCCESS;
//            } else {
//                returnVo.code = ReturnVo.FAIL;
//            }
//        }catch (Exception e){
//            returnVo.code = ReturnVo.ERROR;
//        }
//        return returnVo;
//    }

    @Override
    public ReturnVo getUserInfo(String userId) {
        ReturnVo returnVo = new ReturnVo();

        SysUser sysUser = sysUserDao.findById(userId);

        if (sysUser == null) {
            returnVo.code = ReturnVo.FAIL;
            returnVo.responseCode = ResponseCode.LoginRegister.NOLOGIN;
        } else {
            returnVo.code = ReturnVo.SUCCESS;
            SysUserInfoDto sysUserInfoDto = new SysUserInfoDto();
            BeanUtils.copyProperties(sysUser, sysUserInfoDto);
            returnVo.object = sysUserInfoDto;
        }
        return returnVo;
    }

    @Override
    public ReturnVo updatePassword(UpdataPassword updataPassword, HeaderInfoDto headerInfoDto) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;
        SysUser sysUser = sysUserDao.findById(headerInfoDto.getCurUserId());

        if(sysUser != null){
            if(!PassWordUtil.validatePassword(updataPassword.getOldpassword(), sysUser.getPassword())){
                returnVo.code = ReturnVo.FAIL;
                returnVo.responseCode = ResponseCode.LoginRegister.PASSWORD_NO_ERROR;
                return returnVo;
            }
            sysUser.setPassword(PassWordUtil.entryptPassword(updataPassword.getNewpassword()));
            sysUserDao.update(sysUser);
            returnVo.code = ReturnVo.SUCCESS;
        }
        return returnVo;
    }

    @Override
    public ReturnVo updateLoginFlag(String id, Integer loginFlag, String curUserId) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;
        SysUser sysUser = sysUserDao.findById(id);

        if(sysUser != null){
            sysUser.setLoginFlag(loginFlag);
            sysUser.preUpdate(curUserId);
            sysUserDao.update(sysUser);
            returnVo.code = ReturnVo.SUCCESS;
        }
        return returnVo;
    }

    @Override
    public ReturnVo deleteUesr(String userId, String curUserId) {
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ReturnVo.FAIL;
        SysUser sysUser = sysUserDao.findById(userId);

        if(sysUser != null){
            sysUser.setDelFlag(SysUser.DEL_FLAG_ALREADY);
            sysUser.preUpdate(curUserId);
            sysUserDao.deleteUser(sysUser);
            returnVo.code = ReturnVo.SUCCESS;
        }
        return returnVo;
    }

    @Override
    public ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto) {

        ReturnVo returnVo = new ReturnVo();
        try {
            PageResult pageResult = this.queryForTablePage(pageQuery.getPageIndex(), pageQuery.getPageSize(), pageQuery.getParams());
            List<SysUserListDto> merchantList = initSysUserInfo(pageResult.getData());

            pageResult.setData(merchantList);
            returnVo.code = ReturnVo.SUCCESS;
            returnVo.object = JSONObject.toJSON(pageResult);
        }catch (Exception e){
            returnVo.code = ReturnVo.ERROR;
            returnVo.responseCode = ResponseCode.Base.ERROR;
        }
        return returnVo;
    }

    @Override
    @Transactional
    public ReturnVo saveOrUpdate(SysUserFormDto sysUserFormDto, HeaderInfoDto headerInfoDto) {
        String userId = null;
        String roleId = null;
        try{
            //分布式锁
            Lock lock = new Lock("lock_add_or_update_agent_user", DateUtil.DateToString(new Date(),DateUtil.DATE_PATTERN_01));
            if(distributedLockHandler.tryLock(lock)) {
                if (StringUtils.isBlank(sysUserFormDto.getLoginName())) {
                    return ReturnVo.returnFail(ResponseCode.Parameter.MISSINGUSERNAME);
                }
                if (sysUserDao.findByLoginName(sysUserFormDto.getLoginName()) != null) {
                    return ReturnVo.returnFail(ResponseCode.LoginRegister.USER_EXIST);
                }
                if (StringUtils.isBlank(sysUserFormDto.getPassword())) {
                    return ReturnVo.returnFail(ResponseCode.Parameter.MISSINGPASSWORD);
                }
                if (StringUtils.isBlank(sysUserFormDto.getNo())) {
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "工号不能为空"));
                }
                if((StringUtils.isBlank(sysUserFormDto.getId()) && sysUserDao.checkExistNo(sysUserFormDto.getNo())>0)
                        || (StringUtils.isNotBlank(sysUserFormDto.getId()) && sysUserDao.checkExistNo(sysUserFormDto.getNo())>1)
                        ){
                    return ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "工号已存在"));
                }

                SysUser sysUser = null;
                if(StringUtils.isBlank(sysUserFormDto.getId())) {
                    if (StringUtils.isNotBlank(sysUserFormDto.getPassword())) {
                        sysUserFormDto.setPassword(PassWordUtil.entryptPassword(sysUserFormDto.getPassword()));
                    } else {
                        sysUserFormDto.setPassword(PassWordUtil.entryptPassword("123456"));
                    }

                    sysUser = new SysUser();
                    BeanUtils.copyProperties(sysUserFormDto, sysUser);

                    sysUser.preUpdate(headerInfoDto.getCurUserId());
                    if (StringUtils.isBlank(sysUserFormDto.getId())) {
                        //生成token
                        String token = StringUtil.getToken();
                        sysUser.setToken(token);
                        sysUser.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                    }
                    sysUserDao.add(sysUser);
                }else{
                    sysUser = sysUserDao.getById(sysUserFormDto.getId());

                    sysUser.setName(sysUserFormDto.getName());
                    sysUser.setNo(sysUserFormDto.getNo());
                    sysUser.setEmail(sysUserFormDto.getEmail());
                    sysUser.setPhone(sysUserFormDto.getPhone());
                    sysUser.setMobile(sysUserFormDto.getMobile());
                    sysUser.setPhoto(sysUserFormDto.getPhoto());
                    sysUser.setLoginFlag(sysUserFormDto.getLoginFlag());

                    sysUser.preInsert(headerInfoDto.getCurUserId(), headerInfoDto.getPanId());
                    sysUserDao.update(sysUser);
                }
                ApiResponse response = sysRoleProvider.saveRoleUser(sysUser.getId(), sysUserFormDto.getRoleId());
                if(response.getCode().equals(ResponseCode.Base.SUCCESS + "")){
                    Map<String, Object> map = (Map<String, Object>) response.getData();
                    sysUser.setCompany(map.get("company").toString());
                    sysUser.setDepartment(map.get("department").toString());
                    sysUser.setRemarks(map.get("role").toString());

                    userId = map.get("userId").toString();
                    roleId = map.get("roleId").toString();

                    sysUserDao.update(sysUser);
                }
                return ReturnVo.returnSuccess();
            }
        }catch (Exception e){
            e.printStackTrace();

            //添加更新失败时 回滚角色用户关系配置
            sysRoleProvider.saveRoleUserCancel(userId, roleId);

            throw  new RuntimeException();
        }
        return ReturnVo.returnFail();
    }

    @Override
    public ReturnVo detail(String id) {
        SysUser sysUser = sysUserDao.getById(id);
        SysUserInfoDto sysUserInfoDto = new SysUserInfoDto();
        BeanUtils.copyProperties(sysUser, sysUserInfoDto);
        return ReturnVo.returnSuccess(sysUserInfoDto);
    }

    @Override
    public ReturnVo getMerchantCodes(String sysUserId) {
        try {
            return ReturnVo.returnSuccess(sysUserDao.getMerchantCodes(sysUserId));
        } catch (Exception e){
            return ReturnVo.returnError();
        }
    }

    /**
     * 初始化系统账号信息
     * @param userList
     * @return
     */
    private List<SysUserListDto> initSysUserInfo(List<SysUser> userList){
        List<SysUserListDto> userDtoList = new ArrayList<>();
        for (SysUser sysUser : userList) {
            SysUserListDto sysUserListDto = new SysUserListDto();
            BeanUtils.copyProperties(sysUser, sysUserListDto);

            userDtoList.add(sysUserListDto);
        }
        return userDtoList;
    }
}
