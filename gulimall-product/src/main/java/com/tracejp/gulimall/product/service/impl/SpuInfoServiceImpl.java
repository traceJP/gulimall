package com.tracejp.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.constant.ProductConstant;
import com.tracejp.common.exception.BizCodeEnum;
import com.tracejp.common.to.SkuHasStockTo;
import com.tracejp.common.to.SkuReductionTo;
import com.tracejp.common.to.SpuBoundTo;
import com.tracejp.common.to.es.SkuEsModel;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.product.dao.SkuInfoDao;
import com.tracejp.gulimall.product.dao.SpuInfoDao;
import com.tracejp.gulimall.product.dao.SpuInfoDescDao;
import com.tracejp.gulimall.product.entity.*;
import com.tracejp.gulimall.product.feign.CouponFeignService;
import com.tracejp.gulimall.product.feign.SearchFeignService;
import com.tracejp.gulimall.product.feign.WareFeignService;
import com.tracejp.gulimall.product.service.*;
import com.tracejp.gulimall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SpuInfoDescDao spuInfoDescDao;

    @Autowired
    private SkuInfoDao skuInfoDao;


    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        // 保存 spu基本信息 pms_spu_info
        // MP save方法保存后 会自动返回封装自增主键
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        this.save(spuInfoEntity);
        Long spuId = spuInfoEntity.getId();

        // 保存 spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        if (!CollectionUtils.isEmpty(decript)) {
            SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
            spuInfoDescEntity.setSpuId(spuId);
            spuInfoDescEntity.setDecript(String.join(",", decript));
            spuInfoDescDao.insert(spuInfoDescEntity);
        }

        // 保存 spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImagesBatch(spuId, images);

        // 保存 spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setSpuId(spuId);
                productAttrValueEntity.setAttrId(attr.getAttrId());
                productAttrValueEntity.setAttrValue(attr.getAttrValues());
                productAttrValueEntity.setQuickShow(attr.getShowDesc());

                // 查 attrName
                AttrEntity attrEntity = attrService.getById(attr.getAttrId());
                productAttrValueEntity.setAttrName(attrEntity.getAttrName());

                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(productAttrValueEntities);
        }

        // 保存 spu的积分信息 gulimall_sms -> sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuId);
        couponFeignService.saveSpuBounds(spuBoundTo);

        // 保存 当前spu对应的所有sku信息
        List<Skus> skus = vo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            skus.forEach(item -> {

                // 默认图片
                String defaultImg = "";
                for (Images image : item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }

                //  1. sku的基本信息 pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(skuInfoEntity.getBrandId());
                skuInfoEntity.setSpuId(spuId);
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoDao.insert(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                //  2. sku的图片信息 pms_sku_images
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    BeanUtils.copyProperties(img, skuImagesEntity);
                    skuImagesEntity.setSkuId(skuId);
                    return skuImagesEntity;
                })
                        // 没有图片信息不需要保存
                        .filter(img -> !StringUtils.isEmpty(img.getImgUrl()))
                        .collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntities);

                //  3. sku的销售属性信息 pms_sku_sale_attr_value
                List<Attr> attrs = item.getAttr();
                if (!CollectionUtils.isEmpty(attrs)) {
                    List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                        SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                        BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                        skuSaleAttrValueEntity.setSkuId(skuId);
                        return skuSaleAttrValueEntity;
                    }).collect(Collectors.toList());
                    skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                }

                //  4. sku的优惠、满减等信息 gulimall_sms -> sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0
                        || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    couponFeignService.saveSkuReduction(skuReductionTo);
                }

            });

        }

    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {

        LambdaQueryWrapper<SpuInfoEntity> wrapper = new LambdaQueryWrapper<SpuInfoEntity>();


        // status=1 and 模糊(id=1 or spuName like %xx%)

        String queryKey = (String) params.get("key");
        if (!StringUtils.isEmpty(queryKey)) {
            wrapper.and(w -> w.eq(SpuInfoEntity::getSpuName, queryKey).or().like(SpuInfoEntity::getId, queryKey));
        }

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq(SpuInfoEntity::getPublishStatus, status);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !brandId.equals("0")) {
            wrapper.eq(SpuInfoEntity::getBrandId, brandId);
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !catelogId.equals("0")) {
            wrapper.eq(SpuInfoEntity::getCatalogId, catelogId);
        }

        IPage<SpuInfoEntity> page = this.page(new Query<SpuInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);

    }

    @Transactional
    @Override
    public void spuUp(Long spuId) {

        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        // 查询 attrs
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> originalBaseAttrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        // 去重 ： 去掉 attrIds中没有filterAttrIds的属性
        List<Long> filterAttrIds = attrService.selectSearchAttrs(originalBaseAttrIds);
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream()
                .filter(item -> filterAttrIds.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attr = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attr);
                    return attr;
                })
                .collect(Collectors.toList());

        // 远程 查询库存信息
        List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        // 如何在循环中根据 id 查找到 skuHasStockTos 中的数据 ？ 将 skuHasStockTos 转换为 map，以 id 作为 key 即可 ！
        Map<Long, Boolean> skuHasStockMap = null;
        try {
            R skusHasStocks = wareFeignService.getSkusHasStock(skuIds);
            // 这里 序列化有问题 - 已解决 采用 JSON 转换 ：Object -> Json -> List<Object>
            List<SkuHasStockTo> skuHasStockTos = JSON.parseArray(JSON.toJSONString(skusHasStocks.get("data")), SkuHasStockTo.class);
            if (skuHasStockTos != null) {
                skuHasStockMap = skuHasStockTos.stream()
                        .collect(Collectors.toMap(SkuHasStockTo::getSkuId, SkuHasStockTo::getHasStock));
            }
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e.toString());
        }

        final Map<Long, Boolean> finalSkuHasStockMap = skuHasStockMap;
        List<SkuEsModel> skuEsModelList = skus.stream().map(sku -> {

            // build skuEsModel
            SkuEsModel esModel = new SkuEsModel();

            BeanUtils.copyProperties(sku, esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            // 查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(sku.getBrandId());
            esModel.setBrandName(brandEntity.getName());
            esModel.setBrandImg(brandEntity.getLogo());

            // TODO 热度评分
            esModel.setHotScore(0L);

            // 查询分类信息
            CategoryEntity categoryEntity = categoryService.getById(sku.getCatalogId());
            esModel.setCatalogName(categoryEntity.getName());

            // 查询规格属性
            esModel.setAttrs(attrsList);

            // 设置库存信息
            esModel.setHasStock(finalSkuHasStockMap.getOrDefault(sku.getSkuId(), true));

            return esModel;
        }).collect(Collectors.toList());


        // 发送给 es 进行保存
        R r = searchFeignService.productStatusUp(skuEsModelList);
        if ((Integer) r.get("code") == BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode()) {
            // TODO 远程调用失败
        } else {
            // 修改当前 spu 的状态
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        Long spuId = skuInfoEntity.getSpuId();
        return getById(spuId);
    }

}