package com.tracejp.gulimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * <p> 封装页面所有可能传递过来的查询条件 <p/>
 *
 * @author traceJP
 * @since 2023/3/20 19:37
 */
@Data
public class SearchParam {

    /**
     * 页面传递过来的全文匹配关键字
     */
    private String keyword;

    /**
     * 三级分类id
     */
    private Long catalog3Id;

    /**
     * 排序条件
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * 是否只显示有货
     * 0有货 1无货
     */
    private Integer hasStock;

    /**
     * 价格区间查询
     * skuPrice=1_500
     * skuPrice=_500
     * skuPrice=500_
     */
    private String skuPrice;

    /**
     * 按照品牌进行查询，可以多选
     */
    private List<Long> brandId;

    /**
     * 按照属性进行筛选
     * attrs=<>_<>:<>
     * attrs=1_5寸:8寸
     */
    private List<String> attrs;

    /**
     * 页码
     */
    private Integer pageNum = 1;


    // ======================= 页面传递过来的参数 end =======================

    private String _queryString;

}
