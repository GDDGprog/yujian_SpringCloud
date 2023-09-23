package cn.itcast.hotel;

import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class HotelDocTest {

    @Autowired
    private IHotelService iHotelService;

    private RestHighLevelClient client;

    @Test
    void testAddDocument() throws IOException {
        //根据id查询酒店数据
        Hotel hotel = iHotelService.getById(61083L);
        //转换为文档类型
        HotelDoc hoteloc = new HotelDoc(hotel);
        //1.准备Request对象
        IndexRequest request = new IndexRequest("hotel").id(hoteloc.getId().toString());
        //2.准备Json文档
        request.source(JSON.toJSONString(hoteloc), XContentType.JSON);
        //3.发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetDocumentById() throws IOException {
        //1.准备request
        GetRequest request = new GetRequest("hotel", "61083");
        //2.发送请求,得到响应
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        //3.解析响应结果
        String string = response.getSourceAsString();
        HotelDoc hotelDoc = JSON.parseObject(string, HotelDoc.class);
        System.out.println(hotelDoc);
    }

    @Test
    void testUpdateDocumentById() throws IOException {
        //1.准备request
        UpdateRequest request = new UpdateRequest("hotel", "61083");
        //2.准备请求参数
        request.doc(
                "price","1000",
                "city","广州"
        );
        //3.发送请求
        client.update(request,RequestOptions.DEFAULT);
    }

    @Test
    void testDeleteDocumentById() throws IOException {
        //1.准备request
        DeleteRequest request = new DeleteRequest("hotel","61083");
        //发送请求
        client.delete(request,RequestOptions.DEFAULT);
    }

    //批量添加数据
    @Test
    void testBulkRequest() throws IOException {
        //批量查询酒店数据
        List<Hotel> hotels = iHotelService.list();

        //1.创建Request
        BulkRequest request = new BulkRequest();
        //2.准备参数,添加多个新的Request
        //转换为文档类型HotelDoc
        for (Hotel hotel : hotels) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            request.add(new IndexRequest("hotel")
                    .id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc), XContentType.JSON));
        }
        //3.发送请求
        client.bulk(request,RequestOptions.DEFAULT);
    }

    @Test
    void testInit(){
        System.out.println(client);
    }

    @BeforeEach
    void setUp(){
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.200.131:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }
}
