package com.shuigee.springcloud.gray.feign;

import com.shuigee.springcloud.gray.feign.ribbon.GrayFeignClient;
import feign.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrayFeignClientsConfiguration {

    @Autowired
    private Client feignClient;

    @Bean
    public Client client() {
        return new GrayFeignClient(feignClient);
    }

}