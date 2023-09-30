package cn.itcast.user.web;

import cn.itcast.user.config.PatternProperties;
import cn.itcast.user.pojo.User;
import cn.itcast.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
//@RefreshScope
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //注入nacos的配置属性
    //@Value("${pattern.dateformate}")
    //private String dateformate;

    @Autowired
    private PatternProperties patternProperties;

    @GetMapping("prop")
    public PatternProperties patternProperties(){
        return patternProperties;
    }

    // 编写controller，通过日期格式化器来格式化现在时间并返回
    @GetMapping("now")
    public String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(patternProperties.getDateformate()));
    }

    /**
     * 路径： /user/110
     *
     * @param id 用户id
     * @return 用户
     */
    @GetMapping("/{id}")
    public User queryById(@PathVariable("id") Long id,
                          @RequestHeader(value = "Truth",required = false) String truth) throws InterruptedException {
        if (id == 1){
            //休眠,触发熔断
            Thread.sleep(60);
        }
        return userService.queryById(id);
    }
}
