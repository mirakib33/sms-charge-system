package com.momagic.sms.charge.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class Config {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

//    @Bean(name = "taskExecutor")
//    public Executor taskExecutor() {
//        return Executors.newVirtualThreadPerTaskExecutor();
//    }

}
