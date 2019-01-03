package com.cloud.sysuser.service;

import com.cloud.sysconf.common.basePDSC.BaseMybatisService;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ReturnVo;
import com.cloud.sysuser.common.DTO.LoginFormDto;
import com.cloud.sysuser.common.DTO.SysUserFormDto;
import com.cloud.sysuser.common.DTO.UpdataPassword;
import com.cloud.sysuser.po.SysUser;

/**
 * @Auther Toney
 * @Date 2018/7/5 16:13
 * @Description:
 */
public interface SysUserService extends BaseMybatisService<SysUser, String> {

    /**
     * 新增管理账号
     * @param sysUserFormDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo addNewUser(SysUserFormDto sysUserFormDto, HeaderInfoDto headerInfoDto);

    /**
     * 取消新增的管理账号
     * @param sysUserFormDto
     * @return
     */
    ReturnVo addNewUserCancel(SysUserFormDto sysUserFormDto);

    /**
     * 管理账号登陆
     * @param loginFormDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo userLogin(LoginFormDto loginFormDto, HeaderInfoDto headerInfoDto);

    /**
     * 更新管理账号token
     * @param token
     * @return
     */
    ReturnVo updateToken(String token);

    /**
     * 清理失效的token
     * @return
     */
//    ReturnVo cleanFailureToken();

    /**
     * 获取用户信息
     * @param userId
     * @return
     */
    ReturnVo getUserInfo(String userId);

    /**
     * 获取新旧密码修改密码
     * @param updataPassword
     * @param headerInfoDto
     * @return
     */
    ReturnVo updatePassword(UpdataPassword updataPassword, HeaderInfoDto headerInfoDto );

    /**
     * 更新系统账号登录权限
     * @param id
     * @param loginFlag
     * @param curUserId
     * @return
     */
    ReturnVo updateLoginFlag(String id, Integer loginFlag, String curUserId);

    /**
     * 逻辑删除管理账号
     * @param userId
     * @param curUserId
     * @return
     */
    ReturnVo deleteUesr(String userId, String curUserId);

    /**
     * 分页获取系统用户   适用于列表型的分页
     * @param pageQuery  分页条件
     * @param headerInfoDto
     * @return
     */
    ReturnVo listForTablePage(PageQuery pageQuery, HeaderInfoDto headerInfoDto);

    /**
     * 新增或保存管理账号
     * @param sysUserFormDto
     * @param headerInfoDto
     * @return
     */
    ReturnVo saveOrUpdate(SysUserFormDto sysUserFormDto, HeaderInfoDto headerInfoDto);

    /**
     * 获取用户信息
     * @param id
     * @return
     */
    ReturnVo detail(String id);

    /**
     * 获取用户底下所有的商户号
     * @param sysUserId
     * @return
     */
    ReturnVo getMerchantCodes(String sysUserId);
}

