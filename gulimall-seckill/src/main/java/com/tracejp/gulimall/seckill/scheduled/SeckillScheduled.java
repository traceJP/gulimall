package com.tracejp.gulimall.seckill.scheduled;

import com.tracejp.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/10 14:41
 */
@Slf4j
@Service
public class SeckillScheduled {

    @Autowired
    private SeckillService seckillService;


    /**
     * 业务要求：每天晚上三点，上架最近三天需要秒杀的商品
     */

    @Scheduled(cron = "0 0 3 * * ?")
    public void uploadSeckillSkuLatest3Days() {
        seckillService.uploadSeckillSkuLatest3Days();
    }

}
