package cn.itcast.feign.config;

import cn.itcast.feign.client.fallback.UserClientFallbackFactory;
import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class FeignClientConfiguration {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public UserClientFallbackFactory userClientFallbackFactory() {
        return new UserClientFallbackFactory();
    }
}
