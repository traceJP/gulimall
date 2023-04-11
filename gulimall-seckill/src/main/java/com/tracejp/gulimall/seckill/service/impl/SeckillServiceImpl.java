package com.tracejp.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tracejp.common.constant.SecKillConstant;
import com.tracejp.common.to.SeckillSkuRedisTo;
import com.tracejp.common.to.SeckillSkuTo;
import com.tracejp.common.to.mq.SeckillOrderTo;
import com.tracejp.common.utils.R;
import com.tracejp.common.vo.MemberResponseVo;
import com.tracejp.gulimall.seckill.feign.CouponFeignService;
import com.tracejp.gulimall.seckill.feign.ProductFeignService;
import com.tracejp.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.tracejp.gulimall.seckill.service.SeckillService;
import com.tracejp.gulimall.seckill.vo.SeckillSessionWithSkus;
import com.tracejp.gulimall.seckill.vo.SeckillSkuVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private RabbitTemplate rabbitTemplate;

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

    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        Set<String> keys = redisTemplate.keys(SecKillConstant.SESSIONS_PREFIX + "*");
        long now = new Date().getTime();
        if (!CollectionUtils.isEmpty(keys)) {
            for (String key : keys) {
                String replace = key.replace(SecKillConstant.SESSIONS_PREFIX, "");
                String[] split = replace.split("_");
                long startTime = Long.parseLong(split[0]);
                long endTime = Long.parseLong(split[1]);
                // 在当前时间范围
                if (now >= startTime && now <= endTime) {
                    // 获取当前秒杀场次的所有商品信息
                    // 0, -1 表示查询list中所有
                    List<String> values = redisTemplate.opsForList().range(key, 0, -1);
                    if (!CollectionUtils.isEmpty(values)) {
                        BoundHashOperations<String, String, String> skuOps =
                                redisTemplate.boundHashOps(SecKillConstant.SESSION_SKU_PREFIX);
                        List<String> skus = skuOps.multiGet(values);
                        if (!CollectionUtils.isEmpty(skus)) {
                            return skus.stream()
                                    .map(item -> JSON.parseObject(item, SeckillSkuRedisTo.class))
                                    .collect(Collectors.toList());
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public SeckillSkuRedisTo getSkuSeckillInfo(Long skuId) {
//        List<SeckillSkuRedisTo> currentSeckillSkus = getCurrentSeckillSkus();
//        if (!CollectionUtils.isEmpty(currentSeckillSkus)) {
//            for (SeckillSkuRedisTo currentSeckillSku : currentSeckillSkus) {
//                if (currentSeckillSku.getSkuId().equals(skuId)) {
//                    return currentSeckillSku;
//                }
//            }
//        }
        //
        BoundHashOperations<String, String, String> skuOps =
                redisTemplate.boundHashOps(SecKillConstant.SESSION_SKU_PREFIX);
        Set<String> keys = skuOps.keys();
        if (!CollectionUtils.isEmpty(keys)) {
            for (String key : keys) {
                String[] split = key.split("_");
                Long redisSkuId = Long.parseLong(split[1]);
                if (redisSkuId.equals(skuId)) {
                    String json = skuOps.get(key);
                    SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    // 是否在秒杀时间段内 隐藏随机码
                    long now = new Date().getTime();
                    if (now < redisTo.getStartTime() && now > redisTo.getEndTime()) {
                        redisTo.setRandomCode(null);
                    }
                    return redisTo;
                }
            }
        }
        return null;
    }

    // TODO 秒杀信号量 过期时间 等处理
    @Override
    public String kill(String killId, String key, Integer num) {

        MemberResponseVo member = LoginUserInterceptor.loginUser.get();

        BoundHashOperations<String, String, String> skuOps =
                redisTemplate.boundHashOps(SecKillConstant.SESSION_SKU_PREFIX);
        String json = skuOps.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
        // 合法性验证
        long now = new Date().getTime();
        if (now < seckillSkuRedisTo.getStartTime() || now > seckillSkuRedisTo.getEndTime()) {
            return null;
        }
        if (!seckillSkuRedisTo.getRandomCode().equals(key)) {
            return null;
        }
        if (seckillSkuRedisTo.getSeckillLimit().compareTo(new BigDecimal(num)) < 0) {
            return null;
        }

        // 验证该用户是否已经购买过（一个人只能购买一次）
        // 注意：这里不能直接调用远程服务查询，必须通过redis记录用户购买信息
        String recordKey = member.getId() + "_" + seckillSkuRedisTo.getPromotionSessionId() + "_"
                + seckillSkuRedisTo.getSkuId();
        long ttl = seckillSkuRedisTo.getEndTime() - now;  // 结束时间 - 当前时间
        // SETNX - 判断是否存在key，存在返回false，不存在添加并返回true
        Boolean hasPurchase = redisTemplate.opsForValue().setIfAbsent(recordKey, num.toString(), ttl, TimeUnit.MILLISECONDS);
        if (!hasPurchase) {
            return null;
        }

        // 尝试获取信号量
        RSemaphore semaphore = redissonClient.getSemaphore(SecKillConstant.SKU_STOCK_SEMAPHORE + key);
        // 不允许阻塞在这里 所以使用 tryAcquire，并且这里不允许设置任何阻塞时间
        if (!semaphore.tryAcquire(num)) {
            return null;
        }

        // 成功（创建订单）=>向MQ发送消息，执行创建订单业务（！流量削峰操作！）
        String idStr = IdWorker.getIdStr();
        SeckillOrderTo orderTo = new SeckillOrderTo();
        orderTo.setOrderSn(idStr);
        orderTo.setPromotionSessionId(seckillSkuRedisTo.getPromotionSessionId());
        orderTo.setSkuId(seckillSkuRedisTo.getSkuId());
        orderTo.setSeckillPrice(seckillSkuRedisTo.getSeckillPrice());
        orderTo.setNum(num);
        orderTo.setMemberId(member.getId());
        rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);

        return idStr;
    }

    private void saveSessionInfos(List<SeckillSessionWithSkus> data) {
        data.forEach(item -> {
            // 缓存数据结构（key - list）：startTime_endTime - sessionId_item.skuId（与SESSION_SKU_PREFIX Hash key对应）
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
            // 缓存数据结构（key - hash）：SESSION_SKU_PREFIX - <sessionId_item.skuId: item.skuEntity>
            BoundHashOperations<String, Object, Object> redisOps =
                    redisTemplate.boundHashOps(SecKillConstant.SESSION_SKU_PREFIX);
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

                        // 设置sku对应的秒杀数量信号量（SKU_STOCK_SEMAPHORE + 随机码）
                        redissonClient.getSemaphore(SecKillConstant.SKU_STOCK_SEMAPHORE + uuid)
                                .trySetPermits(relationSku.getSeckillCount().intValue());
                    }
                });
            }
        });
    }

}
