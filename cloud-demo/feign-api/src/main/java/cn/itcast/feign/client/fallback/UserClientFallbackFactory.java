package cn.itcast.feign.client.fallback;

import cn.itcast.feign.client.UserClient;
import cn.itcast.feign.pojo.User;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
    @Override
    public UserClient create(Throwable throwable) {
        return new UserClient(){
            @Override
            public User findUserById(Long id) {
                log.error("查询用户失败, throwable:{}",throwable);
                return new User();
            }
        };
    }
}
