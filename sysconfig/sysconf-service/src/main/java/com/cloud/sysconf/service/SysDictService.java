package com.cloud.sysconf.service;

import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysDictDto;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.po.SysDict;
import com.cloud.sysconf.po.SysMenu;

/**
 * 系统字典的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface SysDictService extends BaseMybatisService<SysDict, String> {

    /**
     * 通过code查询
     * @param code
     * @return
     */
    ReturnVo findByCode(String code);

    /**
     * 刷新系统字典的Redis缓存
     * @return
     */
    ReturnVo refreshRedis();

    /**
     * 分页获取系统字典   适用于列表型的分页
     * @param pageQuery  分页条件
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     * 新增或保存系统字典
     * @param sysDictDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo saveOrUpdate(SysDictDto sysDictDto, HeaderInfoDto headerInfoDto);

    /**
     * 获取系统字典信息
     * @param code
     * @return
     */
    ReturnVo detail(String code);
}
