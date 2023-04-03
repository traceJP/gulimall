package com.tracejp.gulimall.cart.interceptor;

import com.tracejp.common.constant.AuthServerConstant;
import com.tracejp.common.constant.CartConstant;
import com.tracejp.common.vo.MemberResponseVo;
import com.tracejp.gulimall.cart.to.UserInfoTo;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * <p> 购物车拦截器，用户检测用户是否登录 <p/>
 *
 * @author traceJP
 * @since 2023/4/1 19:04
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    /**
     * 用于存储用户信息 - 方便 Controller层 拿到当前线程处理的用户信息
     * 对于整个系统来说，本质 Map<Thread, Object> threadLocal
     * 对于单个线程来说，是一个变量（包装UserInfoTo）
     */
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     * 在目标方法执行之前执行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // spring包装后的spring-session
        HttpSession session = request.getSession();

        MemberResponseVo member = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);

        UserInfoTo userInfo = new UserInfoTo();

        if (member != null) {
            userInfo.setUserId(member.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (!ArrayUtils.isEmpty(cookies)) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                    userInfo.setUserKey(cookie.getValue());
                    userInfo.setHasCookie(true);
                }
            }
        }

        // 无论是否登录 都给你一个 user-key 标识购物车uuid
        // 登录：你可能登录前会有一个临时购物车user-key，登录后会将临时购物车合并到登录购物车
        // 未登录：每次都会生成一个临时购物车user-key
        // 即：无论是否登录，都会有一个user-key
        if (StringUtils.isEmpty(userInfo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfo.setUserKey(uuid);
        }

        threadLocal.set(userInfo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfo = threadLocal.get();
        if (!userInfo.getHasCookie()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }

}
