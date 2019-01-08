package com.cloud.apigateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.cloud.apigateway.utils.JwtUtil;
import com.cloud.sysconf.common.utils.LanguageEnum;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * 对一些必要参数的判断和处理
 * @auther Toney
 * @date 2018/7/4 19:16
 */
@Component
public class BaseFilter extends ZuulFilter {

    @Value("${spring.jwt.id}")
    private String jwtid;

    @Value("${sys.panid}")
    private String panId;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        long startTime = System.currentTimeMillis();

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        //加密的token
        String t = request.getHeader("t");
        String h = request.getHeader("h");

        if(!request.getRequestURI().contains("/d8/") && !request.getRequestURI().contains("/api/")
                && !request.getRequestURI().contains("/ebank/") && !request.getRequestURI().contains("/ecash/")
                && !request.getRequestURI().contains("/hctest/") && !request.getRequestURI().contains("/views/")) {
            //h 鉴权
            if (StringUtils.isNotBlank(h)) {
                if (h.equals(panId)) {
                    requestContext.addZuulRequestHeader("h", h);
                } else {
                    requestContext.setSendZuulResponse(false);
                    ApiResponse apiResponse = new ApiResponse(ResponseCode.Parameter.ILLEGAL,
                            ResponseCode.Parameter.ILLEGAL.getExplain(LanguageEnum.zh_CN));
                    requestContext.setResponseBody(JSONObject.toJSON(apiResponse).toString());
                    requestContext.getResponse().setCharacterEncoding("UTF-8");
                    return null;
                }
            } else {
                requestContext.setSendZuulResponse(false);
                ApiResponse apiResponse = new ApiResponse(ResponseCode.Parameter.LACK,
                        ResponseCode.Parameter.LACK.getExplain(LanguageEnum.zh_CN));
                requestContext.setResponseBody(JSONObject.toJSON(apiResponse).toString());
                requestContext.getResponse().setCharacterEncoding("UTF-8");
                return null;
            }
        }

        if(t != null && !"".equals(t) && !"null".equals(t)) {
            //解密token
            String token = JwtUtil.decodeToken(t, jwtid);

            if(StringUtils.isNotBlank(token)) {
                requestContext.addZuulRequestHeader("t1", token);
            }else{
                requestContext.setSendZuulResponse(false);
                ApiResponse apiResponse = new ApiResponse(ResponseCode.LoginRegister.NOLOGIN,
                        ResponseCode.LoginRegister.NOLOGIN.getExplain(LanguageEnum.zh_CN));
                requestContext.setResponseBody(JSONObject.toJSON(apiResponse).toString());
                requestContext.getResponse().setCharacterEncoding("UTF-8");
            }
        }

        long endTime = System.currentTimeMillis();
        logger.info("======== > Base Filter < =========" + (endTime - startTime));

        return null;
    }
}
