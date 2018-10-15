package com.shuigee.springcloud.gray.feign.ribbon;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.shuigee.springcloud.gray.CoreHeaderInterceptor;
import feign.Client;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class GrayFeignClient implements Client {
    private static final Logger logger = LoggerFactory.getLogger(GrayFeignClient.class);

    private Client delegate;

    public GrayFeignClient(Client delegate) {
        this.delegate = delegate;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {

        String header = StringUtils.collectionToDelimitedString(CoreHeaderInterceptor.label.get(), CoreHeaderInterceptor.HEADER_LABEL_SPLIT);
        logger.info("服务调用(F) -- {}, {}, label: {}", request.method(), request.url(), header);

        // TODO shirorealm
        if (!HystrixRequestContext.isCurrentThreadInitialized()) {
            HystrixRequestContext.initializeContext();
        }
        try {
            Map<String, Collection<String>> headers = new HashMap<>(request.headers());
            headers.put(CoreHeaderInterceptor.HEADER_LABEL, CoreHeaderInterceptor.label.get());
            Request modifiedRequest = Request.create(request.method(), request.url(), headers, request.body(), request.charset());

            return delegate.execute(modifiedRequest, options);
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw e;
        } finally {
        }

    }
}
