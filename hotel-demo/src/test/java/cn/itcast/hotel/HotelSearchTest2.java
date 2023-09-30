package cn.itcast.hotel;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class HotelSearchTest2 {

    private RestHighLevelClient client;

    @Test
    void testAggregation() throws IOException {
        //1.准备request
        SearchRequest request = new SearchRequest("hotel");
        //2.准备DSL语句
        //2.1 设置size为0,不需要查询文档
        request.source().size(0);
        //2.2聚合
        request.source().aggregation(
                AggregationBuilders.terms("brandagg")
                        .field("brand")
                        .size(20)
        );
        //3.发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.解析结果
        System.out.println(response);
        //解析聚合结果
        Aggregations aggregations = response.getAggregations();
        //根据名称获取聚合结果
        Terms brandTerms = aggregations.get("brandagg");
        //获取桶
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        //遍历桶,获取数据信息
        for (Terms.Bucket bucket : buckets) {
            //获取key,也就是品牌信息
            String brandName = bucket.getKeyAsString();
            System.out.println(brandName);
        }
    }

    @Test
    void testSuggestion() throws IOException {
        //1.准备Request
        SearchRequest request = new SearchRequest("hotel");
        //2.准备DSL语句
        request.source().suggest(new SuggestBuilder().addSuggestion(
                "suggestions",
                SuggestBuilders.completionSuggestion("suggestion")
                        .prefix("h")
                        .skipDuplicates(true)
                        .size(20)
        ));
        //3.发起请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //4.解析结果
        //4.1获取结果集中的suggest
        Suggest suggestions = response.getSuggest();
        //4.2根据补全查询名称,获取补全结果
        CompletionSuggestion suggestion = suggestions.getSuggestion("suggestions");
        //4.2获取optins
        List<CompletionSuggestion.Entry.Option> options = suggestion.getOptions();
        //遍历
        for (CompletionSuggestion.Entry.Option option : options) {
            String string = option.getText().string();
            System.out.println(string);
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
