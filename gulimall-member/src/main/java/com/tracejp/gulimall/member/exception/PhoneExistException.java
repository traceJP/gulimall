package com.tracejp.gulimall.member.exception;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/29 20:49
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("手机号已存在");
    }

}
