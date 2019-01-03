package com.cloud.sysconf.service;

import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.dto.SysOfficeDto;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysconf.po.SysOffice;

/**
 * 系统菜单的service
 * @Auther Toney
 * @Date 2018/7/17 20:32
 * @Description:
 */
public interface SysOfficeService extends BaseMybatisService<SysOffice, String> {

    /**
     * 获取组织结构列表数据
     * @param curUserId
     * @return
     */
    ReturnVo getBySysUser(String curUserId);

    /**
     * 新增或保存组织结构信息
     * @param sysOfficeDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo saveOrUpdate(SysOfficeDto sysOfficeDto, HeaderInfoDto headerInfoDto);

    /**
     * 获取组织机构详情
     * @param id
     * @return
     */
    ReturnVo getDetail(String id);

    /**
     * 获取可用组织结构列表数据
     * @return
     */
    ReturnVo getAllUsable();
}
