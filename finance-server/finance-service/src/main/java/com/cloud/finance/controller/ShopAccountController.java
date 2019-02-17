package com.cloud.finance.controller;

import com.cloud.finance.common.dto.UpdateSecurityCode;
import com.cloud.finance.service.ShopAccountService;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 代付controller
 */
@RestController
@RequestMapping(value = "/account")
public class ShopAccountController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(ShopAccountController.class);

    @Autowired
    private ShopAccountService accountService;

    @PostMapping(value = "/info")
    public ApiResponse merchantOrderPage(@RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            return this.toApiResponse(accountService.getAccount(headerInfoDto));
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 商户修改安全码
     * @param securityCode
     * @param headers
     * @return
     */
    @PostMapping("securityCode")
    public ApiResponse updateSecurityCode(@RequestBody UpdateSecurityCode securityCode, @RequestHeader HttpHeaders headers) {

        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        // 只有商户端能操作
        if (!headerInfoDto.getAuth().equals(HeaderInfoDto.AUTH_MERCHANT_SYSTEM)) {
            return this.toApiResponse(ReturnVo.returnFail(ResponseCode.Base.AUTH_ERR));
        }

        ReturnVo returnVo = accountService.updateSecurityCode(securityCode, headerInfoDto.getCurUserId());
        return this.toApiResponse(returnVo);
    }
}
