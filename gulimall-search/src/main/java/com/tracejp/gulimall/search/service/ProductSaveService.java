package com.tracejp.gulimall.search.service;


import com.tracejp.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/18 21:23
 */
public interface ProductSaveService {


    Boolean saveProduct(List<SkuEsModel> skuEsModels) throws IOException;
}
