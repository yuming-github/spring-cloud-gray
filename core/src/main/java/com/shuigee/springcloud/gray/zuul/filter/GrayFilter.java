package com.shuigee.springcloud.gray.zuul.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.shuigee.springcloud.gray.CoreHeaderInterceptor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public class GrayFilter extends ZuulFilter {
    private static final HashMap<String, String> TOKEN_LABEL_MAP = new HashMap<>();
    private static final String DEFAULT_LABEL = "v1.0";

    static {
        TOKEN_LABEL_MAP.put("v1", DEFAULT_LABEL);
        TOKEN_LABEL_MAP.put("v1t", DEFAULT_LABEL + "Test");
        TOKEN_LABEL_MAP.put("v2", "v2.0");
        TOKEN_LABEL_MAP.put("v2t", "v2.0Test");
        TOKEN_LABEL_MAP.put("v3", "v3.0");
        TOKEN_LABEL_MAP.put("v3t", "v3.0Test");
    }


    private static final Logger logger = LoggerFactory.getLogger(GrayFilter.class);

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        String labels = TOKEN_LABEL_MAP.get(token);

        if (StringUtils.isEmpty(labels)) {
            labels = DEFAULT_LABEL;
        }

        CoreHeaderInterceptor.initHystrixRequestContext(labels); // zuul本身调用微服务
        ctx.addZuulRequestHeader(CoreHeaderInterceptor.HEADER_LABEL, labels); // 传递给后续微服务
        logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");

        return null;
    }
}
