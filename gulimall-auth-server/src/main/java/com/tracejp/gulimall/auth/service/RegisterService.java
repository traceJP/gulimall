package com.tracejp.gulimall.auth.service;

import com.tracejp.common.to.UserRegistTo;
import com.tracejp.common.utils.RRException;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/29 20:01
 */
public interface RegisterService {

    void register(UserRegistTo to) throws RRException;

}
