package com.cloud.sysconf.common.basePDSC;

import com.cloud.sysconf.common.utils.page.PageResult;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 基础服务接口
 */
public interface BaseMybatisService<T, ID extends Serializable> {
	
	/**
	 * 新增对象
	 */
	T add(T entity) throws Exception;
	
	/**
	 * 新增对象
	 */
	@SuppressWarnings("unchecked")
	void add(Map map) throws Exception;
	
	/**
	 * 批量新增
	 * list中参数为实体
	 */
	void batchAddEntity(List<T> list) throws Exception;
	
	/**
	 * 批量新增
	 * list中参数为map
	 */
	@SuppressWarnings("unchecked")
	void batchAddMap(List<Map> list) throws Exception;
	
	/**
	 * 修改对象
	 */
	T update(T entity) throws Exception;
	
	/**
	 *  修改对象
	 */
	@SuppressWarnings("unchecked")
	void update(Map map) throws Exception;
	
	/**
	 * 删除对象
	 */
	void delete(T entity) throws Exception;

	/**
	 * 根据id进行删除
	 */
	void deleteById(ID id) throws Exception;
	
	/**
	 * 根据ids进行删除
	 */
	void deleteByIds(String ids) throws Exception;
	
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
	 * 查询（不分页）
	 * entity 查询条件为实体参数
	 */
	List<T> queryForList(T entity);
	
	/**
	 * 查询（不分页）
	 * map 查询条件为map参数
	 */
	@SuppressWarnings("unchecked")
	List<T> queryForList(Map map);
	
	/**
	 * 分页查询列表  其他分页形式
	 * pageIndex 页数
	 * pageSize 每页显示的条数
	 * map 查询参数为map
	 */
	List<T> queryForPage(int pageIndex, int pageSize, Map<String, Object> map);

	/**
	 * 分页查询列表  适用于列表
	 * pageIndex 页数
	 * pageSize 每页显示的条数
	 * map 查询参数为map
	 */
	PageResult queryForTablePage(int pageIndex, int pageSize, Map<String, Object> map);
	
}
