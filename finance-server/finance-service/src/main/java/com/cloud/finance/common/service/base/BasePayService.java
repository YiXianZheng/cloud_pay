package com.cloud.finance.common.service.base;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.common.vo.cash.ChannelAccountData;
import com.cloud.finance.common.vo.pay.mid.MidPayCheckResult;
import com.cloud.finance.common.vo.pay.mid.MidPayCreateResult;
import com.cloud.finance.po.ShopPay;
import com.cloud.sysconf.common.dto.ThirdChannelDto;
import org.springframework.stereotype.Service;


/**
 *
 */
@Service
public interface BasePayService {


	/**
	 * 创建扫码支付二维码
	 * @param thirdChannelDto
	 * @param shopPayDto
	 * @return
	 */
	public MidPayCreateResult createQrCode(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto);

	/*公众号，服务号
	 * @param thirdChannelDto
	 * @param shopPayDto
	 * @return
	 */
	public  MidPayCreateResult createAppJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto);

	/*H5
	 * @param thirdChannelDto
	 * @param shopPayDto
	 * @return
	 */
	public  MidPayCreateResult createH5JumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto);

	/*网关直连 直接请求
	 * @param thirdChannelDto 需跳转中间页
	 * @param shopPayDto
	 * @return
	 */
	public  MidPayCreateResult createGateDirectJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto);

	/*网关收银台  跳转到指定地址
	 * @param thirdChannelDto
	 * @param shopPayDto
	 * @return
	 */
	public  MidPayCreateResult createGateSytJump(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto);

	/*快捷
	 * @param thirdChannelDto
	 * @param shopPayDto
	 * @return
	 */
	public  MidPayCreateResult createQuickJumpUrl(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto);

	/*聚合收银台
	 * @param thirdChannelDto
	 * @param shopPayDto
	 * @return
	 */
	public  MidPayCreateResult createSytAllIn(ThirdChannelDto thirdChannelDto, ShopPayDto shopPayDto);


	/*回查
	 * 功能描述:回查订单状态
	 * channelId-通道ID,
	 * channelPayOrderNo-通道代付订单号
	 * sysPayOrderNo--平台支付订单号
	 */
	public MidPayCheckResult checkOrderResult(ThirdChannelDto thirdChannelDto, ShopPay shopPay);

	/*
	 * 功能描述:生成支付订单号(尽量保持在20到24位纯数字字符串)
	 * channelId-通道ID,
	 * assId-商户ID
	 * assPayOrderNo-商户支付订单号
	 */
	public String createSysPayOrderId(String channelId, String assId, String assPayOrderNo);

	/**
	 * 查询通道账户余额
	 * @param thirdChannelDto
	 * @return
	 */
	ChannelAccountData queryAccount(ThirdChannelDto thirdChannelDto);

}

