package com.cloud.sysconf.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 通道信息
 * @Auther Toney
 * @Date 2018/9/13 09:46
 * @Description:
 */
@Data
public class ThirdChannelDto extends BaseDto {

    public static final int STATUS_OPEN = 1;
    public static final int STATUS_CLOSE = 2;

    public static final int CHANNEL_TYPE_RECHARGE = 1;
    public static final int CHANNEL_TYPE_PAID = 2;

	private String id;						//id
	private String channelName;				//通道名称
	private String externalChannelName;		//通道别称
	private String channelCode;				//通道编码
	private String channelGroupCode;		//通道组
	private Integer channelType;			//通道类型  1:充值  2：代付
	private String payUrl;				    //支付域名 网关支付类需平台跳转的支付地址
	private String queryUrl;				//查询域名
    private String notifyUrl;               //通道回调地址
    private String merchantId;              //商户号
    private String adminUrl;				//管理地址
	private String adminUserName;			//管理账号
	private String adminLoginPassword;		//管理密码
	private String adminCashPassword;		//支付密码
	private String appId;					//APPID
	private String appKey;					//APP秘钥
	private String payMd5Key;				//支付秘钥
	private String cashMd5Key;				//代付秘钥
	private Double cashRate;				//代付手续费
	private String rsaChannelPublicKeyId;	//RSA通道公钥
	private String rsaSelfPrivateKeyId;		//RSA加密私钥
	private String rsaSelfPublicKeyId;		//RSA加密公钥
	private String signType;				//签名方式
	private Double payDayMax;				//每日交易最高限额
	private Double payPerMax;				//单笔最高限额
	private Double payPerMin;				//单笔最低
	private Integer routeWeight;			//权重
	private Integer routePayStatus;			//收银开启状态
	private Integer routeCashStatus;		//代付开启状态

    private Integer openRandom;			//是否开启随机数风控  0 关闭  1 开启整数随机  2 开启小数随机
    private int randomMin;			    //随机数最小值
    private int randomMax;			    //随机数最大值

	//接口费率  对照sys_pay_channel表
	private Double qq_qrcode;
	private Double qq_self_wap;
	private Double qq_h5_wake;
	private Double jd_self_wap;
	private Double jd_h5_wake;
	private Double jd_qrcode;
	private Double wx_self_wap;
	private Double wx_h5_wake;
	private Double wx_qrcode;
	private Double ali_self_wap;
	private Double ali_h5_wake;
	private Double ali_qrcode;
	private Double syt_all_in;
	private Double gate_h5;
	private Double gate_web_syt;
	private Double gate_web_direct;
	private Double gate_qrcode;

	public static ThirdChannelDto map2Object(Map<String, String> map){
		ThirdChannelDto thirdChannelDto = new ThirdChannelDto();

		if(map == null || map.size() == 0){
			return thirdChannelDto;
		}

		thirdChannelDto.setId(map.get("id"));
		thirdChannelDto.setChannelName(map.get("channelName"));
		thirdChannelDto.setChannelCode(map.get("channelCode"));
		thirdChannelDto.setChannelGroupCode(map.get("channelGroupCode"));
        if(StringUtils.isNotBlank(map.get("channelType"))) {
            thirdChannelDto.setChannelType(Integer.parseInt(map.get("channelType").toString()));
        }else{
            thirdChannelDto.setChannelType(STATUS_CLOSE);
        }
		thirdChannelDto.setPayUrl(map.get("payUrl"));
		thirdChannelDto.setQueryUrl(map.get("queryUrl"));
        thirdChannelDto.setNotifyUrl(map.get("notifyUrl"));
        thirdChannelDto.setMerchantId(map.get("merchantId"));
		thirdChannelDto.setAdminUrl(map.get("adminUrl"));
		thirdChannelDto.setAdminUserName(map.get("adminUserName"));
		thirdChannelDto.setAdminLoginPassword(map.get("adminLoginPassword"));
		thirdChannelDto.setAdminCashPassword(map.get("adminCashPassword"));
		thirdChannelDto.setAppId(map.get("appId"));
		thirdChannelDto.setAppKey(map.get("appKey"));
		thirdChannelDto.setPayMd5Key(map.get("payMd5Key"));
		thirdChannelDto.setCashMd5Key(map.get("cashMd5Key"));
		if(StringUtils.isNotBlank(map.get("cashRate"))) {
			thirdChannelDto.setCashRate(Double.parseDouble(map.get("cashRate").toString()));
		}else{
			thirdChannelDto.setCashRate(0D);
		}

		thirdChannelDto.setRsaChannelPublicKeyId(map.get("rsaChannelPublicKeyId"));
		thirdChannelDto.setRsaSelfPrivateKeyId(map.get("rsaSelfPrivateKeyId"));
		thirdChannelDto.setRsaSelfPublicKeyId(map.get("rsaSelfPublicKeyId"));
		thirdChannelDto.setSignType(map.get("signType"));

		if(StringUtils.isNotBlank(map.get("payDayMax"))) {
			thirdChannelDto.setPayDayMax(Double.parseDouble(map.get("payDayMax").toString()));
		}else{
			thirdChannelDto.setPayDayMax(0D);
		}

		if(StringUtils.isNotBlank(map.get("payPerMax"))) {
			thirdChannelDto.setPayPerMax(Double.parseDouble(map.get("payPerMax").toString()));
		}else{
			thirdChannelDto.setPayPerMax(0D);
		}

		if(StringUtils.isNotBlank(map.get("payPerMin"))) {
			thirdChannelDto.setPayPerMin(Double.parseDouble(map.get("payPerMin").toString()));
		}else{
			thirdChannelDto.setPayPerMin(0D);
		}

		if(StringUtils.isNotBlank(map.get("routeWeight"))) {
			thirdChannelDto.setRouteWeight(Integer.parseInt(map.get("routeWeight").toString()));
		}else{
			thirdChannelDto.setRouteWeight(STATUS_CLOSE);
		}

		if(StringUtils.isNotBlank(map.get("routePayStatus"))) {
			thirdChannelDto.setRoutePayStatus(Integer.parseInt(map.get("routePayStatus").toString()));
		}else{
			thirdChannelDto.setRoutePayStatus(STATUS_CLOSE);
		}

		if(StringUtils.isNotBlank(map.get("routeCashStatus"))) {
			thirdChannelDto.setRouteCashStatus(Integer.parseInt(map.get("routeCashStatus").toString()));
		}else{
			thirdChannelDto.setRouteCashStatus(STATUS_CLOSE);
		}

        if(StringUtils.isNotBlank(map.get("openRandom"))) {
            thirdChannelDto.setOpenRandom(Integer.parseInt(map.get("openRandom").toString()));
        }else{
            thirdChannelDto.setOpenRandom(0);
        }

        if(StringUtils.isNotBlank(map.get("randomMin"))) {
            thirdChannelDto.setRandomMin(Integer.parseInt(map.get("randomMin").toString()));
        }else{
            thirdChannelDto.setRandomMin(0);
        }

        if(StringUtils.isNotBlank(map.get("randomMax"))) {
            thirdChannelDto.setRandomMax(Integer.parseInt(map.get("randomMax").toString()));
        }else{
            thirdChannelDto.setRandomMax(0);
        }

		if(StringUtils.isNotBlank(map.get("qq_qrcode"))) {
			thirdChannelDto.setQq_qrcode(Double.parseDouble(map.get("qq_qrcode").toString()));
		}else{
			thirdChannelDto.setQq_qrcode(0D);
		}

		if(StringUtils.isNotBlank(map.get("qq_self_wap"))) {
			thirdChannelDto.setQq_self_wap(Double.parseDouble(map.get("qq_self_wap").toString()));
		}else{
			thirdChannelDto.setQq_self_wap(0D);
		}

		if(StringUtils.isNotBlank(map.get("qq_h5_wake"))) {
			thirdChannelDto.setQq_h5_wake(Double.parseDouble(map.get("qq_h5_wake").toString()));
		}else{
			thirdChannelDto.setQq_h5_wake(0D);
		}

		if(StringUtils.isNotBlank(map.get("jd_self_wap"))) {
			thirdChannelDto.setJd_self_wap(Double.parseDouble(map.get("jd_self_wap").toString()));
		}else{
			thirdChannelDto.setJd_self_wap(0D);
		}

		if(StringUtils.isNotBlank(map.get("jd_h5_wake"))) {
			thirdChannelDto.setJd_h5_wake(Double.parseDouble(map.get("jd_h5_wake").toString()));
		}else{
			thirdChannelDto.setJd_h5_wake(0D);
		}

		if(StringUtils.isNotBlank(map.get("jd_qrcode"))) {
			thirdChannelDto.setJd_qrcode(Double.parseDouble(map.get("jd_qrcode").toString()));
		}else{
			thirdChannelDto.setJd_qrcode(0D);
		}

		if(StringUtils.isNotBlank(map.get("wx_self_wap"))) {
			thirdChannelDto.setWx_self_wap(Double.parseDouble(map.get("wx_self_wap").toString()));
		}else{
			thirdChannelDto.setWx_self_wap(0D);
		}

		if(StringUtils.isNotBlank(map.get("wx_h5_wake"))) {
			thirdChannelDto.setWx_h5_wake(Double.parseDouble(map.get("wx_h5_wake").toString()));
		}else{
			thirdChannelDto.setWx_h5_wake(0D);
		}

		if(StringUtils.isNotBlank(map.get("wx_qrcode"))) {
			thirdChannelDto.setWx_qrcode(Double.parseDouble(map.get("wx_qrcode").toString()));
		}else{
			thirdChannelDto.setWx_qrcode(0D);
		}

		if(StringUtils.isNotBlank(map.get("ali_self_wap"))) {
			thirdChannelDto.setAli_self_wap(Double.parseDouble(map.get("ali_self_wap").toString()));
		}else{
			thirdChannelDto.setAli_self_wap(0D);
		}

		if(StringUtils.isNotBlank(map.get("ali_h5_wake"))) {
			thirdChannelDto.setAli_h5_wake(Double.parseDouble(map.get("ali_h5_wake").toString()));
		}else{
			thirdChannelDto.setAli_h5_wake(0D);
		}

		if(StringUtils.isNotBlank(map.get("ali_qrcode"))) {
			thirdChannelDto.setAli_qrcode(Double.parseDouble(map.get("ali_qrcode").toString()));
		}else{
			thirdChannelDto.setAli_qrcode(0D);
		}

		if(StringUtils.isNotBlank(map.get("syt_all_in"))) {
			thirdChannelDto.setSyt_all_in(Double.parseDouble(map.get("syt_all_in").toString()));
		}else{
			thirdChannelDto.setSyt_all_in(0D);
		}

		if(StringUtils.isNotBlank(map.get("gate_h5"))) {
			thirdChannelDto.setGate_h5(Double.parseDouble(map.get("gate_h5").toString()));
		}else{
			thirdChannelDto.setGate_h5(0D);
		}

		if(StringUtils.isNotBlank(map.get("gate_web_syt"))) {
			thirdChannelDto.setGate_web_syt(Double.parseDouble(map.get("gate_web_syt").toString()));
		}else{
			thirdChannelDto.setGate_web_syt(0D);
		}

		if(StringUtils.isNotBlank(map.get("gate_web_direct"))) {
			thirdChannelDto.setGate_web_direct(Double.parseDouble(map.get("gate_web_direct").toString()));
		}else{
			thirdChannelDto.setGate_web_direct(0D);
		}

		if(StringUtils.isNotBlank(map.get("gate_qrcode"))) {
			thirdChannelDto.setGate_qrcode(Double.parseDouble(map.get("gate_qrcode").toString()));
		}else{
			thirdChannelDto.setGate_qrcode(0D);
		}

		return thirdChannelDto;
	}

}