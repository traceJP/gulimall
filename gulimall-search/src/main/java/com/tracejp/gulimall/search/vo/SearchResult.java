package com.tracejp.gulimall.search.vo;

import com.tracejp.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>  <p/>
 *
 * @author traceJP
 * @since 2023/3/20 19:50
 */
@Data
public class SearchResult {

    /**
     * 所有商品信息
     */
    private List<SkuEsModel> products;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页码
     */
    private Integer totalPages;

    private List<Integer> pageNavs;


    /**
     * 当前查询结果涉及到的所有品牌
     */
    private List<BrandVo> brands;

    /**
     * 当前查询结果涉及到的所有属性
     */
    private List<AttrVo> attrs;

    private List<Long> attrIds = new ArrayList<>();

    /**
     * 当前查询结果涉及到的所有分类
     */
    private List<CatalogVo> catalogs;


    /**
     * 面包屑导航数据
     */
    private List<NavVo> navs = new ArrayList<>();


    @Data
    public static class BrandVo {

        private Long brandId;

        private String brandName;

        private String brandImg;

    }

    @Data
    public static class AttrVo {

        private Long attrId;

        private String attrName;

        private List<String> attrValue;

    }

    @Data
    public static class CatalogVo {

        private Long catalogId;

        private String catalogName;

        private String catalogValue;

    }

    @Data
    public static class NavVo {

        private String navName;

        private String navValue;

        private String link;

    }

}
