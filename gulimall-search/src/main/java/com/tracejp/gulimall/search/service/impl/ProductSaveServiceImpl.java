package com.tracejp.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.tracejp.common.constant.EsConstant;
import com.tracejp.common.to.es.SkuEsModel;
import com.tracejp.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/18 21:23
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient esClient;

    @Override
    public Boolean saveProduct(List<SkuEsModel> skuEsModels) throws IOException {

         // 保存数据至es
        BulkRequest bulkRequest = new BulkRequest();
        skuEsModels.forEach(item -> {
            String source = JSON.toJSONString(item);
            IndexRequest indexRequest = new IndexRequest()
                    .index(EsConstant.PRODUCT_INDEX)
                    .id(item.getSkuId().toString())
                    .source(source, XContentType.JSON);
            bulkRequest.add(indexRequest);
        });

        BulkResponse response = esClient.bulk(bulkRequest, RequestOptions.DEFAULT);

        boolean hasError = response.hasFailures();
        for (BulkItemResponse item : response.getItems()) {
            if (item.isFailed()) {
                log.error("商品上架错误: {}，错误商品id为{}", item.getFailureMessage(), item.getId());
            }
        }

        return hasError;
    }

}
