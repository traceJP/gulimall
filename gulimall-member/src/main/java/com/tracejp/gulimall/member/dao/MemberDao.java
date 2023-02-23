package com.tracejp.gulimall.member.dao;

import com.tracejp.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:13:27
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
