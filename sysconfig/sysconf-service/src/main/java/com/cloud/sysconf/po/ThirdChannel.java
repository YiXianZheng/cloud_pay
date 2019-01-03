package com.cloud.sysconf.po;

import com.cloud.sysconf.common.basePDSC.BasePo;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Entity;

/**
 * 通道信息
 * @Auther Toney
 * @Date 2018/9/13 09:46
 * @Description:
 */
@Entity
@Data
public class ThirdChannel extends BasePo {

    public static final String SIGN_TYPE_MD5 = "MD5";
    public static final String SIGN_TYPE_RSA = "RSA";

    public static final int STATUS_OPEN = 1;
    public static final int STATUS_CLOSE = 2;

    public static final int CHANNEL_TYPE_RECHARGE = 1;
    public static final int CHANNEL_TYPE_PAID = 2;

    @Id
	private String id;						//id
	private String channelName;				//通道名称
	private String externalChannelName;		//通道别名
	private String channelCode;				//通道编码  如"xiandai",开发人员填写，不可修改
	private String channelGroupCode;		//通道组    将两个通道关联起来，通道归并统计
	private Integer channelType;			//通道类型  1:充值  2：代付
	private String payUrl;				    //支付地址
	private String queryUrl;				//查询地址
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

}