package com.tracejp.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.tracejp.common.constant.SecKillConstant;
import com.tracejp.common.to.SeckillSkuRedisTo;
import com.tracejp.common.to.SeckillSkuTo;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.seckill.feign.CouponFeignService;
import com.tracejp.gulimall.seckill.feign.ProductFeignService;
import com.tracejp.gulimall.seckill.service.SeckillService;
import com.tracejp.gulimall.seckill.vo.SeckillSessionWithSkus;
import com.tracejp.gulimall.seckill.vo.SeckillSkuVo;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void uploadSeckillSkuLatest3Days() {

        R session = couponFeignService.getLatest3DaysSession();
        if (session.getCode() == 0) {
            List<SeckillSessionWithSkus> data =
                    JSON.parseArray(JSON.toJSONString(session.get("data")), SeckillSessionWithSkus.class);
            // 秒杀商品 缓存到redis - 分别缓存 session信息 和 关联sku信息
            if (!CollectionUtils.isEmpty(data)) {
                this.saveSessionInfos(data);
                this.saveSessionSkuInfos(data);
            }

        }

    }

    private void saveSessionInfos(List<SeckillSessionWithSkus> data) {
        data.forEach(item -> {
            // 缓存数据结构（key - list）：startTime_endTime - sessionId_item.skuId
            long startTime = item.getStartTime().getTime();
            long endTime = item.getEndTime().getTime();
            String key = SecKillConstant.SESSIONS_PREFIX + startTime + "_" + endTime;
            List<SeckillSkuVo> relationSkus = item.getRelationSkus();
            if (!CollectionUtils.isEmpty(relationSkus)) {
                List<String> sessionIds = relationSkus.parallelStream()
                        .map(sku -> sku.getPromotionSessionId() + "_" + sku.getSkuId())
                        .collect(Collectors.toList());
                // 因外部使用Redisson上锁，一次只有一个线程能执行此方法，所以这里无需保证原子操作，只需直接判断是否存在key，再执行添加操作
                if (!redisTemplate.hasKey(key)) {
                    redisTemplate.opsForList().leftPushAll(key, sessionIds);
                    // redisTemplate.expire(key, endTime - startTime, TimeUnit.SECONDS);
                }
            }
        });
    }

    private void saveSessionSkuInfos(List<SeckillSessionWithSkus> data) {
        data.forEach(item -> {
            // 缓存数据结构（key - hash）：sessionId - <sessionId_item.skuId: item.skuEntity>
            BoundHashOperations<String, Object, Object> redisOps =
                    redisTemplate.boundHashOps(SecKillConstant.SESSION_SKU_PREFIX + item.getId());
            List<SeckillSkuVo> relationSkus = item.getRelationSkus();
            if (!CollectionUtils.isEmpty(relationSkus)) {
                relationSkus.forEach(relationSku -> {
                    String redisKey = relationSku.getPromotionSessionId() + "_" + relationSku.getSkuId();
                    // 因外部使用Redisson上锁，一次只有一个线程能执行此方法，所以这里无需保证原子操作，只需直接判断是否存在key，再执行添加操作
                    if (!redisOps.hasKey(redisKey)) {

                        SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();

                        // 秒杀信息
                        BeanUtils.copyProperties(relationSku, seckillSkuRedisTo);

                        // sku详细信息
                        R skuInfo = productFeignService.getSkuInfo(relationSku.getSkuId());
                        if (skuInfo.getCode() == 0) {
                            SeckillSkuTo info =
                                    JSON.parseObject(JSON.toJSONString(skuInfo.get("skuInfo")), SeckillSkuTo.class);
                            seckillSkuRedisTo.setSkuInfo(info);
                        }

                        // 获取随机码
                        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
                        seckillSkuRedisTo.setRandomCode(uuid);

                        // 秒杀开始时间和结束时间（秒杀场次时间信息 SeckillSession）
                        seckillSkuRedisTo.setStartTime(item.getStartTime().getTime());
                        seckillSkuRedisTo.setEndTime(item.getEndTime().getTime());

                        // 缓存到redis
                        String skuJson = JSON.toJSONString(seckillSkuRedisTo);
                        redisOps.put(redisKey, skuJson);

                        // 设置sku对应的秒杀数量信号量
                        redissonClient.getSemaphore(SecKillConstant.SKU_STOCK_SEMAPHORE + uuid)
                                .trySetPermits(relationSku.getSeckillCount().intValue());
                    }
                });
            }
        });
    }

}
