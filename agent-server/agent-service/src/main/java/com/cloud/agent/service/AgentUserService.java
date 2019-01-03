package com.cloud.agent.service;

import com.cloud.agent.common.dto.AgentFormDto;
import com.cloud.agent.po.AgentUser;
import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ReturnVo;

/**
 * 代理用户的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface AgentUserService extends BaseMybatisService<AgentUser, String> {

    /**
     * 新增或保存代理用户信息
     * @param agentFormDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo addOrUpdate(AgentFormDto agentFormDto, HeaderInfoDto headerInfoDto);

    /**
     * 分页获取代理商  适用于"点击加载更多"类型的分页
     * @param pageQuery  分页条件
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForPage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     * 分页获取代理商   适用于列表型的分页
     * @param pageQuery  分页条件
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     * 删除代理商
     * @param id
     * @param headerInfoDto
     * @return
     */
    ReturnVo delete(String id, HeaderInfoDto headerInfoDto);

    /**
     * 审核代理商
     * @param id
     * @param authStatus
     * @param headerInfoDto
     * @return
     */
    ReturnVo auth(String id, Integer authStatus, HeaderInfoDto headerInfoDto);

    /**
     * 获取代理商详情
     * @param id
     * @return
     */
    ReturnVo detail(String id);

    /**
     * 冻结解冻代理商操作
     * @param id
     * @param optStatus
     * @param headerInfoDto
     * @return
     */
    ReturnVo optStatus(String id, Integer optStatus, HeaderInfoDto headerInfoDto);

    /**
     * 冻结解冻代理商提现
     * @param id
     * @param cashStatus
     * @param headerInfoDto
     * @return
     */
    ReturnVo cashStatus(String id, Integer cashStatus, HeaderInfoDto headerInfoDto);

    /**
     * 获取可选的商户列表
     * @return
     */
    ReturnVo getActiveAgent();

    /**
     * 通过代理号初始化代理信息到Redis
     * @param agentCode
     * @return
     */
    ReturnVo initAgentToRedis(String agentCode);

    /**
     * 通过代理号获取代理信息
     * @param code
     * @return
     */
    ReturnVo getByCode(String code);

    /**
     * 获取代理某一接口的费率
     * @param agentCode
     * @param channelCode
     * @return
     */
    ReturnVo channelRate(String agentCode, String channelCode);
}
