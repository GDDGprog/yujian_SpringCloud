package cn.itcast.hotel.mq;

import cn.itcast.hotel.constants.MqConstants;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HotelListener {

    @Autowired
    private IHotelService hotelService;

    /**
     * 监听酒店新增或修改业务
     * @param id 酒店id
     */
    @RabbitListener(queues=MqConstants.HOTEL_INSERT_QUEUE)
    public void listenHotelInsert(Long id){
        hotelService.insertOrUpdateById(id);
    }

    /**
     * 监听酒店删除业务
     * @param id 酒店id
     */
    @RabbitListener(queues=MqConstants.HOTEL_DELET_QUEUE)
    public void listenHotelDelete(Long id){
        hotelService.deleteById(id);
    }
}
