package com.shibanqiao.springcloud.gray.feign;

import com.netflix.loadbalancer.ILoadBalancer;
import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@ConditionalOnClass({ILoadBalancer.class, Feign.class})
@Configuration
@EnableFeignClients(defaultConfiguration = {GrayFeignClientsConfiguration.class})
public class GrayFeignConfiguration {

}