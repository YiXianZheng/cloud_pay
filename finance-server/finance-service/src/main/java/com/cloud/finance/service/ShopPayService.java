package com.cloud.finance.service;

import com.cloud.finance.common.dto.ShopPayDto;
import com.cloud.finance.po.ShopPay;
import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ReturnVo;

/**
 * ShopPay的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface ShopPayService extends BaseMybatisService<ShopPay, String> {

    /**
     * 新增或者更新
     * @param shopPayDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo save(ShopPayDto shopPayDto, HeaderInfoDto headerInfoDto);

    /**
     * 判断商户订单是否已存在  存在则返回true
     * @param merchantCode
     * @param mercPayOrderNo
     * @return
     */
    boolean checkExist(String merchantCode, String mercPayOrderNo);

    /**
     * 分页获取平台订单
     * @param pageQuery  分页条件
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     *
     * 每月将一个月之前的订单数据移至历史库
     *      e.g. 加入今天是八月某日，那么就把六月一号之前的数据移至历史库
     * @return
     */
    ReturnVo monthMove();

    /**
     * 每天凌晨更新昨日的订单  step 1  -->  step 2
     *     并更新redis统计的缓存数据
     * @return
     */
    ReturnVo dailyUpdate();

    /**
     * 重置近一个月的统计数据
     */
    void initMonth();

    /**
     * 通过订单号获取订单信息
     * @param orderNo
     * @return
     */
    ShopPay getBySysOrderNo(String orderNo);

    /**
     * 更新订单状态
     * @param shopPay
     */
    void updateOrderStatus(ShopPay shopPay);

    /**
     * 更新订单通道信息
     * @param sysPayOrderNo
     * @param channelId
     */
    void updateThirdInfo(String sysPayOrderNo, String channelId);

    /**
     * 更新订单通道信息
     * @param sysPayOrderNo
     * @param channelId
     * @param thirdChannelRespMsg
     */
    void updateThirdReturn(String sysPayOrderNo, String channelId, String thirdChannelRespMsg);

    /**
     * 通过商户号和商户订单号获取订单信息
     * @param merchantCode
     * @param merchantOrderNo
     * @return
     */
    ShopPay getByMerchantCodeAndOrderNo(String merchantCode, String merchantOrderNo);

    /**
     * 统计商户昨天的交易数据
     * @return
     */
    ReturnVo dailySummary();

    /**
     * 获取近三十日的每日汇总数据
     * @param headerInfoDto
     * @return
     */
    ReturnVo getDailySummary(HeaderInfoDto headerInfoDto);

    /**
     * 冻结订单
     * @param orderNo
     * @param status
     * @param headerInfoDto
     * @return
     */
    ReturnVo orderFrozen(String orderNo, Integer status, HeaderInfoDto headerInfoDto);

    /**
     * 查询商户每天每条通道的成功数据
     * @param headerInfoDto
     * @return
     */
    ReturnVo getChannelDailySummary(HeaderInfoDto headerInfoDto);
}
