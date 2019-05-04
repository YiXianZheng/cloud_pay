package com.cloud.finance.controller;

import com.alibaba.fastjson.JSONObject;
import com.cloud.finance.common.dto.UpdateSecurityCode;
import com.cloud.finance.common.utils.MapUtils;
import com.cloud.finance.dao.ShopAccountDao;
import com.cloud.finance.po.MerchantUser;
import com.cloud.finance.service.ShopAccountService;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代付controller
 */
@RestController
@RequestMapping(value = "/account")
public class ShopAccountController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(ShopAccountController.class);

    @Autowired
    private ShopAccountService accountService;
    @Autowired
    private MerchantUserProvider merchantUserProvider;
    @Autowired
    private ShopAccountDao accountDao;

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

    /**
     * 获取所有商户余额
     * @param headers
     * @return
     */
    @PostMapping("/list")
    public ApiResponse getMerchantUsableMoney(@RequestHeader HttpHeaders headers) {

        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        logger.info("merchantUser: " + headerInfoDto.getMerchantUser());
        PageQuery pageQuery = new PageQuery();
        pageQuery.setParams(new HashMap<>());
        try {
            ApiResponse apiResponse = merchantUserProvider.tablePage(pageQuery);
            if (apiResponse != null && apiResponse.getCode().equals(ResponseCode.Base.SUCCESS + "")) {
                Map<Object, Object> map = MapUtils.json2Map(apiResponse.getData());
                List<MerchantUser> list = JSONObject.parseArray(map.get("data").toString(), MerchantUser.class);
                for (MerchantUser merchantUser : list) {
                    accountService.loadAccount(merchantUser.getSysUserId(), merchantUser.getId());
                }
                Date date = new Date();
                List<Map<String, String>> accountList = accountDao.listPage(date);
                return ApiResponse.creatSuccess(accountList);
            } else {
                logger.error("商户列表为空");
                return ApiResponse.creatSuccess("商户列表为空");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }
}
