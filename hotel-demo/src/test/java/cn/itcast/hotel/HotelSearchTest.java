package cn.itcast.hotel;

import cn.itcast.hotel.pojo.HotelDoc;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;

import static cn.itcast.hotel.constants.HotelConstants.MAPPING_TEMPLATE;

public class HotelSearchTest {

    private RestHighLevelClient client;

    //插叙所有数据
    @Test
    void testMatchAll() throws IOException {
        //1.准备Request请求
        SearchRequest request = new SearchRequest("hotel");
        //2.准备DSL
        request.source().query(QueryBuilders.matchAllQuery());
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //调用自定义方法进行结果响应
        responseResult(response);
    }

    //插叙包含如家的数据
    @Test
    void testMatch() throws IOException {
        //1.准备Request请求
        SearchRequest request = new SearchRequest("hotel");
        //2.准备DSL
        request.source().query(QueryBuilders.matchQuery("all","如家"));
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //调用自定义方法进行结果响应
        responseResult(response);
    }

    //测试复合语句boolean query
    @Test
    void testBool() throws IOException {
        //1.准备Request请求
        SearchRequest request = new SearchRequest("hotel");
        //2.准备DSL
        //2.1准备BooleanQuery
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //2.2准备MustQuery(添加term)
        boolQuery.must(QueryBuilders.termQuery("city","北京"))
                        .filter(QueryBuilders.rangeQuery("price").gte(100).lte(200));
        request.source().query(boolQuery);
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //调用自定义方法进行结果响应
        responseResult(response);
    }

    //分页查询
    @Test
    void testPageAndSort() throws IOException {
        //页面以及每页大小
        int page = 1,size = 5;
        //1.准备Request请求
        SearchRequest request = new SearchRequest("hotel");
        //2.准备DSL
        request.source().query(QueryBuilders.matchQuery("all","如家"))
                .from((page-1)*size).size(size).sort("price",SortOrder.ASC);
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //调用自定义方法进行结果响应
        responseResult(response);
    }

    //高亮查询
    @Test
    void testHightligth() throws IOException {
        //1.准备Request请求
        SearchRequest request = new SearchRequest("hotel");
        //2.准备DSL
        request.source().query(QueryBuilders.matchQuery("all","如家"))
                .highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //调用自定义方法进行结果响应
        responseResult(response);
    }

    void responseResult(SearchResponse response){
        //4.解析响应
        SearchHits searchHits = response.getHits();
        //4.1获取总条数
        long value = searchHits.getTotalHits().value;
        System.out.println("共有"+value+"条数据!!");
        //4.2获取每条数据
        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            //获取文档jsource
            String sourceAsString = hit.getSourceAsString();
            //反序列化
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            //获取高亮结果
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if (!CollectionUtils.isEmpty(highlightFields)) {
                //根据字段获取高亮结果
                HighlightField name = highlightFields.get("name");
                if (name != null) {
                    //获取高亮值
                    String nameValue = name.getFragments()[0].string();
                    //覆盖高亮结果
                    hotelDoc.setName(nameValue);
                }
                System.out.println(hotelDoc);
            }

        }
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
