package com.tracejp.gulimall.order.interceptor;

import com.tracejp.common.constant.AuthServerConstant;
import com.tracejp.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p> 拦截未登录用户 <p/>
 *
 * @author traceJP
 * @since 2023/4/4 8:25
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 放行白名单
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/order/order/status/**", requestURI);
        if (match) {
            return true;
        }

        Object attribute = request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            // 没登录就去登录
            request.getSession().setAttribute("msg", "请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }

        MemberResponseVo memberResponseVo = (MemberResponseVo) attribute;
        loginUser.set(memberResponseVo);
        return true;
    }


}
