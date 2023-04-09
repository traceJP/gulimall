package com.tracejp.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 库存工作单
 * 
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 21:16:23
 */
@Data
@TableName("wms_ware_order_task_detail")
public class WareOrderTaskDetailEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * sku_id
	 */
	private Long skuId;
	/**
	 * sku_name
	 */
	private String skuName;
	/**
	 * 购买个数
	 */
	private Integer skuNum;
	/**
	 * 工作单id
	 */
	private Long taskId;

	/**
	 * 业务要求：
	 * 库存工作单：当库存锁定时（下单后），将创建一个订单对应库存的锁定任务，即 WareOrderTaskEntity
	 * 每个订单对应多个商品sku库存，即 WareOrderTaskDetailEntity
	 * 新增字段：wareId，lockStatus 用于代表当前sku商品对应的仓库id，可以定位到对应的仓库表WareSkuEntity记录，操作其锁定解锁
	 * lockStatus：1锁定 2解锁 3已扣减   用于分布式事物判断
	 */

	private Long wareId;

	private Integer lockStatus;

}
