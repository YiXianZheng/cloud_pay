package com.cloud.finance.controller.api;

import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.service.base.CashServiceFactory;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.utils.SysCashResultConstants;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.po.ShopAccount;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopAccountService;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.Util;
import com.cloud.sysconf.common.vo.ResultVo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * ecash对外服务接口
 */
@RestController
@RequestMapping(value = "/ecash")
public class EbankCashController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(EbankCashController.class);

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private CashServiceFactory cashServiceFactory;
    @Autowired
    private ShopAccountService shopAccountService;


    /**
     * 代付申请(主系统开放API)
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/apply")
    public String apply(HttpServletRequest request, HttpServletResponse response){

        long startTime = System.currentTimeMillis();   //获取开始时间
        String bankAccount = this.getStringParameter("bankAccount");    //开户人
        String bankCode = this.getStringParameter("bankCode");          //银行编码
        String bankNo = this.getStringParameter("bankNo");              //银行卡卡号
        String amount = this.getStringParameter("amount");              //代付金额  分为单位
        Double amountYuan = SafeComputeUtils.div(this.getDoubleParameter("amount"), 100D);    //以元为单位
        String merCode = this.getStringParameter("merCode");            //商户号
        String key = this.getStringParameter("key");                    //安全码
        //非必填
        String bankSubbranch = this.getStringParameter("bankSubbranch");    //支行
        String bin = this.getStringParameter("bin");                        //银行卡联行号
        String province = this.getStringParameter("province");              //银行卡省份
        String city = this.getStringParameter("city");                      //银行卡城市

        if(StringUtils.isBlank(bankAccount)){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_PARAMS_ERR, "开户人为空");
        }
        if(StringUtils.isBlank(bankCode)){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_PARAMS_ERR, "银行编号为空");
        }
        if(StringUtils.isBlank(bankNo)){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_PARAMS_ERR, "银行卡号为空");
        }
        if(StringUtils.isBlank(amount)){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_PARAMS_ERR, "代付金额为空");
        }
        if(StringUtils.isBlank(merCode)){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_PARAMS_ERR, "商户号为空");
        }
        if(StringUtils.isBlank(key)){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_PARAMS_ERR, "秘钥为空");
        }

        // 判断银行卡是否已审核
        // merCode  商户号获取用户id
        // bankNo   根据卡号与商户id查询银行卡信息

        String cashMin = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "CASH_MIN");
        Double minCash = StringUtils.isNotBlank(cashMin)?Double.parseDouble(cashMin):0D;
        if(amountYuan < minCash){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_AMOUNT_ERR, "单笔代付最低"+cashMin+"元");
        }
        String cashMax = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, "CASH_MAX");
        Double maxCash = StringUtils.isNotBlank(cashMax)?Double.parseDouble(cashMax):0D;
        if(amountYuan > maxCash){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_AMOUNT_ERR, "单笔代付最高"+cashMax+"元");
        }
        logger.info("[ecash apply] -- mercahnt code:" + merCode);
        ResultVo resultVo = shopRechargeService.checkAccountForAPI(merCode, amountYuan, key, bankAccount, bankCode, bankNo, bankSubbranch, bin, province, city);
        if("1".equals(resultVo.getCode())){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_CHECK_ERR, resultVo.getMsg());
        }
        ShopAccount shopAccount = shopAccountService.getAccountByMerchantCode(merCode);
        if(shopAccount == null || shopAccount.getUsableMoney()<amountYuan){
            logger.info("[ecash apply] -- 余额不足");
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_ACCOUNT_ERR, "代付余额不足");
        }
        ShopRecharge shopRecharge = (ShopRecharge) resultVo.getObj();

        ThirdChannelDto thirdChannelDto = cashServiceFactory.channelRoute(null, shopRecharge.getMerchantUser(), amountYuan);
        if(thirdChannelDto == null){
            logger.info("[ecash apply] -- 通道异常");
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_CHANNEL_ERR, "当前不可提现，如有疑问请联系客服");
        }

        //通道代付限额
        Double channelPayMin = thirdChannelDto.getPayPerMin();
        if (amountYuan < channelPayMin) {
            logger.info("[代付失败]["+thirdChannelDto.getId()+"] 单笔代付最低 :"+ channelPayMin);
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_CHANNEL_ERR, "单笔代付金额最低"+channelPayMin+"元");
        }
        Double channelPayMax = thirdChannelDto.getPayPerMax();
        if (amountYuan > channelPayMax) {
            logger.info("[代付失败]["+thirdChannelDto.getId()+"] 单笔代付最高 :"+ channelPayMax);
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_CHANNEL_ERR, "单笔代付金额最高"+channelPayMax+"元");
        }

        shopRecharge.setThirdChannelId(thirdChannelDto.getId());
        shopRecharge.setThirdChannelType(thirdChannelDto.getChannelType());
        shopRecharge.setThirdChannelCostRate(thirdChannelDto.getCashRate());
        shopRechargeService.updateThirdInfo(shopRecharge);

        //通过第三方通道的编码获取对应通道的实现类
        BaseCashService baseCashService = cashServiceFactory.getPayment(thirdChannelDto.getChannelCode());
        if(baseCashService == null){
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_CHANNEL_ERR, "当前不可提现，如有疑问请联系客服");
        }

        CashRespData cashRespData = baseCashService.applyCash(shopRecharge, thirdChannelDto);
        if(CashRespData.STATUS_SUCCESS.equals(cashRespData.getStatus()) || CashRespData.STATUS_DOING.equals(cashRespData.getStatus())){
            Map<String, String> map = new HashMap<>();
            map.put("code", cashRespData.getStatus());
            map.put("msg", cashRespData.getMsg());

            long endTime = System.currentTimeMillis(); //获取结束时间
            logger.info("[create cash request success]:cost- " + (endTime - startTime) + "ms,assCode:" + merCode );

            return renderString(response, map);
        }else{
            return renderFailString(response, false, SysCashResultConstants.ERROR_CASH_APPLY_ERR, cashRespData.getMsg());
        }
    }

}
