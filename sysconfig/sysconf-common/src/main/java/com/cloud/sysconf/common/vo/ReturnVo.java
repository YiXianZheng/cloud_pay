package com.cloud.sysconf.common.vo;


import com.cloud.sysconf.common.utils.ResponseCode;


/**
 * 服务内部通讯返回专用，接口请用ApiResponse
 *
 *  注意： 一般用于结果无需额外操作的service，调用方只关心操作的结果
 */
public class ReturnVo {

    public static final int SUCCESS = 1; //预期请求结果
    public static final int FAIL = 0;    //非预期请求结果，可能是失败也可能是没有更改或者记录
    public static final int ERROR = -1;  //请求异常

    public Integer code;
    public ResponseCode responseCode;
    public Object object;

    public static ReturnVo returnSuccess(){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = SUCCESS;
        return returnVo;
    }

    public static ReturnVo returnSuccess(ResponseCode responseCode){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = SUCCESS;
        returnVo.responseCode = responseCode;
        return returnVo;
    }


    public static ReturnVo returnSuccess(Object object){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = SUCCESS;
        returnVo.object = object;
        return returnVo;
    }

    public static ReturnVo returnSuccess(ResponseCode responseCode, Object object){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = SUCCESS;
        returnVo.responseCode = responseCode;
        returnVo.object = object;
        return returnVo;
    }

    public static ReturnVo returnFail(){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = FAIL;
        return returnVo;
    }

    public static ReturnVo returnFail(ResponseCode responseCode){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = FAIL;
        returnVo.responseCode = responseCode;
        return returnVo;
    }

    public static ReturnVo returnFail(ResponseCode responseCode, Object object){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = FAIL;
        returnVo.responseCode = responseCode;
        returnVo.object = object;
        return returnVo;
    }

    public static ReturnVo returnError(){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ERROR;
        return returnVo;
    }

    public static ReturnVo returnError(ResponseCode responseCode){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ERROR;
        returnVo.responseCode = responseCode;
        return returnVo;
    }

    public static ReturnVo returnError(ResponseCode responseCode, Object object){
        ReturnVo returnVo = new ReturnVo();
        returnVo.code = ERROR;
        returnVo.responseCode = responseCode;
        returnVo.object = object;
        return returnVo;
    }
}
