package com.cloud.merchant.service;

import com.cloud.merchant.common.dto.MerchantFormDto;
import com.cloud.merchant.common.dto.MerchantUpdateFormDto;
import com.cloud.merchant.po.MerchantUser;
import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ReturnVo;

/**
 * 商户用户的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface MerchantUserService extends BaseMybatisService<MerchantUser, String> {

    /**
     * 新增或保存商户用户信息
     * @param merchantFormDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo addMerchant(MerchantFormDto merchantFormDto, HeaderInfoDto headerInfoDto);

    /**
     * 分页获取商户商  适用于"点击加载更多"类型的分页
     * @param pageQuery  分页条件
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForPage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     * 分页获取商户商   适用于列表型的分页
     * @param pageQuery  分页条件
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     * 删除商户商
     * @param id
     * @param headerInfoDto
     * @return
     */
    ReturnVo delete(String id, HeaderInfoDto headerInfoDto);


    /**
     * 获取商户商详情
     * @param id
     * @return
     */
    ReturnVo detail(String id);

    /**
     * 通过用户编号获取商户详情
     * @param sysUserId
     * @return
     */
    ReturnVo detailByUserId(String sysUserId);
    /**
     * 冻结解冻商户商操作
     * @param id
     * @param optStatus
     * @param headerInfoDto
     * @return
     */
    ReturnVo optStatus(String id, Integer optStatus, HeaderInfoDto headerInfoDto);

    /**
     * 冻结解冻商户商提现
     * @param id
     * @param cashStatus
     * @param headerInfoDto
     * @return
     */
    ReturnVo cashStatus(String id, Integer cashStatus, HeaderInfoDto headerInfoDto);

    /**
     * 通过商户号初始化商户信息到Redis
     * @param merchantCode
     * @return
     */
    ReturnVo initMerchantToRedis(String merchantCode);

    /**
     * 通过商户号获取商户信息
     * @param code
     * @return
     */
    ReturnVo getByCode(String code);

    /**
     * 获取商户某一接口的费率
     * @param merchantUser
     * @param channelCode
     * @return
     */
    ReturnVo channelRate(String merchantUser, String channelCode);

    /**
     * 冻结或解冻商户API支付权限
     * @param id
     * @param payStatus
     * @param headerInfoDto
     * @return
     */
    ReturnVo payStatus(String id, Integer payStatus, HeaderInfoDto headerInfoDto);

    /**
     * 更新商户信息
     * @param merchantUpdateFormDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo updateMerchant(MerchantUpdateFormDto merchantUpdateFormDto, HeaderInfoDto headerInfoDto);

    /**
     * 更新商户通道配置
     * @param merchantUser
     * @param channelCode
     * @param agentRate
     * @param usable
     * @param headerInfoDto
     * @return
     */
    ReturnVo updateChannelRate(String merchantUser, String channelCode, Double agentRate, Integer usable, HeaderInfoDto headerInfoDto);
}
