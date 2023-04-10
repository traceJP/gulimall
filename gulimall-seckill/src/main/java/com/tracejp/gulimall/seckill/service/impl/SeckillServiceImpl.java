package com.tracejp.gulimall.seckill.service.impl;

import com.tracejp.gulimall.seckill.feign.CouponFeignService;
import com.tracejp.gulimall.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/10 14:46
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private CouponFeignService couponFeignService;


    @Override
    public void uploadSeckillSkuLatest3Days() {



    }

}
