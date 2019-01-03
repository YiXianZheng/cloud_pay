package com.cloud.merchant.controller;

import com.cloud.merchant.common.dto.MerchantFormDto;
import com.cloud.merchant.common.dto.MerchantUpdateFormDto;
import com.cloud.merchant.dao.MerchantPayChannelDao;
import com.cloud.merchant.service.MerchantUserService;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理用户的Controller
 */
@RestController
@RequestMapping("/merchant/user")
public class MerchantUserController extends BaseController {

    @Autowired
    private MerchantUserService merchantUserService;

    /**
     * 添加商户用户
     * @param merchantFormDto
     * @param headers
     * @return
     */
    @PostMapping("/save")
    public ApiResponse save(@RequestBody(required = true) MerchantFormDto merchantFormDto, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = merchantUserService.addMerchant(merchantFormDto, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }


    /**
     * 更新商户用户
     * @param merchantUpdateFormDto
     * @param headers
     * @return
     */
    @PostMapping("/update")
    public ApiResponse update(@RequestBody(required = true) MerchantUpdateFormDto merchantUpdateFormDto, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = merchantUserService.updateMerchant(merchantUpdateFormDto, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 分页搜索商户商  适用于"点击加载更多"类型
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping("/page")
    public ApiResponse listForPage(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = merchantUserService.listForPage(pageQuery, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 分页搜索商户商 适用于列表分页
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping("/tablePage")
    public ApiResponse listForTablePage(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = merchantUserService.listForTablePage(pageQuery, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 删除商户商
     * @param id
     * @param headers
     * @return
     */
    @PostMapping("/delete")
    public ApiResponse delete(@RequestParam(required = true) String id, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = merchantUserService.delete(id, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 商户商详情
     * @param id
     * @return
     */
    @PostMapping("/detail")
    public ApiResponse detail(@RequestParam(required = true) String id){
        try{
            ReturnVo returnVo = merchantUserService.detail(id);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }


    /**
     * 商户商详情
     * @param headers
     * @return
     */
    @PostMapping("/perInfo")
    public ApiResponse perInfo( @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = merchantUserService.detail(headerInfoDto.getMerchantUser());
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 冻结、解冻商户商
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
            ReturnVo returnVo = merchantUserService.optStatus(id, optStatus, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 冻结解冻商户商提现权限
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
            ReturnVo returnVo = merchantUserService.cashStatus(id, cashStatus, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 冻结解冻商户API支付权限
     * @param id
     * @param payStatus  操作权限  1：正常   0：冻结
     * @param headers
     * @return
     */
    @PostMapping("/payStatus")
    public ApiResponse payStatus(@RequestParam(required = true) String id, @RequestParam(required = true) Integer payStatus,
                                  @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = merchantUserService.payStatus(id, payStatus, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 分页搜索代理商的商户  适用于"点击加载更多"类型
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping("/pageForAgent")
    public ApiResponse pageForAgent(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            Map<String, Object> params = pageQuery.getParams();
            if(params == null)
                params = new HashMap<>();
            params.put("agentUser", headerInfoDto.getCurUserId());
            pageQuery.setParams(params);

            ReturnVo returnVo = merchantUserService.listForPage(pageQuery, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 分页搜索代理商的商户 适用于列表分页
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping("/tablePageForAgent")
    public ApiResponse tablePageForAgent(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            Map<String, Object> params = pageQuery.getParams();
            if(params == null)
                params = new HashMap<>();
            params.put("agentUser", headerInfoDto.getCurUserId());
            pageQuery.setParams(params);

            ReturnVo returnVo = merchantUserService.listForTablePage(pageQuery, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 通过商户号初始化商户信息到redis
     * @param merchantCode
     * @return
     */
    @PostMapping("/initMerchantToRedis")
    public ApiResponse initMerchantToRedis(@RequestParam(required = true) String merchantCode){
        try{
            ReturnVo returnVo = merchantUserService.initMerchantToRedis(merchantCode);
            return this.toApiResponse(returnVo);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 商户商详情
     * @param code
     * @return
     */
    @PostMapping("/detailByCode")
    public ApiResponse detailByCode(@RequestParam(required = true) String code) {
        try {
            ReturnVo returnVo = merchantUserService.getByCode(code);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 商户商接口费率
     * @param channelCode
     * @param merchantUser
     * @return
     */
    @PostMapping("/channelRate")
    public ApiResponse channelRate(@RequestParam(required = true) String channelCode,
                                   @RequestParam(required = true) String merchantUser) {
        try {
            ReturnVo returnVo = merchantUserService.channelRate(merchantUser, channelCode);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }


    /**
     * 修改商户通道费率
     * @param id
     * @param channelCode
     * @param agentRate
     * @param usable
     * @param headers
     * @return
     */
    @PostMapping("/updateChannelRate")
    public ApiResponse updateChannelRate(@RequestParam(required = true) String id, @RequestParam(required = true) String channelCode,
                                         @RequestParam(required = true) Double agentRate, @RequestParam Integer usable,
                                         @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = merchantUserService.updateChannelRate(id, channelCode, agentRate, usable, headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e) {
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }
}
