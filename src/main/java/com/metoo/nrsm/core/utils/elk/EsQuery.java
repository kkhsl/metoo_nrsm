//package com.metoo.nrsm.core.utils.elk;
//
//
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.search.aggregations.AggregationBuilder;
//import org.elasticsearch.search.aggregations.AggregationBuilders;
//import org.elasticsearch.search.aggregations.bucket.terms.Terms;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//@Slf4j
//@Service
//public class EsQuery {
//
//    @Autowired
//    private EsClientConfig esClientConfig;
//
//    public static void test() {
//        String indName = "nat-2024.09.18";
//        SearchRequest searchRequest = new SearchRequest(indName);
//        EsClientConfig esClientConfig = new EsClientConfig();
//        RestHighLevelClient client = esClientConfig.elasticsearchClient();
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//        // 按源IP分组查询
//        AggregationBuilder aggregationBuilder = AggregationBuilders.terms("destIpGroup").field("destIp.keyword");
//        searchSourceBuilder.aggregation(aggregationBuilder);
//        searchRequest.source(searchSourceBuilder);
//        try {
//            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
//            //map:key=分组的状态,value=每组的个数
//            Map<String, Integer> stateCountMap = new LinkedHashMap<>();
//            // 获取前 5 个请求最多的源IP
//            Terms terms = response.getAggregations().get("destIpGroup");
//            for (int i = 0; i < 5; i++) {
//                Terms.Bucket bucket = terms.getBuckets().get(i);
//                stateCountMap.put(bucket.getKeyAsString(), Long.valueOf(bucket.getDocCount()).intValue());
//            }
//            stateCountMap.forEach((k, v) -> {
//                System.out.println("destIp = " + k + "，请求次数 = " + v);
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        SearchRequest q = new SearchRequest();
//    }
//
//    public static void main(String[] args) {
//        EsQuery.test();
//    }
//
//}
