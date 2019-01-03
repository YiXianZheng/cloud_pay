package com.cloud.sysconf.common.utils.page;
import java.io.Serializable;
import java.util.List;
/**
 * 向前台返回分页结果模型
 * @author toney
 *
 */
public class PageResult implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private int pageSize = 10;//每页条数
	private int pageIndex = 1;//当前页（页数）
	private int lastPage = 1;//上一页（页数）
	private int nextPage = 1;//下一页（页数）
	private int totalCount = 0; //总记录数
	private int totalPage = 0;//总页数
	
	public PageResult(){}
	
	public PageResult(int pageIndex,int pageSize){
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}
	
	@SuppressWarnings("unchecked")
	private List data;//查询结果集合
	
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	public int getLastPage() {
		return lastPage;
	}
	public void setLastPage(int lastPage) {
		this.lastPage = lastPage;
	}
	public int getNextPage() {
		return nextPage;
	}
	public void setNextPage(int nextPage) {
		this.nextPage = nextPage;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	
	@SuppressWarnings("unchecked")
	public List getData() {
		return data;
	}
	@SuppressWarnings("unchecked")
	public void setData(List data) {
		this.data = data;
	}
	
}
