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

    SMS_CODE_EXCEPTION(10002, "验证码获取频率太高，请稍后再试"),

    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),

    USER_EXIST_EXCEPTION(15001, "用户存在"),

    PHONE_EXIST_EXCEPTION(15002, "手机号存在"),

    LOGINACCT_PASSWORD_INVALID_EXCEPTION(15003, "账号或密码错误");

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
