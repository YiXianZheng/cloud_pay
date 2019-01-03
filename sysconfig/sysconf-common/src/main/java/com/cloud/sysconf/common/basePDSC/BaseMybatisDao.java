package com.cloud.sysconf.common.basePDSC;

import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/7/19 10:58
 * @Description:
 */
@NoRepositoryBean
public interface BaseMybatisDao<T, ID extends Serializable> {

    /**
     * 分页开始第一条记录索引
     */
    public final static String KEY_PAGE_START = "pageStart";

    /**
     *分页请求记录数
     */
    public final static String KEY_PAGE_SIZE = "pageSize";

    /**
     * 新增对象
     */
    int add(T entity);

    /**
     * 新增对象
     */
    @SuppressWarnings("unchecked")
    int add(Map map);

    /**
     * 批量新增
     * list中参数为实体
     */
    int batchAddEntity(List<T> list);

    /**
     * 批量新增
     * list中参数为map
     */
    @SuppressWarnings("unchecked")
    int batchAddMap(List<Map> list);

    /**
     * 修改对象
     */
    int update(T entity);

    /**
     *  修改对象
     */
    @SuppressWarnings("unchecked")
    int update(Map map);

    /**
     * 删除对象
     */
    int delete(T entity);

    /**
     * 根据id进行删除
     */
    int deleteById(ID id);

    /**
     * 根据ids进行删除
     */
    int deleteByIds(long[] ids);

    /**
     * 根据实体参数查询数目
     */
    int count(T entity);

    /**
     * 根据map参数查询数目
     */
    @SuppressWarnings("unchecked")
    int count(Map map);

    /**
     * 根据id获取对象
     */
    T getById(ID id);

    /**
     * 获取列表数据(实体参数，不分页)
     */
    List<T> list(T entity);

    /**
     * 获取列表数据(map参数，不分页)
     */
    @SuppressWarnings("unchecked")
    List<T> list(Map map);

    /**
     * 分页查询列表(查询条件为实体参数)
     */
    List<T> listPage(T entity);

    /**
     * 分页查询列表(查询条件为map参数)
     */
    @SuppressWarnings("unchecked")
    List<T> listPage(Map map);
}
