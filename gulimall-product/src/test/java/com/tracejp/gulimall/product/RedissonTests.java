package com.tracejp.gulimall.product;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/20 14:28
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RedissonTests {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void test() {

        // 1 获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");

        // 2 加锁
        // lock.lock(); 阻基式寺待。默认加的锁都是30s
        // 1)锁 lock.lock(); 的自动续期，如果业务超长，运行期间自动给锁续上新的30s。
        //   不用担心业务时间长，锁自动过期被删掉
        // 2)、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除.

        // 手动解锁 问题：在锁时间到了后不会自动续期
        lock.lock(10, TimeUnit.SECONDS); // 10s自动解锁，自动解锁时间一定要大于业务的执行时间

        // 看门狗执行原理：
        // 如果传递了锁的超时时间，就发送给redis执行lua脚本，进行占锁，默认超时就是我们指定的时间
        // 如果我们未指定超时时间，就使用 30 * 1000【LockWatchdogTimeout看门狗的默认时间】
        // 只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】，每隔 1/3 时间就会再次续期

        // 最佳实战：不要使用看门狗，会造成死锁出现
        // 还是使用 lock.lock(10, TimeUnit.SECONDS); 指定锁的超时时间，10s以后自动解锁，自动解锁时间一定要大于业务的执行时间

        try {
            System.out.println("加锁成功，执行业务逻辑");
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("释放锁");
            lock.unlock();
        }


    }

}
