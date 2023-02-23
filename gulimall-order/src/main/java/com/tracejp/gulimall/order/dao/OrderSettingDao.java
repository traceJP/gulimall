package com.tracejp.gulimall.order.dao;

import com.tracejp.gulimall.order.entity.OrderSettingEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单配置信息
 * 
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:15:10
 */
@Mapper
public interface OrderSettingDao extends BaseMapper<OrderSettingEntity> {
	
}
