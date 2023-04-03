package com.tracejp.gulimall.cart.to;

import lombok.Data;

/**
 * <p> 用于标识登录用户和临时用户 <p/>
 * userId：登录用户的数据库id
 * userKey：临时用户的uuid
 *
 * @author traceJP
 * @since 2023/4/1 19:07
 */
@Data
public class UserInfoTo {

    private Long userId;

    /**
     * 用户是否存在userKey的cookie
     * 用于判断是否需要给用户添加userKey的cookie
     */
    private Boolean hasCookie = false;

    private String userKey;

}
