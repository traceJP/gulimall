package com.tracejp.gulimall.member.exception;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/29 20:50
 */
public class UserNameExistException extends RuntimeException {

    public UserNameExistException() {
        super("用户名已存在");
    }

}
