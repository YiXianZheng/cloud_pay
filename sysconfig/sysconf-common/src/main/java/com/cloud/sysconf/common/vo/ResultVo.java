package com.cloud.sysconf.common.vo;

import com.cloud.sysconf.common.enums.ResultCode;
import lombok.Data;

/**
 * 服务内部通讯返回专用
 *
 *  注意： 一般用于需要返回Object数据的操作
 * @Auther Toney
 * @Date 2018/7/6 14:43
 * @Description:
 */
@Data
public class ResultVo {

    private String code;

    private String msg;

    private Object obj;

    public ResultVo(){}

    public ResultVo(String code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public ResultVo(String code, String msg, Object obj){
        this.code = code;
        this.msg = msg;
        this.obj = obj;
    }

    public ResultVo(ResultCode resultCode){
        this.code = resultCode.getCode();
        this.msg = resultCode.getValue();
    }
}
