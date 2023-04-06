package com.tracejp.gulimall.ware.exception;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/6 20:42
 */
public class NoStockException extends RuntimeException {

    public NoStockException(Long skuId) {
        super("商品：" + skuId + "：没有足够的库存");
    }

}
