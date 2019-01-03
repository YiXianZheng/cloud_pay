package com.cloud.sysconf.common.basePDSC;

import com.alibaba.fastjson.JSONObject;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.common.vo.ReturnVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Auther Toney
 * @Date 2018/7/19 10:59
 * @Description:
 */
public class BaseController {

    @Autowired
    protected HttpServletRequest request;

    /**
     * 日志对象
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 获取session对象
     * @return
     */
    protected HttpSession getSession(HttpServletRequest request){
        HttpSession session =request.getSession();
        return session;
    }

    /**
     * 获取请求头中的数据
     * @param headers
     * @return
     */
    protected HeaderInfoDto getHeaderInfo(@RequestHeader HttpHeaders headers){
        String userId = headers.getFirst("userId");
        String panId = headers.getFirst("h");
        String token = headers.getFirst("t1");
        String auth = headers.getFirst("auth");
        String roleId = headers.getFirst("roleId");
        String agentUser = headers.getFirst("agentUser");
        String merchantUser = headers.getFirst("merchantUser");

        HeaderInfoDto headerInfo = new HeaderInfoDto();
        headerInfo.setCurUserId(userId);
        headerInfo.setPanId(panId);
        headerInfo.setToken(token);
        headerInfo.setAuth(auth);
        headerInfo.setRoleId(roleId);
        headerInfo.setAgentUser(agentUser);
        headerInfo.setMerchantUser(merchantUser);

        return headerInfo;
    }

    /**
     * returnVo 转成 apiResponse
     * @param returnVo
     * @return
     */
    protected ApiResponse toApiResponse(ReturnVo returnVo){
        if(returnVo.code == ReturnVo.SUCCESS){
            Object object = null;
            if(returnVo.object != null){
                object = returnVo.object;
            }
            return ApiResponse.creatSuccess(object);
        }else if(returnVo.code == ReturnVo.FAIL){
            return ApiResponse.creatFail(returnVo.responseCode);
        }else{
            return ApiResponse.creatFail(ResponseCode.Base.ERROR);
        }
    }


    /*
     * 获取中文参数结果
     */
    public String getZhParameter(String paramName){
        String paramValue=	this.request.getParameter(paramName);
        if( StringUtils.isNotBlank( paramValue )){
            try {
                paramValue = new String(new String(paramValue.getBytes("ISO-8859-1"),"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage());
            }
        }else{
            return paramValue;
        }
        return paramValue;
    }

    /*
     *获得非空double类型参数
     */
    public Double getDoubleParameter(String paramName){
        String paramValue=	this.request.getParameter(paramName);
        Double returnDouble = 0.000D;
        if( StringUtils.isNotBlank( paramValue )){
            returnDouble = (double) (Float.valueOf(paramValue));
        }else{
            returnDouble = 0.000D;
        }
        return returnDouble;
    }
    /*
     *获得非空字符串
     */
    public String getStringParameter(String paramName){
        String paramValue=	this.request.getParameter(paramName);
        String returnString = "";
        if( StringUtils.isNotBlank( paramValue )){
            returnString = paramValue;
        }else{
            returnString = "";
        }
        return returnString;
    }

    /**
     * 客户端返回字符串
     * @param response
     * @param message
     * @param type
     * @return
     */
    protected String renderString(HttpServletResponse response, String message, String type) {
        try {
            response.reset();
            response.setContentType(type);
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(message);
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 客户端返回JSON字符串
     * @param response
     * @param object
     * @return
     */
    protected String renderString(HttpServletResponse response, Object object) {
        return renderString(response, JSONObject.toJSON(object).toString(), "application/json");
    }


    /**
     * 客户端返回失败JSON字符串
     * @param response
     * @param result
     * @param code
     * @param msg
     * @return
     */
    protected String renderFailString(HttpServletResponse response, boolean result, int code, String msg) {
        Map<String, Object> mp =  new HashMap<String, Object>();
        mp.put("success", result);
        mp.put("code", code);
        mp.put("message", msg);
        return renderString(response, JSONObject.toJSON(mp).toString(), "application/json");
    }

    public String printAcceptValue(HttpServletRequest request){
        String acceptLog = "["+request.getServletPath()+"][accept params:]";
        String returnValue = "";
        Map map =request.getParameterMap();
        Iterator it = map.keySet().iterator();
        while(it.hasNext()){
            String key;
            String value;
            String[] vls;
            key=it.next().toString();
            vls=(String[])map.get(key);
            acceptLog = acceptLog +key+":"+vls[0]+",";
            returnValue = returnValue+key;
        }
        this.logger.info(acceptLog);
        return returnValue;
    }

}
