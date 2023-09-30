package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class HotelService extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public PageResult search(RequestParams params) {
        try {
            //1.准备Request
            SearchRequest request = new SearchRequest("hotel");
            //2.准备DSL
            //构建BoolQuery
            getBoolQueryBuilder(params,request);
            //3.分页
            int page = params.getPage();
            int size = params.getSize();
            request.source().from((page-1)*size).size(size);

            //4.排序
            String location = params.getLocation();
            if (location!=null&&!"".equals(location)){
                request.source().sort(SortBuilders.geoDistanceSort("location",
                        new GeoPoint(location))
                        .order(SortOrder.ASC)
                        .unit(DistanceUnit.KILOMETERS)
                );
            }
            //3.发送请求,得到响应
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4.解析响应
            return responseResult(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<String>> filters(RequestParams params) {
        try {
            //1.准备request
            SearchRequest request = new SearchRequest("hotel");
            //2.准备DSL语句
            //2.1 query
            getBoolQueryBuilder(params,request);
            //2.2 设置size为0,不需要查询文档
            request.source().size(0);
            //2.3聚合
            buildAggregation(request);
            //3.发送请求
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4.解析结果
            Map<String, List<String>> result = new HashMap<>();
            //4.1解析聚合结果
            Aggregations aggregations = response.getAggregations();
            //4.2根据品牌名称,获取品牌具体结果
            List<String> brandList = getAggByName(aggregations,"brandAgg");
            //4.3根据城市名称,获取城市具体结果
            List<String> cityList = getAggByName(aggregations,"cityAgg");
            //4.4根据星级名称,获取星级具体结果
            List<String> starNameList = getAggByName(aggregations,"starNamAagg");
            //放入map
            result.put("brand",brandList);
            result.put("city",cityList);
            result.put("starName",starNameList);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getSuggestion(String prefix) {
        try {
            //1.准备Request
            SearchRequest request = new SearchRequest("hotel");
            //2.准备DSL语句
            request.source().suggest(new SuggestBuilder().addSuggestion(
                    "suggestions",
                    SuggestBuilders.completionSuggestion("suggestion")
                            .prefix(prefix)
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
            List<String> res = new ArrayList<>();
            for (CompletionSuggestion.Entry.Option option : options) {
                String string = option.getText().string();
                res.add(string);
            }
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据id删除es表中的数据
     * @param id 酒店id
     */
    @Override
    public void deleteById(Long id) {
        try {
            //1.准备Request
            DeleteRequest request = new DeleteRequest("hotel",id.toString());
            //2.发送请求
            client.delete(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据id添加或修改es表中的数据
     * @param id 酒店id
     */
    @Override
    public void insertOrUpdateById(Long id) {
        try {
            //0. 根据id查询酒店信息
            Hotel hotel = getById(id);
            //将查询到的hotel信息转化为hotelDoc
            HotelDoc hotelDoc = new HotelDoc(hotel);
            //1.准备Request
            IndexRequest request = new IndexRequest("hotel").id(hotel.getId().toString());
            //2.准备DSL语句
            request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
            //3.发送请求
            client.index(request,RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getAggByName(Aggregations aggregations,String aggName) {
        //4.2根据名称获取聚合结果
        Terms brandTerms = aggregations.get(aggName);
        //4.3获取桶
        List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
        //4.4遍历桶,获取数据信息
        List<String> brandList = new ArrayList<>();
        for (Terms.Bucket bucket : buckets) {
            //获取key,也就是品牌信息
            String brandName = bucket.getKeyAsString();
            brandList.add(brandName);
        }
        return brandList;
    }

    private void buildAggregation(SearchRequest request) {
        request.source().aggregation(
                AggregationBuilders.terms("brandAgg")
                        .field("brand")
                        .size(100)
        );
        request.source().aggregation(
                AggregationBuilders.terms("cityAgg")
                        .field("city")
                        .size(100)
        );
        request.source().aggregation(
                AggregationBuilders.terms("starNamAagg")
                        .field("starName")
                        .size(100)
        );
    }

    private void getBoolQueryBuilder(RequestParams params,SearchRequest request) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //2.1 根据关键字搜索
        String key = params.getKey();
        if (key!=null&&!"".equals(key)){
            boolQuery.must(QueryBuilders.matchQuery("all",key));
        }else{
            boolQuery.must(QueryBuilders.matchAllQuery());
        }
        //2.2 根据城市搜索
        String city = params.getCity();
        if (city!=null&&!"".equals(city)){
            boolQuery.filter(QueryBuilders.termQuery("city",city));
        }
        //2.3 根据品牌搜索
        String brand = params.getBrand();
        if (brand!=null&&!"".equals(brand)){
            boolQuery.filter(QueryBuilders.termQuery("brand",brand));
        }
        //2.4 根据星级搜索
        String starName = params.getStarName();
        if (starName!=null&&!"".equals(starName)){
            boolQuery.filter(QueryBuilders.termQuery("starName",starName));
        }
        //2.5 根据价格搜索(大于等于最小价格,小于等于最大价格)
        String minPrice = params.getMinPrice();
        String maxPrice = params.getMaxPrice();
        if (minPrice!=null&&!"".equals(minPrice)&&maxPrice!=null&&!"".equals(maxPrice)){
            boolQuery.filter(QueryBuilders.rangeQuery("price")
                    .gte(minPrice)
                    .lte(maxPrice));
        }
        // 2.算分函数查询
        FunctionScoreQueryBuilder functionScoreQuery = QueryBuilders.functionScoreQuery(
                boolQuery, // 原始查询，boolQuery
                new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{ // function数组
                        new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                                QueryBuilders.termQuery("isAD", true), // 过滤条件
                                ScoreFunctionBuilders.weightFactorFunction(10) // 算分函数
                        )
                }
        );
        // 3.设置查询条件
        request.source().query(functionScoreQuery);
    }

    private PageResult responseResult(SearchResponse response){
        //4.解析响应
        SearchHits searchHits = response.getHits();
        //4.1获取总条数
        long total = searchHits.getTotalHits().value;
        //4.2获取每条数据
        SearchHit[] hits = searchHits.getHits();
        List<HotelDoc> hotels = new ArrayList<>();
        for (SearchHit hit : hits) {
            //获取文档jsource
            String sourceAsString = hit.getSourceAsString();
            //反序列化
            HotelDoc hotelDoc = JSON.parseObject(sourceAsString, HotelDoc.class);
            //获取排序值
            Object[] sortValues = hit.getSortValues();
            if (sortValues!=null&&sortValues.length>0){
                Object sortValue = sortValues[0];
                hotelDoc.setDistance(sortValue);
            }
            hotels.add(hotelDoc);
        }
        //4.3封装返回
        return new PageResult(total,hotels);
    }
}
