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
public class SysOfficeDto extends BaseDto {

    private String key;        //前段tree类型表格需要的字段，不能重复

    private String id;

    private String parentId;    //父级ID

    private String name;        //组织名称

    private Integer type;       //机构类型 1: 公司  2：部门

    private String address;     //联系地址

    private String master;      //负责人

    private String phone;       //联系电话

    private String fax;         //传真

    private String email;       //邮箱

    private Integer usable;     //是否可用

    private int sort;           //排序

    private List<SysOfficeDto> children = new ArrayList<>();

    ///////////前端显示结果

    private String typeStr;    //组织机构名称

    private String usableStr;

    public String getTypeStr(){
        if(1 == this.type){
            return "公司";
        }else if(2 == this.type){
            return "部门";
        }else{
            return "其它";
        }
    }

    public String getUsableStr(){
        if(1 == this.type){
            return "可用";
        }else if(2 == this.type){
            return "不可用";
        }else{
            return "";
        }
    }
}
