package com.tracejp.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.to.SocialUser;
import com.tracejp.common.to.UserLoginTo;
import com.tracejp.common.to.UserRegistTo;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:13:27
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(UserRegistTo userRegistTo);

    boolean checkUserNameUnique(String userName);

    boolean checkPhoneUnique(String phone);

    MemberEntity login(UserLoginTo to);

    MemberEntity login(SocialUser socialUser);

    MemberEntity regist(SocialUser socialUser);
}

