package com.cloud.finance.controller;

import com.cloud.agent.provider.AgentUserProvider;
import com.cloud.finance.common.dto.AccountInfoDto;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.service.base.CashServiceFactory;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopAccount;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopAccountService;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ResultVo;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

}
