package com.cloud.sysconf.controller;

import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelInfoDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.po.ThirdChannel;
import com.cloud.sysconf.service.SysPayChannelService;
import com.cloud.sysconf.service.ThirdChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付通道的Controller
 * @Auther Toney
 * @Date 2018/7/29 17:20
 * @Description:
 */
@RestController
@RequestMapping(value = "/sys/pay/channel")
public class SysPayChannelController extends BaseController {

    @Autowired
    private SysPayChannelService sysPayChannelService;
    @Autowired
    private ThirdChannelService thirdChannelService;

    /**
     * 获取有效的系统支付通道
     * @return
     */
    @RequestMapping("/loadValidChannel")
    public ApiResponse loadMenu(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            ReturnVo returnVo = sysPayChannelService.loadValidPayChannel(headerInfoDto);
            return this.toApiResponse(returnVo);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 初始化通道数据到Redis
     * @return
     */
    @RequestMapping("/initChannelToRedis")
    public ApiResponse initChannelToRedis(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            if(HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())) {
                return this.toApiResponse(sysPayChannelService.initChannelToRedis());
            }else{
                return ApiResponse.creatFail(ResponseCode.Base.AUTH_ERR);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 加在通道列表
     * @param headers
     * @return
     */
    @RequestMapping("/loadChannels")
    public ApiResponse loadChannels(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            if(HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())) {
                return this.toApiResponse(sysPayChannelService.loadChannels());
            }else{
                return ApiResponse.creatFail(ResponseCode.Base.AUTH_ERR);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }


    /**
     * 更新第三方通道信息
     * @param headers
     * @param thirdChannelInfoDto
     * @return
     */
    @RequestMapping("/third/update")
    public ApiResponse updateChannel(@RequestHeader HttpHeaders headers, @RequestBody(required = true) ThirdChannelInfoDto thirdChannelInfoDto){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            if(HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())) {
                return this.toApiResponse(thirdChannelService.thirdChannelInfoDto(headerInfoDto, thirdChannelInfoDto));
            }else{
                return ApiResponse.creatFail(ResponseCode.Base.AUTH_ERR);
            }
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 获取有效的第三方支付通道列表
     * @return
     */
    @RequestMapping("/third/payList")
    public ApiResponse payList(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            return ApiResponse.creatFail(ResponseCode.Base.AUTH_ERR);
        }
        try{
            ReturnVo returnVo = thirdChannelService.loadValidChannel(ThirdChannel.CHANNEL_TYPE_RECHARGE);
            return this.toApiResponse(returnVo);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 获取有效的第三方代付通道列表
     * @return
     */
    @RequestMapping("/third/cashList")
    public ApiResponse thirdList(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            return ApiResponse.creatFail(ResponseCode.Base.AUTH_ERR);
        }
        try{
            ReturnVo returnVo = thirdChannelService.loadValidChannel(ThirdChannel.CHANNEL_TYPE_PAID);
            return this.toApiResponse(returnVo);
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }
}
