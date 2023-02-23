package com.tracejp.gulimall.coupon.dao;

import com.tracejp.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:11:25
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
