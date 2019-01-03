package com.cloud.apigateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.cloud.apigateway.service.MerchantApiFilterService;
//import com.cloud.finance.common.utils.SysPayResultConstants;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * API接口鉴权
 * @auther Toney
 * @date 2018/7/4 19:17
 */
@Component
public class MerchantApiFilter extends ZuulFilter {

    @Value("${spring.jwt.id}")
    private String jwtid;

    @Autowired
    private MerchantApiFilterService merchantApiFilterService;

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
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        if ((request.getRequestURI().contains("/hc/") && !request.getRequestURI().contains("/checkResult"))
                || (request.getRequestURI().contains("/ebank/") && !request.getRequestURI().contains("/queryOrder"))
                ) {
            return true;
        }

        return false;
    }

    @Override
    public Object run() {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        long startTime = System.currentTimeMillis();

        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        ApiResponse authResponse = merchantApiFilterService.checkApi(request);
        if(authResponse != null){
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseBody(JSONObject.toJSON(authResponse).toString());
            requestContext.getResponse().setCharacterEncoding("UTF-8");
        }

        long endTime = System.currentTimeMillis();
        logger.info("======== > MerchantApi Filter < =========" + (endTime - startTime));

        return null;
    }
}
