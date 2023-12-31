package cn.itcast.order.service;
import cn.itcast.feign.client.UserClient;
import cn.itcast.feign.pojo.User;
import cn.itcast.order.mapper.OrderMapper;
import cn.itcast.order.pojo.Order;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserClient userClient;

    public Order queryOrderById(Long orderId) {
        // 1.查询订单
        Order order = orderMapper.findById(orderId);

        //2.利用Feign发起http请求,查询用户
        User user = userClient.findUserById(order.getUserId());
        //3. 封装User到Order中
        order.setUser(user);
        // 4.返回
        return order;
    }

    @SentinelResource("goods")
    public void queryGoods(){
        System.out.println("查询商品");
    }

    //@Autowired
    //private RestTemplate restTemplate;

    //public Order queryOrderById(Long orderId) {
    //    // 1.查询订单
    //    Order order = orderMapper.findById(orderId);
    //
    //    //2. 利用RestTemplate发送http请求,查询用户信息
    //    //2.1 url路径
    //    String url = "http://userservice/user/" + order.getUserId();
    //    //2.2 发送请求
    //    /**
    //     * 如果发起的请求是get,就用getForObject
    //     * 如果是post,就用postForObject
    //     *
    //     * getForObject中的参数,参数1:请求的url,参数2:返回的对象
    //     */
    //    //2.3 发起http请求,实现远程调用
    //    User user = restTemplate.getForObject(url, User.class);//order-service 存在 User.class
    //    //3. 封装User到Order中
    //    order.setUser(user);
    //    // 4.返回
    //    return order;
    //}
}
