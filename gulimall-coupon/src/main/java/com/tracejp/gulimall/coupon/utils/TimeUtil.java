package com.tracejp.gulimall.coupon.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/10 14:55
 */
public class TimeUtil {

    /**
     * 返回系统时间的 yyyy-MM-dd 00:00:00
     */
    public static LocalDateTime getNowAsStart() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        return LocalDateTime.of(now, min);
    }

    /**
     * 返回系统时间的 yyyy-MM-dd 23:59:59
     */
    public static LocalDateTime getNowAsEnd() {
        LocalDate now = LocalDate.now();
        LocalTime max = LocalTime.MAX;
        return LocalDateTime.of(now, max);
    }

}
