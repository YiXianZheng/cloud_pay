package com.cloud.sysconf.common.basePDSC;

import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;

/**
 * @Auther Toney
 * @Date 2018/7/6 10:14
 * @Description:
 */
@MappedSuperclass
@Data
public class BasePo implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String DEL_FLAG_COMMON = "0";
    public static final String DEL_FLAG_ALREADY = "1";
    public static final String DEL_FLAG_AUTH = "2";

    protected String remarks;   //备注

    protected String createBy;  //创建者

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:SS")
    protected Date createDate; //创建日期

    protected String updateBy;  //更新者

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:SS")
    protected Date updateDate;  //更新日期

    protected String delFlag;  //删除标记（0：正常  1：删除  2：审核）

    protected String panId;


    /**
     * 插入之前执行方法，需要手动调用
     */
    public void preInsert(String currUser, String panId){
        if (StringUtils.isNotBlank(currUser)){
            this.updateBy = currUser;
            this.createBy = currUser;
        }
        this.updateDate = new Date();
        this.createDate = this.updateDate;
        this.panId = panId;
        this.delFlag = DEL_FLAG_COMMON;
    }

    /**
     * 更新之前执行方法，需要手动调用
     */
    public void preUpdate(String currUser){
        if (StringUtils.isNotBlank(currUser)){
            this.updateBy = currUser;
        }
        this.updateDate = new Date();
    }
}
