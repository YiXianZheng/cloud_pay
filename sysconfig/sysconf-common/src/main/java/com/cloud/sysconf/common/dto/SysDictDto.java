package com.cloud.sysconf.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

/**
 * @Auther Toney
 * @Date 2018/7/26 19:33
 * @Description:
 */
@Data
public class SysDictDto extends BaseDto {

    public static final int IS_ADD_YES = 1;
    public static final int IS_ADD_NO = 2;

    private String code;        //字典编码

    private String value;       //数据值

    private String label;       //字典名称

    private String description; //字典描述

    private int sort;           //排序

    private Integer isAdd;      //是否是新增的数据 1 是  2 否
}
