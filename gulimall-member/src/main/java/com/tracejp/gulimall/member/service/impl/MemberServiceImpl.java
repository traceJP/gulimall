package com.tracejp.gulimall.member.service.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tracejp.common.to.SocialUser;
import com.tracejp.common.to.UserLoginTo;
import com.tracejp.common.to.UserRegistTo;
import com.tracejp.gulimall.member.entity.MemberLevelEntity;
import com.tracejp.gulimall.member.exception.PhoneExistException;
import com.tracejp.gulimall.member.exception.UserNameExistException;
import com.tracejp.gulimall.member.service.MemberLevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.member.dao.MemberDao;
import com.tracejp.gulimall.member.entity.MemberEntity;
import com.tracejp.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(UserRegistTo userRegistTo) {

        MemberEntity memberEntity = new MemberEntity();

        // 设置默认等级
        MemberLevelEntity levelEntity = memberLevelService.getByDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        // 用户名 手机号 唯一
        if (!this.checkUserNameUnique(userRegistTo.getUserName())) {
            throw new UserNameExistException();
        }
        if (!this.checkPhoneUnique(userRegistTo.getPhone())) {
            throw new PhoneExistException();
        }
        memberEntity.setUsername(userRegistTo.getUserName());
        memberEntity.setMobile(userRegistTo.getPhone());

        // 默认值
        memberEntity.setNickname(userRegistTo.getUserName());

        // 密码加密  MD5加盐
        // BCryptPasswordEncoder md5盐加密：(spring的加密工具)
        // 传入密码，会计算出md5，然后生成一个随机盐，将其进行拼串返回。
        // 比对：通过返回值，可以直接解析出盐值，然后将用户提供的密码传入进行比对 即可进行判断 passwordEncoder.matches()比对方法
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(userRegistTo.getPassword());
        memberEntity.setPassword(encodePassword);

        this.baseMapper.insert(memberEntity);
    }

    // 检查通过返回true 不通过返回false
    @Override
    public boolean checkUserNameUnique(String userName) {
        LambdaQueryWrapper<MemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberEntity::getUsername, userName);
        return this.baseMapper.selectCount(wrapper) < 1;
    }

    @Override
    public boolean checkPhoneUnique(String phone) {
        LambdaQueryWrapper<MemberEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberEntity::getMobile, phone);
        return this.baseMapper.selectCount(wrapper) < 1;
    }

    @Override
    public MemberEntity login(UserLoginTo to) {
        String loginacct = to.getLoginacct();
        String password = to.getPassword();

        MemberEntity memberEntity = this.baseMapper.selectOne(new LambdaQueryWrapper<MemberEntity>()
                .eq(MemberEntity::getUsername, loginacct)
                .or()
                .eq(MemberEntity::getMobile, loginacct)
        );

        if (memberEntity != null) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, memberEntity.getPassword());
            if (matches) {
                return memberEntity;
            }
        }
        return null;
    }

    @Override
    public MemberEntity login(SocialUser socialUser) {

        MemberEntity memberEntity = this.baseMapper.selectOne(
                new LambdaQueryWrapper<MemberEntity>().eq(MemberEntity::getSocialUid, socialUser.getUid())
        );

        if (memberEntity == null) {  // 首次登录 注册

            memberEntity = this.regist(socialUser);
        } else {
            // 替换token
            MemberEntity update = new MemberEntity();
            update.setId(memberEntity.getId());
            update.setAccessToken(socialUser.getAccess_token());
            update.setExpiresIn(socialUser.getExpires_in());
            this.baseMapper.updateById(update);

            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());
        }

        return memberEntity;
    }

    @Override
    public MemberEntity regist(SocialUser socialUser) {
        MemberEntity memberEntity = new MemberEntity();

        // 设置默认等级
        MemberLevelEntity levelEntity = memberLevelService.getByDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        memberEntity.setSocialUid(socialUser.getUid());
        memberEntity.setAccessToken(socialUser.getAccess_token());
        memberEntity.setExpiresIn(socialUser.getExpires_in());

        try {
            // 查询社交信息 需要同时使用accessToken 和 用户的唯一社交id 查询
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("access_token", socialUser.getAccess_token());
            queryMap.put("uid", socialUser.getUid());
            String response = HttpUtil.get("https://api.weibo.com/2/users/show.json", queryMap);

            // JSONObject extends HashMap 可以直接按key拿到value
            JSONObject jsonObject = JSON.parseObject(response);
            String name = jsonObject.getString("name");
            int gender = "m".equals(jsonObject.getString("gender")) ? 1 : 0;

            memberEntity.setNickname(name);
            memberEntity.setGender(gender);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.baseMapper.insert(memberEntity);
        return memberEntity;
    }

}