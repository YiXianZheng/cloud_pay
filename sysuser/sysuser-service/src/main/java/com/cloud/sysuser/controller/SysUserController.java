package com.cloud.sysuser.controller;

import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.PassWordUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.StringUtil;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysuser.common.DTO.*;
import com.cloud.sysuser.service.SysUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sys/user")
public class SysUserController extends BaseController {

    @Value("${sys.default.password}")
    private String DEFAULT_PASSWORD;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 系统用户注册
     * @param headers
     * @param loginFormDto
     * @return
     */
    @PostMapping("/register")
    public ApiResponse register(@RequestHeader HttpHeaders headers, @RequestBody(required = true) LoginFormDto loginFormDto){

        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        SysUserFormDto sysUserFormDto = new SysUserFormDto();
        BeanUtils.copyProperties(loginFormDto, sysUserFormDto);
        ReturnVo returnVo = sysUserService.addNewUser(sysUserFormDto, headerInfoDto);
        return toApiResponse(returnVo);
    }

    /**
     * 系统用户登陆
     * @param loginForm
     * @param headers
     * @return
     */
    @PostMapping("/login")
    public ApiResponse userLogin(@RequestBody(required = true) LoginFormDto loginForm, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        if(StringUtils.isBlank(loginForm.getLoginName())){
            return ApiResponse.creatFail(ResponseCode.Parameter.MISSINGUSERNAME);
        }
        if(StringUtils.isBlank(loginForm.getPassword())){
            return ApiResponse.creatFail(ResponseCode.Parameter.MISSINGPASSWORD);
        }


        try{

            ReturnVo returnVo = sysUserService.userLogin(loginForm, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }

    }

    /**
     * 更新Token
     * @param headers
     * @return
     */
    @PostMapping("/updateToken")
    public ApiResponse updateToken(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        String token = headerInfoDto.getToken();

        //如果token存在，则更新
        try{
            if(StringUtil.isNotBlank(token)){
                ReturnVo returnVo = sysUserService.updateToken(token);
                return this.toApiResponse(returnVo);
            }else{
                return ApiResponse.creatFail(ResponseCode.Parameter.LACK);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }


    }


    /**
     * 更新Password
     *
     * @param headers
     * @return
     */
    @PostMapping("/updatePassword")
    public ApiResponse updatePassword(@RequestBody(required = true) UpdataPassword updataPassword, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        if(StringUtils.isBlank(updataPassword.getOldpassword()) || StringUtils.isBlank(updataPassword.getNewpassword())
            || StringUtils.isBlank(updataPassword.getReinputspassword())){
            return ApiResponse.creatFail(ResponseCode.LoginRegister.PASSWORD_EMPTY);
        }

        if(!updataPassword.getNewpassword().equals(updataPassword.getReinputspassword())){
            return ApiResponse.creatFail(ResponseCode.LoginRegister.PASSWORD_NO_CONSISTENT);
        }

        if(updataPassword.getNewpassword().equals(updataPassword.getOldpassword())){
            return ApiResponse.creatFail(ResponseCode.LoginRegister.PASSWORD_EQUALLY);
        }

        try{

            ReturnVo returnVo = sysUserService.updatePassword(updataPassword, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }

    }


//    @PostMapping("/cleanFailureToken")
//    public ApiResponse cleanFailureToken(){
//        ReturnVo returnVo = sysUserService.cleanFailureToken();
//        if(returnVo.code == ReturnVo.FAIL){
//            return ApiResponse.creatSuccess("没有数据更新");
//        }else if(returnVo.code == ReturnVo.SUCCESS){
//            return ApiResponse.creatSuccess("清除成功");
//        }else{
//            return ApiResponse.creatFail(ResponseCode.Base.ERROR);
//        }
//    }

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    @PostMapping("/infoForProvider")
    public ApiResponse infoForProvider(@RequestParam(required = true) String userId){
        try{
            if(StringUtil.isNotBlank(userId)){
                ReturnVo returnVo = sysUserService.getUserInfo(userId);
                return this.toApiResponse(returnVo);
            }else{
                return ApiResponse.creatFail(ResponseCode.Parameter.LACK);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 新增或者保存管理账号
     *  外包调用的API
     * @param sysUserFormDto
     * @return
     */
    @PostMapping("/addNewUser")
    public ApiResponse addNewUser(@RequestHeader HttpHeaders headers, @RequestBody(required = true) SysUserFormDto sysUserFormDto){

        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        System.out.println("添加商户顺便添加用户");
        try {
            if(StringUtils.isBlank(sysUserFormDto.getPassword())){
                sysUserFormDto.setPassword(DEFAULT_PASSWORD);
            }
            ReturnVo returnVo = sysUserService.addNewUser(sysUserFormDto, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 新增或者保存管理账号
     *
     * @param sysUserProviderDto
     * @return
     */
    @PostMapping("/addNewUserForProvider")
    public ApiResponse addNewUserForProvider(@RequestBody(required = true) SysUserProviderDto sysUserProviderDto){
        SysUserFormDto sysUserFormDto = new SysUserFormDto();
        BeanUtils.copyProperties(sysUserProviderDto, sysUserFormDto);

        HeaderInfoDto headerInfoDto = new HeaderInfoDto();
        headerInfoDto.setCurUserId(sysUserProviderDto.getOptUser());
        headerInfoDto.setPanId(sysUserProviderDto.getPanId());

        if(StringUtils.isBlank(sysUserFormDto.getPassword())){
            sysUserFormDto.setPassword(DEFAULT_PASSWORD);
        }
        ReturnVo returnVo = sysUserService.addNewUser(sysUserFormDto, headerInfoDto);
        return this.toApiResponse(returnVo);
    }

    /**
     * 新增或者保存管理账号的补偿方法
     * @param sysUserProviderDto
     * @return
     */
    @PostMapping("/addNewUserCancel")
    public ApiResponse addNewUserCancel(@RequestBody(required = true) SysUserProviderDto sysUserProviderDto){
        SysUserFormDto sysUserFormDto = new SysUserFormDto();
        BeanUtils.copyProperties(sysUserProviderDto, sysUserFormDto);

        ReturnVo returnVo = sysUserService.addNewUserCancel(sysUserFormDto);
        return this.toApiResponse(returnVo);
    }

    /**
     * 更新系统账号登录权限
     * @param userId
     * @param loginFlag
     * @param curUserId
     * @return
     */
    @PostMapping("/updateLoginFlagForProvider")
    public ApiResponse updateLoginFlagForProvider(@RequestParam String userId, @RequestParam Integer loginFlag,
                                                  @RequestParam String curUserId){

        ReturnVo returnVo = sysUserService.updateLoginFlag(userId, loginFlag, curUserId);
        return this.toApiResponse(returnVo);
    }

    /**
     * 删除系统账号
     * @param userId
     * @param curUserId
     * @return
     */
    @PostMapping("/deleteForProvider")
    public ApiResponse deleteForProvider(@RequestParam String userId, @RequestParam String curUserId){

        ReturnVo returnVo = sysUserService.deleteUesr(userId, curUserId);
        return this.toApiResponse(returnVo);
    }

    /**
     * 分页搜索系统用户 适用于列表分页
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping("/tablePage")
    public ApiResponse tablePageForAgent(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = sysUserService.listForTablePage(pageQuery, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 新增或保存系统用户信息
     * @param headers
     * @param sysUserFormDto
     * @return
     */
    @PostMapping("/save")
    public ApiResponse save(@RequestHeader HttpHeaders headers, @RequestBody(required = true) SysUserFormDto sysUserFormDto){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        return this.toApiResponse(sysUserService.saveOrUpdate(sysUserFormDto, headerInfoDto));
    }

    /**
     * 获取用户信息
     * @param id
     * @returnk
     */
    @PostMapping("/detail")
    public ApiResponse detail(@RequestParam String id){
        return this.toApiResponse(sysUserService.detail(id));
    }



    /**
     * 获取用户的下级商户编号
     * @param sysUserId
     * @return
     */
    @PostMapping("/getMerchantCodes")
    public ApiResponse getMerchantCodes(@RequestParam(required = true) String sysUserId) {
        try {
            ReturnVo returnVo = sysUserService.getMerchantCodes(sysUserId);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 验证超级权限
     * @param password
     * @return
     */
    @PostMapping("/rootPwd")
    public ApiResponse checkRootPower(@RequestParam String password) {
        String rootPwd = "d4d6273acbd4f215929db9e06c415c8ef6ccd8228cda89e486fc784f";
        try {
            if (PassWordUtil.validatePassword(password, rootPwd)) {
                return ApiResponse.creatSuccess("密码正确");
            } else {
                return ApiResponse.creatSuccess("密码错误");
            }
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }
}
