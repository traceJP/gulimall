package com.tracejp.common.to;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/29 19:42
 */
@Data
public class UserRegistTo {

    @NotEmpty(message = "用户名不能为空")
    private String userName;

    @NotEmpty(message = "密码必须填写")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;

}
