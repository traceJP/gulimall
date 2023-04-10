package com.tracejp.gulimall.seckill.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/4/10 15:14
 */
@Data
public class SeckillSessionWithSkus {

    /**
     * id
     */
    @TableId
    private Long id;
    /**
     * 场次名称
     */
    private String name;
    /**
     * 每日开始时间
     */
    private Date startTime;
    /**
     * 每日结束时间
     */
    private Date endTime;
    /**
     * 启用状态
     */
    private Integer status;
    /**
     * 创建时间
     */
    private Date createTime;


    /**
     * 秒杀场次关联的秒杀商品
     */
    @TableField(exist = false)
    private List<SeckillSkuVo> relationSkus;

}
