package com.cloud.apigateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

import java.util.UUID;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.SEND_RESPONSE_FILTER_ORDER;

/**
 * 请求完成后往请求头插入某些内容
 * @auther Toney
 * @date 2018/7/4 19:17
 */
@Component
public class addResponseHeaderFilter extends ZuulFilter{
    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_RESPONSE_FILTER_ORDER - 1;
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
        HttpServletResponse response = requestContext.getResponse();
        response.setHeader("X-Foo", UUID.randomUUID().toString());

        long endTime = System.currentTimeMillis();
        logger.info("======== > addResponse Filter < =========" + (endTime - startTime));

        return null;
    }
}
