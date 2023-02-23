package com.tracejp.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.gulimall.member.entity.MemberLoginLogEntity;

import java.util.Map;

/**
 * 会员登录记录
 *
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:13:27
 */
public interface MemberLoginLogService extends IService<MemberLoginLogEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

