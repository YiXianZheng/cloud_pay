package com.cloud.sysconf.common.dto;

import com.cloud.sysconf.common.basePDSC.BaseDto;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Auther Toney
 * @Date 2018/7/26 19:33
 * @Description:
 */
@Data
public class SysMenuDto extends BaseDto {

    private String id;

    private String parentId;    //父级ID

    private String name;        //名称

    private String path;        //链接

    private String icon;        //图标

    private List<SysMenuDto> children = new ArrayList<>();

    //////////////////

    private String key;
}
