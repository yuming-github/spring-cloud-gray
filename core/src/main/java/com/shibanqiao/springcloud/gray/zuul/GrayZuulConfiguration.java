package com.shibanqiao.springcloud.gray.zuul;

import com.netflix.zuul.http.ZuulServlet;
import com.shibanqiao.springcloud.gray.zuul.filter.GrayFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(value = ZuulServlet.class)
public class GrayZuulConfiguration {

    @Bean
    public GrayFilter grayFilter() {
        return new GrayFilter();
    }
}
