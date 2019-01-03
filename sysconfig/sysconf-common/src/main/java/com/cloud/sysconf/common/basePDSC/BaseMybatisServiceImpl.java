package com.cloud.sysconf.common.basePDSC;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import javax.annotation.PostConstruct;

import com.cloud.sysconf.common.utils.DateUtil;
import com.cloud.sysconf.common.utils.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 服务基类实现类
 */
public abstract class BaseMybatisServiceImpl<T, ID extends Serializable,X extends BaseMybatisDao<T, ID>> 
					implements BaseMybatisService<T, ID>,ApplicationContextAware {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	//@Autowired 相当于注入BaseMybatisDao,导致多个实现类，异常
	private X mybatisDao;
	/** 实体类类型 */
	private Class<X> daoClass;
	/** spring上下文 */
	private ApplicationContext context;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		context = applicationContext;
	}
	
	@SuppressWarnings("unchecked")
	public BaseMybatisServiceImpl() {
		Type type = getClass().getGenericSuperclass();
		if (ParameterizedType.class.isAssignableFrom(type.getClass())){
			Type[] parameterizedType = ((ParameterizedType) type).getActualTypeArguments();
			daoClass = (Class<X>) parameterizedType[2];
		}
	}
	
	@SuppressWarnings("unused")
	@PostConstruct
	private void initDao(){
		//获取实际的dao对象
		mybatisDao = context.getBean(daoClass);
	}
	
	public X getMybatisDao(){
		return mybatisDao;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void add(Map map) {
		getMybatisDao().add(map);
	}

	@Override
	public T add(T entity) {
		getMybatisDao().add(entity);
		return entity;
	}

	@Override
	public void batchAddEntity(List<T> list) throws Exception {
		getMybatisDao().batchAddEntity(list);
	}

	@Override
	public void batchAddMap(List<Map> list) throws Exception {
		getMybatisDao().batchAddMap(list);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Map map) {
		getMybatisDao().update(map);
	}

	@Override
	public T update(T entity) {
		getMybatisDao().update(entity);
		return entity;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int count(Map map) {
		return getMybatisDao().count(map);
	}

	@Override
	public int count(T entity) {
		return getMybatisDao().count(entity);
	}

	@Override
	public void delete(T entity) {
		getMybatisDao().delete(entity);
	}

	@Override
	public void deleteById(ID id) {
		getMybatisDao().deleteById(id);
	}

	@Override
	public void deleteByIds(String ids) throws Exception {
		long[] idsArray = {};
		String[] idsTemp = ids.split(",");
		if(idsTemp!=null && idsTemp.length>0){
			idsArray = new long[idsTemp.length];
			for(int i=0;i<idsTemp.length;i++){
				idsArray[i] = Integer.parseInt(idsTemp[i]);
			}
		}
		getMybatisDao().deleteByIds(idsArray);
	}

	@Override
	public T getById(ID id) {
		return getMybatisDao().getById(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> queryForList(Map map) {
		return getMybatisDao().list(map);
	}

	@Override
	public List<T> queryForList(T entity) {
		return getMybatisDao().list(entity);
	}

	@Override
	public List<T> queryForPage(int pageIndex, int pageSize,
			Map<String, Object> map) {
		int firstResult = (pageIndex - 1) * pageSize;
		if (map == null) {
			map = new HashMap<String,Object>();
		}
		map.put(BaseMybatisDao.KEY_PAGE_START, firstResult);
		map.put(BaseMybatisDao.KEY_PAGE_SIZE, pageSize);
		return getMybatisDao().listPage(map);
	}

	@Override
	public PageResult queryForTablePage(int pageIndex, int pageSize,
								   Map<String, Object> param) {
		PageResult pr = new PageResult();

		int totalPage = 0;
		int lastPage = 1;
		int nextPage = 1;
        if(param != null && param.get("beginTime") != null && param.get("endTime") != null){
            Date beginTime = DateUtil.StringToDate(param.get("beginTime").toString(), DateUtil.DATE_PATTERN_02);
            Date endTime = DateUtil.StringToDate(param.get("endTime").toString(), DateUtil.DATE_PATTERN_02);

            if(beginTime != null){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(beginTime);
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE), 0, 0, 0);

                beginTime = calendar.getTime();
            }
            param.put("beginTime", DateUtil.DateToString(beginTime, DateUtil.DATE_PATTERN_01));


            if(endTime != null){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(endTime);
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DATE), 23, 59, 59);

                endTime = calendar.getTime();
            }
            param.put("endTime", DateUtil.DateToString(endTime, DateUtil.DATE_PATTERN_01));
        }
		int totalCount = this.count(param);
		if(totalCount%pageSize==0){
			totalPage = totalCount/pageSize;
		}else{
			totalPage = totalCount/pageSize+1;
		}
		List list = this.queryForPage(pageIndex, pageSize, param);

		if(totalPage>pageIndex){
			nextPage = pageIndex + 1;
		}else{
			nextPage = pageIndex;
		}
		if(pageIndex>1){
			lastPage = pageIndex - 1;
		}else{
			lastPage = pageIndex;
		}

		pr.setData(list);
		pr.setPageSize(pageSize);
		pr.setPageIndex(pageIndex);
		pr.setTotalPage(totalPage);
		pr.setTotalCount(totalCount);
		pr.setLastPage(lastPage);
		pr.setNextPage(nextPage);
		return pr;
	}

}
