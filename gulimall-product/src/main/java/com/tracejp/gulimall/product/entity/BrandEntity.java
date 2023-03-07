package com.tracejp.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.tracejp.common.valid.AddGroup;
import com.tracejp.common.valid.ListValue;
import com.tracejp.common.valid.UpdateGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author tracejp
 * @email tracejp@163.com
 * @date 2023-02-23 19:11:22
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@NotNull(message = "修改时品牌id不能为空", groups = {UpdateGroup.class})
	@Null(message = "新增时品牌id必须为空", groups = {AddGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@URL(message = "logo必须是一个合法的URL地址")
	private String logo;
	/**
	 * 介绍
	 */
	@NotBlank(message = "介绍不能为空")
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	// 这里的显示状态 是真的显示状态 不是MP逻辑位
	// 注意：这里不要加 TableLogic 注解， MP 不允许修改逻辑位 ， 会导致 showStatus 无法修改
	// @TableLogic(value = "1", delval = "0")
	@ListValue(value = {0, 1}, message = "显示状态必须是0或1")
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@Pattern(regexp="^[a-zA-Z]$", message = "检索首字母必须是一个字母")
	private String firstLetter;
	/**
	 * 排序
	 */
	@Min(value = 0, message = "排序必须大于等于0")
	private Integer sort;

}
