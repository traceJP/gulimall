package com.tracejp.common.to;

import lombok.Data;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/30 11:02
 */
@Data
public class SocialUser {

    private String access_token;

    @Deprecated
    private String remind_in;

    private Long expires_in;

    private String uid;

    private String isRealName;

}
