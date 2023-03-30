package com.tracejp.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.tracejp.common.exception.BizCodeEnum;
import com.tracejp.common.to.SocialUser;
import com.tracejp.common.to.UserLoginTo;
import com.tracejp.common.to.UserRegistTo;
import com.tracejp.gulimall.member.exception.PhoneExistException;
import com.tracejp.gulimall.member.exception.UserNameExistException;
import com.tracejp.gulimall.member.feign.CouponFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tracejp.gulimall.member.entity.MemberEntity;
import com.tracejp.gulimall.member.service.MemberService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.R;



/**
 * 会员
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:13:27
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test() {
    	MemberEntity memberEntity = new MemberEntity();
    	memberEntity.setNickname("张三");
    	R memberCoupons = couponFeignService.memberCoupons();
    	return R.ok()
                .put("member", memberEntity)
                .put("coupons", memberCoupons.get("coupons"));
    }


    @PostMapping("/regist")
    public R regist(@RequestBody UserRegistTo userRegistTo) {
        try {
            memberService.regist(userRegistTo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        } catch (UserNameExistException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody UserLoginTo to) {
        MemberEntity memberEntity = memberService.login(to);
        if (memberEntity == null) {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getCode(),
                    BizCodeEnum.LOGINACCT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }
        return R.ok().put("data", memberEntity);
    }

    @PostMapping("/oauth2/login")
    public R oauthLogin(@RequestBody SocialUser socialUser) {
        MemberEntity memberEntity = memberService.login(socialUser);

        return R.ok().put("data", memberEntity);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    // @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    // @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    // @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    // @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    // @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
