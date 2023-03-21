package com.tracejp.gulimall.search.feign;

import com.tracejp.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/21 21:31
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @RequestMapping("/product/attr/info/{attrId}")
    R getAttrInfo(@PathVariable("attrId") Long attrId);

    @GetMapping("/product/brand/infos")
    R infos(@RequestParam("brandIds") List<Long> brandIds);

}
