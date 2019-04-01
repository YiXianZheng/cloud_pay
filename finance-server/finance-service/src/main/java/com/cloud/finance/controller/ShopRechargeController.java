package com.cloud.finance.controller;

import com.cloud.finance.common.enums.RechargeStatusEnum;
import com.cloud.finance.common.service.base.BaseCashService;
import com.cloud.finance.common.service.base.CashServiceFactory;
import com.cloud.finance.common.utils.SafeComputeUtils;
import com.cloud.finance.common.vo.cash.CashReqData;
import com.cloud.finance.common.vo.cash.CashRespData;
import com.cloud.finance.dao.ShopRechargeDao;
import com.cloud.finance.po.ShopRecharge;
import com.cloud.finance.service.ShopRechargeService;
import com.cloud.sysconf.common.basePDSC.BaseController;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import com.cloud.sysconf.common.enums.DictEnum;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ResultVo;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysuser.provider.SysUserProvider;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 代付controller
 */
@RestController
@RequestMapping(value = "/recharge")
public class ShopRechargeController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(ShopRechargeController.class);

    @Value("${sys.panid}")
    private String panId;

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private CashServiceFactory cashServiceFactory;
    @Autowired
    private ShopRechargeService shopRechargeService;
    @Autowired
    private SysUserProvider sysUserProvider;
    @Autowired
    private ShopRechargeDao shopRechargeDao;

    /**
     * 代付申请
     * @param cashReqData
     * @param headers
     * @return
     */
    @PostMapping("/apply")
    public ApiResponse apply(@RequestBody CashReqData cashReqData, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        ApiResponse apiResponse = sysUserProvider.getMerchantCodes(headerInfoDto.getCurUserId());
        String merchantCode = null;
        if (apiResponse != null && (ResponseCode.Base.SUCCESS + "").equals(apiResponse.getCode())) {
            merchantCode = apiResponse.getData().toString();
        }
        String cashBeginTime = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, DictEnum.CASH_BEGIN_TIME.getCode());
        String cashEndTime = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, DictEnum.CASH_END_TIME.getCode());
        if(StringUtils.isNotBlank(cashBeginTime) && StringUtils.isNotBlank(cashEndTime)){
            String nowStr = DateUtil.DateToString(new Date(), DateUtil.DATE_PATTERN_07);
            Date now = DateUtil.StringToDate(nowStr, DateUtil.DATE_PATTERN_07);
            Date beginTime = DateUtil.StringToDate(cashBeginTime, DateUtil.DATE_PATTERN_07);
            Date endTime = DateUtil.StringToDate(cashEndTime, DateUtil.DATE_PATTERN_07);
            if(now.before(beginTime) || now.after(endTime)) {
                logger.info("[代付申请失败] 当前时间段不可代付【" + cashBeginTime + " - " + cashEndTime + "】");
                return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前时间段不可代付【" + cashBeginTime + " - " + cashEndTime + "】")));
            }
        }

        String cashMin = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, DictEnum.CASH_MIN.getCode());
        Double minCash = StringUtils.isNotBlank(cashMin)?Double.parseDouble(cashMin):0D;
        if(cashReqData.getAmount() < minCash){
            logger.info("[代付申请失败] 单笔代付至少"+cashMin+"元");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "单笔代付至少"+cashMin+"元")));
        }
        String cashMax = redisClient.Gethget(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_SYS_DICT, DictEnum.CASH_MAX.getCode());
        Double maxCash = StringUtils.isNotBlank(cashMax)?Double.parseDouble(cashMax):0D;
        if(cashReqData.getAmount() > maxCash){
            logger.info("[代付申请失败] 单笔代付最高限额"+cashMax+"元");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "单笔代付最高限额"+cashMax+"元")));
        }
        ResultVo resultVo = shopRechargeService.checkAccount(cashReqData, headerInfoDto);
        ThirdChannelDto thirdChannelDto = null;
        if("0".equals(resultVo.getCode())) {
            ShopRecharge shopRecharge = (ShopRecharge) resultVo.getObj();

            String channelCodes = redisClient.Gethget(RedisConfig.MERCHANT_INFO_DB, merchantCode, "thirdChannels");
            logger.info("[通道分配] " + channelCodes);
            Set<String> channels = new HashSet<>();
            if (channelCodes != null && channelCodes.contains(",")) {
                String[] idArr = channelCodes.split(",");
                for (String channelId: idArr) {
                    if(StringUtils.isNotBlank(channelId)){
                        channels.add(channelId);
                    }
                }

                if(channels.size()>0) {
                    for (String channelId : channels) {
                        Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, channelId);
                        if(map.get("channelType") != null && "2".equals(map.get("channelType").toString())){
                            thirdChannelDto = ThirdChannelDto.map2Object(map);
                            break;
                        }
                    }
                }
            }

            if (thirdChannelDto != null) {
                //通过第三方通道的编码获取对应通道的实现类
                BaseCashService baseCashService = cashServiceFactory.getPayment(thirdChannelDto.getChannelCode());
                if(baseCashService == null){
                    logger.info("[代付失败] 通道实现类异常");
                    return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前不可提现，如有疑问请联系客服")));
                }
                CashRespData cashRespData = baseCashService.applyCash(shopRecharge, thirdChannelDto);
                logger.info("[recharge cash] status:" + cashRespData.getStatus());
                if(CashRespData.STATUS_SUCCESS.equals(cashRespData.getStatus()) || CashRespData.STATUS_DOING.equals(cashRespData.getStatus())){
                    return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), cashRespData.getMsg())));
                }else{
                    return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), cashRespData.getMsg())));
                }
            }
            logger.info("[代付申请成功] ShopRechargeNo:" + shopRecharge.getRechargeNo());
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), "代付申请成功【"+ shopRecharge.getRechargeNo() +"】")));
        }else{
            logger.info("[代付申请失败] " + resultVo.getMsg());
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), resultVo.getMsg())));
        }
    }

    /**
     * 分页查询代付订单
     * @param pageQuery
     * @param headers
     * @return
     */
    @PostMapping(value = "/page")
    public ApiResponse merchantOrderPage(@RequestBody PageQuery pageQuery, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);
        try{
            Map<String, Object> params = pageQuery.getParams();
            if(params == null)
                params = new HashMap<>();
            if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth()))
                params.put("sysUserId", headerInfoDto.getCurUserId());
            pageQuery.setParams(params);

            return this.toApiResponse(shopRechargeService.listForTablePage(pageQuery, headerInfoDto));
        } catch (Exception e){
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }

    /**
     * 代付审核
     * @param rechargeNo
     * @param channelId
     * @param headers
     * @return
     */
    @Transactional
    @PostMapping("/auth")
    public ApiResponse auth(@RequestParam String rechargeNo, @RequestParam String channelId, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            logger.info("[代付审核] 权限异常");
            return toApiResponse(ReturnVo.returnFail(ResponseCode.Base.AUTH_ERR));
        }

        ShopRecharge shopRecharge = shopRechargeService.getByRechargeNo(rechargeNo);
        if(shopRecharge == null){
            logger.info("[代付审核] 找不到代付订单");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "找不到代付订单")));
        }
        if(!RechargeStatusEnum.CASH_STATUS_WAIT.getStatus().equals(shopRecharge.getRechargeStatus())){
            logger.info("[代付审核] 当前状态不可操作");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "非审核状态订单不可操作")));
        }

        ThirdChannelDto thirdChannelDto = null;
        Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, channelId);
        if(map.get("channelType") != null && "2".equals(map.get("channelType"))){
            thirdChannelDto = ThirdChannelDto.map2Object(map);
        }
        if("0000".equals(map.get("id"))){
            //人工通道
            shopRecharge.setThirdChannelId(thirdChannelDto.getId());
            shopRecharge.setThirdChannelType(thirdChannelDto.getChannelType());
            shopRecharge.setThirdChannelCostRate(thirdChannelDto.getCashRate());
            shopRechargeService.updateThirdInfo(shopRecharge);
            logger.info("[代付审核] 人工通道 更新通道信息成功");

            shopRecharge.setRechargeStatus(1);
            shopRecharge.setThirdChannelOrderNo("");
            shopRecharge.setThirdChannelRespMsg("人工通道");
            shopRecharge.setCompleteTime(new Date());
            shopRechargeService.rechargeSuccess(shopRecharge);
            logger.info("[代付审核] 人工通道审核成功");
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), "审核成功")));
        }
        if(thirdChannelDto == null){
            logger.info("[代付失败] 通道不可用");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "通道不可用")));
        }

        Double rechargeMoney = SafeComputeUtils.sub(shopRecharge.getRechargeMoney(), shopRecharge.getRechargeRateMoney());
        //通道代付限额
        Double channelPayMin = thirdChannelDto.getPayPerMin();
        if (rechargeMoney < channelPayMin) {
            logger.info("[代付失败]["+thirdChannelDto.getId()+"] 单笔支付最低 :"+ channelPayMin);
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "支付金额小于"+channelPayMin+"元")));
        }
        Double channelPayMax = thirdChannelDto.getPayPerMax();
        if (rechargeMoney > channelPayMax) {
            logger.info("[代付失败]["+thirdChannelDto.getId()+"] 单笔支付最高 :"+ channelPayMax);
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "单笔支付金额大于"+channelPayMax+"元")));
        }

        shopRecharge.setThirdChannelId(thirdChannelDto.getId());
        shopRecharge.setThirdChannelType(thirdChannelDto.getChannelType());
        shopRecharge.setThirdChannelCostRate(thirdChannelDto.getCashRate());
        shopRechargeService.updateThirdInfo(shopRecharge);

        //通过第三方通道的编码获取对应通道的实现类
        BaseCashService baseCashService = cashServiceFactory.getPayment(thirdChannelDto.getChannelCode());
        if(baseCashService == null){
            logger.info("[代付失败] 通道实现类异常");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前不可提现，如有疑问请联系客服")));
        }

        CashRespData cashRespData = baseCashService.applyCash(shopRecharge, thirdChannelDto);
        logger.info("[recharge cash] status:"+cashRespData.getStatus());
        if(CashRespData.STATUS_SUCCESS.equals(cashRespData.getStatus()) || CashRespData.STATUS_DOING.equals(cashRespData.getStatus())){
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode(), cashRespData.getMsg())));
        }else{
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), cashRespData.getMsg())));
        }

    }

    /**
     * 代付驳回
     * @param rechargeNo
     * @param remarks
     * @param headers
     * @return
     */
    @PostMapping("/reject")
    public ApiResponse reject(@RequestParam String rechargeNo, @RequestParam String remarks, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            logger.info("[代付驳回] 权限异常");
            return toApiResponse(ReturnVo.returnFail(ResponseCode.Base.AUTH_ERR));
        }

        ShopRecharge shopRecharge = shopRechargeService.getByRechargeNo(rechargeNo);
        if(shopRecharge == null){
            logger.info("[代付驳回] 找不到代付订单");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "找不到代付订单")));
        }
        if(!RechargeStatusEnum.CASH_STATUS_WAIT.getStatus().equals(shopRecharge.getRechargeStatus())
                && !RechargeStatusEnum.CASH_STATUS_SUCCESS.getStatus().equals(shopRecharge.getRechargeStatus())
                && !RechargeStatusEnum.CASH_STATUS_DOING.getStatus().equals(shopRecharge.getRechargeStatus())){
            logger.info("[代付驳回] 当前状态不可操作");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "非审核状态订单不可操作")));
        }

        shopRecharge.setRechargeStatus(RechargeStatusEnum.CASH_STATUS_REJECT.getStatus());
        shopRecharge.setRemarks(remarks);
        shopRecharge.preUpdate(headerInfoDto.getCurUserId());
        ReturnVo returnVo = shopRechargeService.updateRechargeStatus(shopRecharge);

        logger.info("[代付驳回] status:"+returnVo.code);
        if(ReturnVo.SUCCESS == returnVo.code){
            return toApiResponse(ReturnVo.returnSuccess());
        }else{
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "代付驳回失败")));
        }

    }

    /**
     * 代付银行账户信息修改
     * @param rechargeNo
     * @param cashReqData
     * @param headers
     * @return
     */
    @PostMapping("/updateBankInfo")
    public ApiResponse updateBankInfo(@RequestParam String rechargeNo, @RequestBody CashReqData cashReqData, @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            logger.info("[代付银行账户信息修改] 权限异常");
            return toApiResponse(ReturnVo.returnFail(ResponseCode.Base.AUTH_ERR));
        }

        ShopRecharge shopRecharge = shopRechargeService.getByRechargeNo(rechargeNo);
        if(shopRecharge == null){
            logger.info("[代付银行账户信息修改] 找不到代付订单");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "找不到代付订单")));
        }
        if(!RechargeStatusEnum.CASH_STATUS_WAIT.getStatus().equals(shopRecharge.getRechargeStatus())){
            logger.info("[代付银行账户信息修改] 当前状态不可操作");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "非审核状态订单不可操作")));
        }

        if(StringUtils.isNotBlank(cashReqData.getBankCode())) {
            logger.info("[代付银行账户信息修改] 更新银行编码：" + cashReqData.getBankCode());
            shopRecharge.setBankCode(cashReqData.getBankCode());
        }
        if(StringUtils.isNotBlank(cashReqData.getBankAccount())) {
            logger.info("[代付银行账户信息修改] 更新银行卡持卡人名称：" + cashReqData.getBankAccount());
            shopRecharge.setBankAccount(cashReqData.getBankAccount());
        }
        if(StringUtils.isNotBlank(cashReqData.getBankNo())) {
            logger.info("[代付银行账户信息修改] 更新银行卡卡号：" + cashReqData.getBankNo());
            shopRecharge.setBankNo(cashReqData.getBankNo());
        }
        if(StringUtils.isNotBlank(cashReqData.getBin())) {
            logger.info("[代付银行账户信息修改] 更新银行联行号：" + cashReqData.getBin());
            shopRecharge.setBankBin(cashReqData.getBin());
        }
        if(StringUtils.isNotBlank(cashReqData.getSubbranch())) {
            logger.info("[代付银行账户信息修改] 更新银行开户行支行：" + cashReqData.getSubbranch());
            shopRecharge.setBankSubbranch(cashReqData.getSubbranch());
        }
        if(StringUtils.isNotBlank(cashReqData.getProvince())) {
            logger.info("[代付银行账户信息修改] 更新银行所在省：" + cashReqData.getProvince());
            shopRecharge.setProvince(cashReqData.getProvince());
        }
        if(StringUtils.isNotBlank(cashReqData.getCity())) {
            logger.info("[代付银行账户信息修改] 更新银行所在城市：" + cashReqData.getCity());
            shopRecharge.setCity(cashReqData.getCity());
        }
        if(cashReqData.getFee() != null) {
            logger.info("[代付银行账户信息修改] 更新代付手续费：" + cashReqData.getFee());
            shopRecharge.setRechargeRateMoney(cashReqData.getFee());
        }
        shopRecharge.preUpdate(headerInfoDto.getCurUserId());
        ReturnVo returnVo = shopRechargeService.updateBankInfo(shopRecharge);
        logger.info("[代付银行账户信息修改] status:"+returnVo.code);
        if(ReturnVo.SUCCESS == returnVo.code){
            return toApiResponse(ReturnVo.returnSuccess());
        }else{
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "代付银行账户信息修改失败")));
        }

    }

    /**
     * 代付申请
     * @param cashReqData
     * @param headers
     * @return
     */
    @PostMapping("/admin/apply")
    public ApiResponse adminApply(@RequestBody CashReqData cashReqData, @RequestHeader HttpHeaders headers,
                                  @RequestParam String channelId){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        if(!(HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth()) && "000001".equals(headerInfoDto.getPanId()))){
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "无操作权限")));
        }

        if(channelId == null){
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "通道id不能为空")));
        }
        Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, channelId);
        ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);

        if(thirdChannelDto == null){
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "通道不存在")));
        }

        //通过第三方通道的编码获取对应通道的实现类
        BaseCashService baseCashService = cashServiceFactory.getPayment(thirdChannelDto.getChannelCode());
        if(baseCashService == null){
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "当前不可提现，如有疑问请联系客服")));
        }

        CashRespData cashRespData = baseCashService.adminApplyCash(cashReqData, thirdChannelDto);
        logger.info("[recharge cash] status:"+cashRespData.getStatus());
        if(CashRespData.STATUS_SUCCESS.equals(cashRespData.getStatus()) || CashRespData.STATUS_DOING.equals(cashRespData.getStatus())){
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode()), cashRespData.getMsg()));
        }else{
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), cashRespData.getMsg())));
        }

    }

    /**
     * 查询通道的代付状态，若已经代付成功，更新代付单状态
     * @param rechargeNo
     * @return
     */
    @PostMapping("/queryChannelCashStatus")
    public ApiResponse queryChannelCashStatus(@RequestParam String rechargeNo){

        ShopRecharge shopRecharge = shopRechargeService.getByRechargeNo(rechargeNo);
        if(shopRecharge==null){
            logger.info("[代付状态查询失败]：找不到代付单");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode()), "代付单不存在"));
        }
        if(RechargeStatusEnum.CASH_STATUS_SUCCESS.getStatus() == shopRecharge.getRechargeStatus()){
            logger.info("[代付状态查询成功]：代付单已完结");
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode()), "代付成功"));
        }
        Map<String, String> map = redisClient.Gethgetall(RedisConfig.THIRD_PAY_CHANNEL, shopRecharge.getThirdChannelId());
        ThirdChannelDto thirdChannelDto = ThirdChannelDto.map2Object(map);
        if(thirdChannelDto == null){
            logger.info("[代付状态查询失败]：通道异常");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode()), "通道异常"));
        }
        if(StringUtils.isBlank(thirdChannelDto.getQueryUrl())){
            logger.info("[代付状态查询失败]：该通道不支持此操作");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode()), "该通道不支持此操作"));
        }

        //通过第三方通道的编码获取对应通道的实现类
        BaseCashService baseCashService = cashServiceFactory.getPayment(thirdChannelDto.getChannelCode());
        if(baseCashService == null){
            logger.info("[代付状态查询失败] 通道实现类异常");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode()), "通道实现类异常"));
        }

        CashRespData cashRespData = baseCashService.queryCash(shopRecharge, thirdChannelDto);
        logger.info("[代付状态查询] status:"+cashRespData.getStatus());
        if(CashRespData.STATUS_SUCCESS.equals(cashRespData.getStatus()) || CashRespData.STATUS_DOING.equals(cashRespData.getStatus())){
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode()), cashRespData.getMsg()));
        }else{
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), cashRespData.getMsg())));
        }

    }

    /**
     * 后台手动下发
     * @param merchantCode  商户号
     * @param bankCode      银行编码
     * @param cardNo        卡号
     * @param account       持卡人
     * @param money         代付金额
     * @param subbranch     支行名称
     * @param province      所在省份
     * @param city          所在城市
     * @param headers
     * @return
     */
    @PostMapping("/create")
    public ApiResponse apiCreate(@RequestParam String merchantCode, @RequestParam String bankCode, @RequestParam String cardNo,
                                @RequestParam String account, @RequestParam Double money, @RequestParam String subbranch,
                                @RequestParam String province, @RequestParam String city, @RequestParam Double fee,
                                 @RequestHeader HttpHeaders headers){
        HeaderInfoDto headerInfoDto = this.getHeaderInfo(headers);

        if(!HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(headerInfoDto.getAuth())){
            logger.info("[创建代付单失败] 权限异常");
            return toApiResponse(ReturnVo.returnFail(ResponseCode.Base.AUTH_ERR));
        }
        if(StringUtils.isBlank(merchantCode)){
            logger.info("[创建代付单失败] 商户号不能为空");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "商户号不能为空")));
        }
        if(StringUtils.isBlank(bankCode)){
            logger.info("[创建代付单失败] 银行编码不能为空");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "银行编码不能为空")));
        }
        if(StringUtils.isBlank(cardNo)){
            logger.info("[创建代付单失败] 银行卡号不能为空");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "银行卡号不能为空")));
        }
        if(StringUtils.isBlank(account)){
            logger.info("[创建代付单失败] 持卡人不能为空");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "持卡人不能为空")));
        }
        if(money == null){
            logger.info("[创建代付单失败] 下发金额不能为空");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "下发金额不能为空")));
        }

        Map<String, String> merchant = redisClient.Gethgetall(RedisConfig.MERCHANT_INFO_DB, merchantCode);
        if(merchant == null || merchant.size()==0){
            logger.info("[创建代付单失败] 商户不存在");
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "商户不存在")));
        }

        ShopRecharge shopRecharge = new ShopRecharge();
        shopRecharge.setUserId(merchant.get("sysUserId"));
        shopRecharge.setMerchantUser(merchant.get("id"));
        shopRecharge.setBankCode(bankCode);
        shopRecharge.setBankAccount(account);
        shopRecharge.setBankNo(cardNo);
        shopRecharge.setRechargeMoney(money);
        shopRecharge.setBankSubbranch(subbranch);
        shopRecharge.setProvince(province);
        shopRecharge.setCity(city);
        shopRecharge.setRechargeRateMoney(fee);
        shopRecharge.setRechargeStatus(1);
        shopRecharge.setCompleteTime(new Date());
        shopRecharge.setRemarks("手动下发");
        shopRecharge.preInsert(headerInfoDto.getCurUserId(), panId);
        shopRecharge = shopRechargeService.apiCreate(shopRecharge);
        logger.info("[创建代付单失败] rechargeNo:"+shopRecharge.getRechargeNo());
        if(StringUtils.isNotBlank(shopRecharge.getId())){
            return toApiResponse(ReturnVo.returnSuccess(new ResponseCode.COMMON(ResponseCode.Base.SUCCESS.getCode()), shopRecharge.getRechargeNo()));
        }else{
            return toApiResponse(ReturnVo.returnFail(new ResponseCode.COMMON(ResponseCode.Base.ERROR.getCode(), "创建失败")));
        }
    }

    /**
     * 统计下发数据
     * @param userId
     * @param bankNo
     * @param today
     * @return
     */
    @PostMapping("/summaryPaid")
    public ApiResponse summaryPaid(@RequestParam String userId, @RequestParam String bankNo, @RequestParam String today) {

        try {
            Map<String, Object> map = shopRechargeDao.summaryPaid(userId, bankNo, today);
            return toApiResponse(ReturnVo.returnSuccess(map));
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
        }
    }
}
