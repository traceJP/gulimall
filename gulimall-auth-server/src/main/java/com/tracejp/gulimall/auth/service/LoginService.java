package com.tracejp.gulimall.auth.service;

import com.tracejp.common.to.UserLoginTo;
import com.tracejp.common.utils.R;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/29 19:17
 */
public interface LoginService {

    R sendCode(String phone);

    R login(UserLoginTo to);
}
