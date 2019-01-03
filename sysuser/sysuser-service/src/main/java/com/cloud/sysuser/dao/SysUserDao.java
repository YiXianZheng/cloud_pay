package com.cloud.sysuser.dao;

import com.cloud.sysconf.common.basePDSC.BaseMybatisDao;
import com.cloud.sysuser.po.SysUser;
import org.apache.ibatis.annotations.Param;

/**
 * @Auther Toney
 * @Date 2018/7/6 10:41
 * @Description:
 */
public interface SysUserDao extends BaseMybatisDao<SysUser, String> {

    /**
     * 通过登陆名和盘口ID查找用户
     * @param loginName
     * @return
     */
    public SysUser findByLoginName(@Param("loginName") String loginName);

    /**
     * 通过token和panid查找用户
     * @param token
     * @return
     */
    SysUser findByToken(@Param("token") String token);

    /**
     * 通过id和panid查找用户
     * @param id
     * @return
     */
    SysUser findById(@Param("id") String id);

    /**
     * 逻辑删除
     * @param sysUser
     */
    void deleteUser(SysUser sysUser);

    /**
     * 判断工号是否存在
     * @param no
     * @return
     */
    int checkExistNo(@Param("no") String no);

    /**
     * 获取某一用户底下所有的商户号 1,2,3,4
     * @param sysUserId
     * @return
     */
    String getMerchantCodes(@Param("id")String sysUserId);

//    /**
//     * 清除失效的token
//     */
//    @Modifying
//    @Query(value = "UPDATE sys_user SET token = NULL, token_update_time = NULL " +
//            " WHERE id IN (SELECT tt.id FROM ( SELECT t.id FROM sys_user t " +
//            "                   WHERE t.token_update_time <= (CURRENT_TIMESTAMP - INTERVAL 30 MINUTE) ) tt)", nativeQuery = true)
//    int cleanFailureToken();
}
