package com.cloud.sysconf.common.utils.page;

import lombok.Data;

import java.util.Map;

/**
 * 分页参数
 * @Auther Toney
 * @Date 2018/8/4 11:00
 * @Description:
 */
@Data
public class PageQuery {

    /**
     * 分页页码
     */
    private int pageIndex = 1;

    /**
     * 每页条数
     */
    private int pageSize = 10;

    /**
     * 查询条件
     */
    private Map<String, Object> params;
}
