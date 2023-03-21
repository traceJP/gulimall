package com.tracejp.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.tracejp.common.constant.EsConstant;
import com.tracejp.common.to.es.SkuEsModel;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.search.feign.ProductFeignService;
import com.tracejp.gulimall.search.service.MallSearchService;
import com.tracejp.gulimall.search.vo.AttrResponseVo;
import com.tracejp.gulimall.search.vo.BrandVo;
import com.tracejp.gulimall.search.vo.SearchParam;
import com.tracejp.gulimall.search.vo.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/20 19:39
 */
@Slf4j
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private RestHighLevelClient esClient;


    @Override
    public SearchResult search(SearchParam param) {
        SearchRequest searchRequest = buildSearchRequest(param);
        SearchResponse response = null;
        try {
            response = esClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            log.error("es search error: {}", e.getMessage());
        }
        return buildSearchResult(param, response);
    }

    /**
     * 构建检索请求
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        SearchRequest request = new SearchRequest().indices(EsConstant.PRODUCT_INDEX).source(sourceBuilder);

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // query bool filter
        if (!StringUtils.isEmpty(param.getKeyword())) {

            // keyword
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));

            // highlight
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color: red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }

        if (param.getCatalog3Id() != null && param.getCatalog3Id() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        if (param.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            param.getAttrs().forEach(item -> {
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                String[] s = item.split("_");
                String attrId = s[0];
                String[] attrValues = s[1].split(":");
                boolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                boolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", boolQuery, ScoreMode.None));
            });
        }

        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gte(s[0]).lte(s[1]));
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").lte(s[0]));
                }
                if (param.getSkuPrice().endsWith("_")) {
                    boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gte(s[0]));
                }
            }
        }

        sourceBuilder.query(boolQueryBuilder);

        // sort
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            String[] s = sort.split("_");
            sourceBuilder.sort(s[0], s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC);
        }

        // from size
        if (param.getPageNum() != null) {
            // from = (pageNum - 1) * pageSize
            sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
            sourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);
        }


        /*
         * 聚合分析
         */
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        // subAggregation 子聚合
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(50));
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(50));
        sourceBuilder.aggregation(brandAgg);

        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg");
        catalogAgg.field("catalogId").size(50);
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(50));
        sourceBuilder.aggregation(catalogAgg);

        // 嵌入式聚合
        NestedAggregationBuilder nested = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(50));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        nested.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(nested);


        return request;
    }

    /**
     * 构建检索结果
     */
    private SearchResult buildSearchResult(SearchParam param, SearchResponse response) {
        SearchResult result = new SearchResult();

        SearchHits hits = response.getHits();

        // 总记录数
        result.setTotal(hits.getTotalHits().value);

        // 总页码 = 总记录数 / 总页码
        int totalPages = (int) hits.getTotalHits().value % EsConstant.PRODUCT_PAGE_SIZE == 0 ?
                (int) hits.getTotalHits().value / EsConstant.PRODUCT_PAGE_SIZE :
                (int) hits.getTotalHits().value / EsConstant.PRODUCT_PAGE_SIZE + 1;
        result.setTotalPages(totalPages);

        // 分页导航列表
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 0; i < totalPages; i++) {
            pageNavs.add(i + 1);
        }
        result.setPageNavs(pageNavs);

        // x 面包屑导航功能
        if (!CollectionUtils.isEmpty(param.getAttrs())) {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.getAttrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    // 使用 fastJson转换
                    AttrResponseVo responseVo = JSON.parseObject(r.get("attr").toString(), AttrResponseVo.class);
                    navVo.setNavName(responseVo.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                // 面包屑取消
                String encode = null;
                try {
                    encode = URLEncoder.encode(attr, "UTF-8");
                    // 处理特殊字符
                    encode = encode.replace("+", "%20");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                param.set_queryString(param.get_queryString().replace("&attrs=" + encode, ""));
                // 本机部署环境URL
                navVo.setLink("http://localhost:12000/list.html?" + param.get_queryString());

                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(navVos);
        }
/*        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();
            navVo.setNavName("品牌");
            R r = productFeignService.infos(param.getBrandId());
            if (r.getCode() == 0) {
                List<BrandVo> brandVos = JSON.parseArray((String) r.get("brand"), BrandVo.class);
                StringBuffer buffer = new StringBuffer();
                String replace = null;
                for (BrandVo brandVo : brandVos) {
                    buffer.append(brandVo.getBrandName() + ";");
                    try {
                        replace = URLEncoder.encode(brandVo.getBrandId().toString(), "UTF-8");
                        replace = replace.replace("+", "%20");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                navVo.setNavValue(buffer.toString());
                param.set_queryString(param.get_queryString().replace("&brandId=" + replace, ""));
                navVo.setLink("http://localhost:12000/list.html?" + param.get_queryString());
            }
        }*/


        // 所有商品信息
        if (hits.getHits() != null && hits.getHits().length > 0) {
            List<SkuEsModel> esModels = Arrays.stream(hits.getHits()).map(item -> {
                String sourceAsString = item.getSourceAsString();
                SkuEsModel esModel = JSON.parseObject(sourceAsString, SkuEsModel.class);

                // highlight
                if (!StringUtils.isEmpty(item.getHighlightFields().get("skuTitle"))) {
                    HighlightField skuTitle = item.getHighlightFields().get("skuTitle");
                    esModel.setSkuTitle(skuTitle.getFragments()[0].string());
                }

                return esModel;
            }).collect(Collectors.toList());
            result.setProducts(esModels);
        }


        // 聚合信息封装
        Aggregations aggregations = response.getAggregations();

        // 返回 Aggregation 接口对象 =》 ParsedAggregation 实现了 Aggregation 接口 =》 可以转换到对应type类型的对象
        ParsedLongTerms catalogAgg = aggregations.get("catalog_agg");
        if (!CollectionUtils.isEmpty(catalogAgg.getBuckets())) {
            List<SearchResult.CatalogVo> catalogVos = catalogAgg.getBuckets().stream().map(item -> {
                SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();

                catalogVo.setCatalogId(item.getKeyAsNumber().longValue());

                ParsedStringTerms catalogNameAgg = item.getAggregations().get("catalog_name_agg");
                catalogVo.setCatalogName(catalogNameAgg.getBuckets().get(0).getKeyAsString());

                return catalogVo;
            }).collect(Collectors.toList());
            result.setCatalogs(catalogVos);
        }

        ParsedLongTerms brandAgg = aggregations.get("brand_agg");
        if (!CollectionUtils.isEmpty(brandAgg.getBuckets())) {
            List<SearchResult.BrandVo> brandVos = brandAgg.getBuckets().stream().map(item -> {
                SearchResult.BrandVo catalogVo = new SearchResult.BrandVo();

                catalogVo.setBrandId(item.getKeyAsNumber().longValue());

                ParsedStringTerms brandNameAgg = item.getAggregations().get("brand_name_agg");
                catalogVo.setBrandName(brandNameAgg.getBuckets().get(0).getKeyAsString());

                ParsedStringTerms brandImgAgg = item.getAggregations().get("brand_img_agg");
                catalogVo.setBrandImg(brandImgAgg.getBuckets().get(0).getKeyAsString());

                return catalogVo;
            }).collect(Collectors.toList());
            result.setBrands(brandVos);
        }

        ParsedNested attrAgg = aggregations.get("attr_agg");
        // ParsedNested attrsAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        if (!CollectionUtils.isEmpty(attrIdAgg.getBuckets())) {
            List<SearchResult.AttrVo> attrVos = attrIdAgg.getBuckets().stream().map(item -> {
                SearchResult.AttrVo attrVo = new SearchResult.AttrVo();

                attrVo.setAttrId(item.getKeyAsNumber().longValue());

                ParsedStringTerms attrNameAgg = item.getAggregations().get("attr_name_agg");
                attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());

                ParsedStringTerms attrValueAgg = item.getAggregations().get("attr_value_agg");
                if (!CollectionUtils.isEmpty(attrValueAgg.getBuckets())) {
                    List<String> attrValues = attrValueAgg.getBuckets().stream()
                            .map(MultiBucketsAggregation.Bucket::getKeyAsString)
                            .collect(Collectors.toList());
                    attrVo.setAttrValue(attrValues);
                }

                return attrVo;
            }).collect(Collectors.toList());
            result.setAttrs(attrVos);
        }

        return result;
    }

}
