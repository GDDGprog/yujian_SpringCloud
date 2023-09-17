package cn.itcast.feign.client;

import cn.itcast.feign.config.FeignClientConfiguration;
import cn.itcast.feign.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "userservice",configuration = FeignClientConfiguration.class)
public interface UserClient {

    @GetMapping("/user/{id}")
    User findUserById(@PathVariable("id") Long id);
}
