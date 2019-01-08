package com.cloud.finance.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.vo.ReturnVo;

/**
 * 财务的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface FinanceService {

    /**
     * 生成平台订单，未入库
     * @param mercCode          商户号
     * @param mercPayOrderNo    商户订单号
     * @param mercNotifyUrl     异步通知通知地址
     * @param mercReturnUrl     同步回调地址
     * @param mercCancelUrl     取消回调地址
     * @param paymentType       支付通道
     * @param subPayCode        子支付类型（网关直连，其他的与通道类型一直）
     * @param mercPayMoney      支付金额
     * @param mercPayMessage    商户备注
     * @param mercGoodsTitle    订单标题
     * @param mercGoodsDesc     订单描述
     * @param mercMd5Key        请求签名
     * @param source          订单来源
     * @return
     */
    ShopPayDto beforeCreateOrder(String mercCode, String mercPayOrderNo, String mercNotifyUrl, String mercReturnUrl,
                           String mercCancelUrl,String paymentType, String subPayCode, Double mercPayMoney, String mercPayMessage,
                                 String mercGoodsTitle, String mercGoodsDesc, String mercMd5Key, Integer sysOrderType,
                                 Integer source);

    /**
     * 初始化概览  统计当天数据并与redis中的数据整合
     * @param type 1 平台  2 代理  3 商户 4 所有
     * @param userCode 平台"000000"， 代理或商户ID
     * @return
     */
    ReturnVo initOverview(Integer type, String userCode);

    /**
     * 获取平台概览
     * @param type 1 平台  2 代理  3 商户 4 所有
     * @param userCode 平台"000000"， 代理或商户ID
     * @return
     */
    ReturnVo overview(Integer type, String userCode);

    /**
     * 获取商户排名数据
     * @return
     */
    ReturnVo merchantRanking();

    /**
     * 通道使用情况图表数据
     * @return
     */
    ReturnVo channelGraph();

    ReturnVo channelRate(String code, Double money);

    /**
     * 生成通道财务分析概览数据
     * @param headerInfoDto
     * @return
     */
    ReturnVo initChannelOverview(HeaderInfoDto headerInfoDto);

    /**
     * 获取通道财务分析概览数据
     * @param headerInfoDto
     * @return
     */
    ReturnVo channelOverview(HeaderInfoDto headerInfoDto);
}
