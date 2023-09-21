package cn.itcast.mq.listener;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Map;

@Component
public class SpringRabbitListener {

    //@RabbitListener(queues = "simple.queue")
    //public void lisntenSimpleQueue(String msg){
    //    System.out.println("消费者接收到simple.queue的消息:["+ msg +"]");
    //}

    @RabbitListener(queues = "simple.queue")
    public void lisntenWorkQueue1(String msg) throws InterruptedException {
        System.out.println("消费者1接收到simple.queue的消息:["+ msg +"]"+ LocalTime.now());
        Thread.sleep(20);
    }

    @RabbitListener(queues = "simple.queue")
    public void lisntenWorkQueue2(String msg) throws InterruptedException {
        System.err.println("消费者2接收到simple.queue的消息:["+ msg +"]"+LocalTime.now());
        Thread.sleep(200);
    }

    @RabbitListener(queues = "fanout.queue1")
    public void lisntenFanoutQueue1(String msg){
        System.out.println("消费者接收到fanout1.queue的消息:["+ msg +"]");
    }

    @RabbitListener(queues = "fanout.queue2")
    public void lisntenFanoutQueue2(String msg){
        System.out.println("消费者接收到fanou2.queue的消息:["+ msg +"]");
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "direct.queue1"),
            exchange = @Exchange(name = "itcast.direct",type = "direct"),
            key = {"red","blue"}
    ))
    public void listenDirectQueue1(String msg){
        System.out.println("消费者接收到direct.queue1的消息:["+ msg +"]");
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "direct.queue2"),
            exchange = @Exchange(name = "itcast.direct",type = ExchangeTypes.DIRECT),
            key = {"red","yellow"}
    ))
    public void listenDirectQueue2(String msg){
        System.out.println("消费者接收到direct.queue2的消息:["+ msg +"]");
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "topic.queue1"),
            exchange = @Exchange(name = "itcast.topic",type = ExchangeTypes.TOPIC),
            key = "china.#"
    ))
    public void listenTopicQueue1(String msg){
        System.out.println("消费者接收到topic.queue1的消息:["+ msg +"]");
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "topic.queue2"),
            exchange = @Exchange(name = "itcast.topic",type = ExchangeTypes.TOPIC),
            key = "#.news"
    ))
    public void listenTopicQueue2(String msg){
        System.out.println("消费者接收到topic.queue2的消息:["+ msg +"]");
    }

    @RabbitListener(queues = "object.queue")
    public void listenObjectQueue(Map<String,Object> msg){
        System.out.println("消费者接收到object.queue的消息:["+ msg +"]");
    }
}
