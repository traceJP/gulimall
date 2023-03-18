package com.tracejp.common.exception;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/6 20:03
 */
public enum BizCodeEnum {

    VALID_EXCEPTION(10000, "参数格式校验失败"),

    UNKNOWN_EXCEPTION(10001, "系统未知异常"),

    PRODUCT_UP_EXCEPTION(11000, "商品上架异常");

    private int code;

    private String msg;

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
