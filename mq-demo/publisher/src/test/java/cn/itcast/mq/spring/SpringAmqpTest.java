package cn.itcast.mq.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAmqpTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void name() {
        String qeueeName = "simple.queue";
        String message = "hello,spring amqp123";
        rabbitTemplate.convertAndSend(qeueeName, message);
    }

    @Test
    public void testWorkQueue() throws InterruptedException {
        String qeueeName = "simple.queue";
        String message = "hello,spring amqp____";
        for (int i = 1; i <= 50; i++) {
            //发送消息
            rabbitTemplate.convertAndSend(qeueeName, message+i);
            //避免发送太快
            Thread.sleep(20);
        }
    }

    @Test
    public void testSendFanoutExchange() {
        //交换机名称
        String exchangeName = "itcast.fanout";
        //消息
        String message = "hello,everyone";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName, "", message);
    }

    @Test
    public void testSendDirectExchange() {
        //交换机名称
        String exchangeName = "itcast.direct";
        //消息
        String message = "hello,yellow";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName, "yellow", message);
    }

    @Test
    public void testSendTopicExchange() {
        //交换机名称
        String exchangeName = "itcast.topic";
        //消息
        String message = "yujian可以找到一份好的实习111";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName, "china.weather", message);
    }

    @Test
    public void testSendObjectExchange() {
        Map<String,Object> msg = new HashMap<>();
        msg.put("name","yujian");
        msg.put("age",18);
        rabbitTemplate.convertAndSend("object.queue",msg);
    }
}
