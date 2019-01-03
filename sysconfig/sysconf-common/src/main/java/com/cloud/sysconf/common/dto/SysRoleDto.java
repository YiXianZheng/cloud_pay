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
public class SysRoleDto extends BaseDto {

    private String id;

    private String name;        //角色名

    private String enname;      //英文名

    private String officeId;    //组织机构ID

    private String officeInfo;  //组织结构信息

    private String roleType;    //角色类型

    private Integer usable;      //是否可用

    ////////////////////
    private List<String> checkedKeys = new ArrayList<>(); //菜单选中的节点，注意若传根结点则所有子节点均选中

    private String usableStr;

    public String getUsableStr(){
        if(1 == this.usable){
            return "可用";
        }else if(2 == this.usable){
            return "不可用";
        }
        return "";
    }

}
