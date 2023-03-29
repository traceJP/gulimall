package com.tracejp.gulimall.auth.controller;

import com.tracejp.common.to.UserLoginTo;
import com.tracejp.common.to.UserRegistTo;
import com.tracejp.common.utils.R;
import com.tracejp.common.utils.RRException;
import com.tracejp.gulimall.auth.service.LoginService;
import com.tracejp.gulimall.auth.service.RegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/27 22:35
 */
@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private RegisterService registerService;


    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {

        return loginService.sendCode(phone);
    }


    @PostMapping("/register")
    // 这里UserRegistTo前端采用的是form标签直接提交，不是json的提交方式，所以这里不需要@RequestBody
    // @Valid 是使用 Hibernate validation 的时候使用。@Validated 是只用 Spring Validator 校验机制使用。
    public String register(@Validated UserRegistTo to, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {

            Map<String, String> map = new HashMap<>();
            List<FieldError> fieldErrors = result.getFieldErrors();
            fieldErrors.forEach(item -> map.put(item.getField(), item.getDefaultMessage()));

            // TODO 为什么这里会抛出异常? 并且抛出异常后nacos服务就下线了？？？
/*            Map<String, String> map = result.getFieldErrors().stream()
                    .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));*/


            // addFlashAttribute 该属性只能被读取一次 (flash 一闪而过) 防止页面刷新后继续读取到该数据
            // 当数据取出后 底层的session就会被删掉
            redirectAttributes.addFlashAttribute("errors", map);

            // 用户注册发送post请求 ---> 如果转发给/reg.html（get请求） ---> 则相当于使用post请求get接口 错误
            // 直接渲染页面 ---》 会造成表单重复提交
            // 使用重定向 - 重定向时model域不会保存数据  使用 springMVC中的RedirectAttributes 向重定向页面传递数据
            // springMVC中的RedirectAttributes 原理 ===》 使用了session原理

            return "redirect:http://auth.gulimall.com/reg.html";
        }

        try {
            registerService.register(to);
        } catch (RRException e) {
            Map<String, String> errors = new HashMap<>(1);
            errors.put("code", e.getMessage());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        } catch (RuntimeException e) {
            Map<String, String> errors = new HashMap<>(1);
            errors.put("msg", e.getMessage());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        return "redirect:http://auth.gulimall.com/login.html";
    }

    @PostMapping("/login")
    public String login(UserLoginTo to, RedirectAttributes redirectAttributes) {
        R r = loginService.login(to);
        if (r.getCode() != 0) {
            Map<String, String> errors = new HashMap<>(1);
            errors.put("msg", r.get("msg").toString());
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
        return "redirect:http://gulimall.com";
    }



}
