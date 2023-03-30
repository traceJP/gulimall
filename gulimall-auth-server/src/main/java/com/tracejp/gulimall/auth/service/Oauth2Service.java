package com.tracejp.gulimall.auth.service;

import com.tracejp.gulimall.auth.vo.MemberResponseVo;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/30 9:53
 */
public interface Oauth2Service {

    MemberResponseVo login(String code);

}
