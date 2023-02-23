package com.tracejp.gulimall.product;

import com.tracejp.gulimall.product.entity.BrandEntity;
import com.tracejp.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallProductApplicationTests {


    private BrandService brandService;

    @Autowired
    public GulimallProductApplicationTests(BrandService brandService) {
        this.brandService = brandService;
    }

    @Test
    void contextLoads() {

        BrandEntity brandEntity = new BrandEntity();

        brandEntity.setName("小米");

        brandService.save(brandEntity);

    }

}
