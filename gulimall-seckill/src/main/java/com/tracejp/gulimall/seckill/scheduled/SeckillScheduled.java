package com.tracejp.gulimall.seckill.scheduled;

import com.tracejp.common.constant.SecKillConstant;
import com.tracejp.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 业务要求：每天晚上三点，上架最近三天需要秒杀的商品
     * cron = "0 0 3 * * ?"
     */
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days() {
        log.info("执行定时任务 - 商品上架！");
        // 为该任务上锁 - 防止分布式系统中，定时任务同时到点后，同时执行该任务
        RLock lock = redissonClient.getLock(SecKillConstant.UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            seckillService.uploadSeckillSkuLatest3Days();
        } finally {
            lock.unlock();
        }
    }

}
