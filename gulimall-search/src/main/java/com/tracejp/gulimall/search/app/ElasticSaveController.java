package com.tracejp.gulimall.search.app;

import com.tracejp.common.exception.BizCodeEnum;
import com.tracejp.common.to.es.SkuEsModel;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/18 21:19
 */
@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        boolean hasError;
        try {
            hasError = productSaveService.saveProduct(skuEsModels);
        } catch (IOException e) {
            log.error("商品保存es错误", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }

        if (hasError) {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        } else {
            return R.ok();
        }

    }

}
