package com.cloud.finance.common.vo.pay.req;

import com.cloud.sysconf.common.utils.finance.MD5Util;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * 请求H5扫码支付API需要提交的数据
 */
@Data
public class RepPayCreateData {

	// 每个字段具体的意思请查看API文档
	private String assCode = "";		//商户编码
	private String assPayOrderNo = "";	//商户订单号
	
	private String assNotifyUrl = "";	//通知地址
	private String assReturnUrl = "";	//返回地址
	private String assCancelUrl = "";	//取消跳转
	private String assPayMoney = "";		//支付金额
	
	//付款信息
	private String paymentType = "";	//支付类型
	private String subPayCode = "";		//支付子类型
	
	//不参与签名
	private String assPayMessage = "";	//附带信息
	private String assGoodsTitle = "";	//商品名称
	private String assGoodsDesc = "";	//商品描述
	
	public static final String[] SIGN_PARAMS = {"assCode","assPayOrderNo","assNotifyUrl","assReturnUrl","assCancelUrl","paymentType","subPayCode","assPayMoney"};

	private String sign = "";
	public RepPayCreateData(String assCode, String assPayOrderNo,
			String assNotifyUrl, String assReturnUrl,String assCancelUrl, 
			String paymentType, String subPayCode,String assPayMoney, 
			String assPayMessage, String assGoodsTitle,String assGoodsDesc, 
			String md5Key) throws Exception {
		super();
		this.assCode = assCode;
		this.assPayOrderNo = assPayOrderNo;
		
		this.assNotifyUrl = assNotifyUrl;
		this.assReturnUrl = assReturnUrl;
		this.assCancelUrl = assCancelUrl;
		
		this.paymentType = paymentType;
		this.subPayCode = subPayCode;
		this.assPayMoney = assPayMoney;
		
		this.assPayMessage = assPayMessage;
		this.assGoodsTitle = assGoodsTitle;
		this.assGoodsDesc = assGoodsDesc;
		this.sign = MD5Util.getSign(toSignParamsMap(), md5Key);
	}

	private Map<String, Object> toSignParamsMap() {
		Map<String, Object> map = new HashMap<>();
		Field[] fields = this.getClass().getDeclaredFields();
		for (Field field : fields) {
			Object obj;
			String fieldName=field.getName();
			try {
				obj = field.get(this);
				if (obj != null) {
					if(!fieldName.equals("sign")&&Arrays.asList(SIGN_PARAMS).contains(fieldName)){
						map.put(field.getName(), obj);
					}
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return map;
	}

}
