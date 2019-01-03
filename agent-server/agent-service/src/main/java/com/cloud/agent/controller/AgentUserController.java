package com.cloud.agent.controller;

import com.cloud.agent.common.dto.AgentFormDto;
import com.cloud.agent.service.AgentUserService;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 代理用户的Controller
 */
@RestController
@RequestMapping("/agent/user")
public class AgentUserController extends BaseController {

    @Autowired
    private AgentUserService agentUserService;

    /**
     * 添加代理用户
     * @param agentFormDto
     * @param headers
     * @return
     */
    @PostMapping("/save")
    public ApiResponse save(@RequestBody(required = true) AgentFormDto agentFormDto, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = agentUserService.addOrUpdate(agentFormDto, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 分页搜索代理商  适用于"点击加载更多"类型
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping("/page")
    public ApiResponse listForPage(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = agentUserService.listForPage(pageQuery, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 分页搜索代理商 适用于列表分页
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping("/tablePage")
    public ApiResponse listForTablePage(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = agentUserService.listForTablePage(pageQuery, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 删除代理商
     * @param id
     * @param headers
     * @return
     */
    @PostMapping("/delete")
    public ApiResponse delete(@RequestParam(required = true) String id, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = agentUserService.delete(id, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 代理商详情
     * @param id
     * @return
     */
    @PostMapping("/detail")
    public ApiResponse detail(@RequestParam(required = true) String id){
        try{
            ReturnVo returnVo = agentUserService.detail(id);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 代理商详情
     * @param headers
     * @return
     */
    @PostMapping("/perInfo")
    public ApiResponse detail(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = agentUserService.detail(headerInfoDto.getAgentUser());
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 审核代理商
     * @param id
     * @param headers
     * @return
     */
    @PostMapping("/auth")
    public ApiResponse auth(@RequestParam(required = true) String id, @RequestParam(required = true) Integer authStatus,
                            @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = agentUserService.auth(id, authStatus, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 冻结、解冻代理商
     * @param id
     * @param optStatus  操作权限  1：正常   0：冻结
     * @param headers
     * @return
     */
    @PostMapping("/optStatus")
    public ApiResponse optStatus(@RequestParam(required = true) String id, @RequestParam(required = true) Integer optStatus,
                            @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = agentUserService.optStatus(id, optStatus, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 冻结解冻代理商提现权限
     * @param id
     * @param cashStatus  操作权限  1：正常   0：冻结
     * @param headers
     * @return
     */
    @PostMapping("/cashStatus")
    public ApiResponse cashStatus(@RequestParam(required = true) String id, @RequestParam(required = true) Integer cashStatus,
                            @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = agentUserService.cashStatus(id, cashStatus, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 代理商详情
     * @return
     */
    @PostMapping("/activeAgent")
    public ApiResponse getActiveAgent(){
        try{
            ReturnVo returnVo = agentUserService.getActiveAgent();
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 代理商详情  应用通讯专用
     * @param id
     * @return
     */
    @PostMapping("/detailForProvider")
    public ApiResponse detailForProvider(@RequestParam(required = true) String id){
        try{
            ReturnVo returnVo = agentUserService.detail(id);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 通过代理号初始化代理信息到redis
     * @param agentCode
     * @return
     */
    @PostMapping("/initAgentToRedis")
    public ApiResponse initMerchantToRedis(@RequestParam(required = true) String agentCode){
        try{
            ReturnVo returnVo = agentUserService.initAgentToRedis(agentCode);
            return this.toApiResponse(returnVo);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 代理商详情
     * @param code
     * @return
     */
    @PostMapping("/detailByCode")
    public ApiResponse detailByCode(@RequestParam(required = true) String code) {
        try {
            ReturnVo returnVo = agentUserService.getByCode(code);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 代理商接口费率
     * @param channelCode
     * @param agentUser
     * @return
     */
    @PostMapping("/channelRate")
    public ApiResponse channelRate(@RequestParam(required = true) String channelCode,
                                   @RequestParam(required = true) String agentUser) {
        try {
            ReturnVo returnVo = agentUserService.channelRate(agentUser, channelCode);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }
}
