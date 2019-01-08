package com.cloud.apigateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.cloud.apigateway.service.AuthSysuserFilterService;
import com.cloud.apigateway.utils.JwtUtil;
import com.cloud.sysconf.common.dto.HeaderInfoDto;
import com.cloud.sysconf.common.utils.LanguageEnum;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_DECORATION_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * 系统用户鉴权
 * @auther Toney
 * @date 2018/7/4 19:17
 */
@Component
public class AuthSysuserFilter extends ZuulFilter {

    @Value("${spring.jwt.id}")
    private String jwtid;

    @Autowired
    private AuthSysuserFilterService authSysuserFilterService;

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 2;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        if ((request.getRequestURI().contains("/sys/") && !request.getRequestURI().contains("/sys/user/login")
                    && !request.getRequestURI().contains("/sys/user/register"))
                || request.getRequestURI().contains("/agent/user/")
                || request.getRequestURI().contains("/merchant/user/")
                || request.getRequestURI().contains("/finance/")
                || (request.getRequestURI().contains("/recharge/") && !request.getRequestURI().contains("/recharge/api/"))
                || request.getRequestURI().contains("/account/")
                || request.getRequestURI().contains("/hc/")
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

        //加密的token
        String t = request.getHeader("t");

        if(t != null && !"".equals(t) && !"null".equals(t)) {
            //2.系统权限
            String auth = request.getHeader("auth");
            if(StringUtils.isNotBlank(auth) && (HeaderInfoDto.AUTH_PLATFORM_SYSTEM.equals(auth)
                    || HeaderInfoDto.AUTH_AGENT_SYSTEM.equals(auth) || HeaderInfoDto.AUTH_MERCHANT_SYSTEM.equals(auth))){
                requestContext.addZuulRequestHeader("auth", auth);
                //解密token
                String token = JwtUtil.decodeToken(t, jwtid);

                //通过token鉴权
                //1.登陆用户
                Map<String, String> map = authSysuserFilterService.getUserByToken(token);
                if(map != null && StringUtils.isNotBlank(map.get("userId"))){
                    String userId = map.get("userId");
                    String roleId = map.get("roleId");
                    requestContext.addZuulRequestHeader("userId", userId);
                    requestContext.addZuulRequestHeader("roleId", roleId);

                    if(HeaderInfoDto.AUTH_AGENT_SYSTEM.equals(auth)){
                        String agentUser = map.get("agentUser");
                        requestContext.addZuulRequestHeader("agentUser", agentUser);
                    }

                    if(HeaderInfoDto.AUTH_MERCHANT_SYSTEM.equals(auth)){
                        String merUser = map.get("merchantUser");
                        requestContext.addZuulRequestHeader("merchantUser", merUser);
                    }

                    if(StringUtils.isNotBlank(map.get("optStatus")) && !"1".equals(map.get("optStatus"))){
                        requestContext.setSendZuulResponse(false);
                        ApiResponse apiResponse = ApiResponse.creatFail(new ResponseCode.COMMON(ResponseCode.LoginRegister.USER_LOCKED.getCode(),
                                "用户已锁定"));
                        requestContext.setResponseBody(JSONObject.toJSON(apiResponse).toString());
                        requestContext.getResponse().setCharacterEncoding("UTF-8");
                    }
                }
            }else{
                requestContext.setSendZuulResponse(false);
                ApiResponse apiResponse = ApiResponse.creatFail(new ResponseCode.COMMON(ResponseCode.Base.API_ERR.getCode(),
                        "未知请求类型"));
                requestContext.setResponseBody(JSONObject.toJSON(apiResponse).toString());
                requestContext.getResponse().setCharacterEncoding("UTF-8");
            }
        }else{
            requestContext.setSendZuulResponse(false);
            ApiResponse apiResponse = new ApiResponse(ResponseCode.Parameter.LACK,
                    ResponseCode.Parameter.LACK.getExplain(LanguageEnum.zh_CN));
            requestContext.setResponseBody(JSONObject.toJSON(apiResponse).toString());
            requestContext.getResponse().setCharacterEncoding("UTF-8");
        }

        long endTime = System.currentTimeMillis();
        logger.info("======== > AuthSysuser Filter < =========" + (endTime - startTime));

        return null;
    }
}
