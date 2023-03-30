package com.tracejp.gulimall.auth.vo;

import lombok.Data;

import java.util.Date;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/30 15:01
 */
@Data
public class MemberResponseVo {

    /**
     * id
     */
    private Long id;
    /**
     * 会员等级id
     */
    private Long levelId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 昵称
     */
    private String nickname;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 头像
     */
    private String header;
    /**
     * 性别
     */
    private Integer gender;
    /**
     * 生日
     */
    private Date birth;
    /**
     * 所在城市
     */
    private String city;
    /**
     * 职业
     */
    private String job;
    /**
     * 个性签名
     */
    private String sign;
    /**
     * 用户来源
     */
    private Integer sourceType;
    /**
     * 积分
     */
    private Integer integration;
    /**
     * 成长值
     */
    private Integer growth;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 注册时间
     */
    private Date createTime;

    // =============================== 微博登录 ==============================

    /**
     * 社交用户唯一标识（无论用户访问多少次，该值都是用户的唯一标识）
     */
    private String socialUid;

    /**
     * 社交用户访问令牌（用于方便开发者判断的，用户的一个现时token） 实际上不需要存储此字段
     * 访问令牌代表用户在微博客户端 同意了一次 第三方应用的授权
     */
    private String accessToken;

    /**
     * 社交用户访问令牌过期时间 实际上不需要存储此字段
     */
    private Long expiresIn;

}
