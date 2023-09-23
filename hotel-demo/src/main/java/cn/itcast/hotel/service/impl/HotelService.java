package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.pojo.RequestParams;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
            BoolQueryBuilder boolQuery = getBoolQueryBuilder(params);

            request.source().query(boolQuery);
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

    private BoolQueryBuilder getBoolQueryBuilder(RequestParams params) {
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
        return boolQuery;
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
