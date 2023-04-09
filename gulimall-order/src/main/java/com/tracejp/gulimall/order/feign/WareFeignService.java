package com.tracejp.gulimall.order.feign;

import com.tracejp.common.utils.R;
import com.tracejp.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/4 20:59
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hasStock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);

}
