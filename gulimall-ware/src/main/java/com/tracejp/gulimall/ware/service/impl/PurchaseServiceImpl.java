package com.tracejp.gulimall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tracejp.common.constant.WareConstant;
import com.tracejp.gulimall.ware.entity.PurchaseDetailEntity;
import com.tracejp.gulimall.ware.service.PurchaseDetailService;
import com.tracejp.gulimall.ware.service.WareSkuService;
import com.tracejp.gulimall.ware.vo.MergeVo;
import com.tracejp.gulimall.ware.vo.PurchaseDoneVo;
import com.tracejp.gulimall.ware.vo.PurchaseItemDoneVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;

import com.tracejp.gulimall.ware.dao.PurchaseDao;
import com.tracejp.gulimall.ware.entity.PurchaseEntity;
import com.tracejp.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    private PurchaseDetailService purchaseDetailService;

    @Autowired
    private WareSkuService wareSkuService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageUnreceivedList(Map<String, Object> params) {
        LambdaQueryWrapper<PurchaseEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PurchaseEntity::getStatus, WareConstant.PurchaseStatusEnum.CREATE)
                .or()
                .eq(PurchaseEntity::getStatus, WareConstant.PurchaseStatusEnum.ASSIGNED);

        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void mergePurchase(MergeVo mergeVo) {

        Long purchaseId = mergeVo.getPurchaseId();

        // 新建采购单
        if (purchaseId == null) {
            PurchaseEntity purchaseEntity = new PurchaseEntity();
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATE.getCode());
            this.save(purchaseEntity);
            purchaseId = purchaseEntity.getId();
        }

        // 合并
        List<Long> items = mergeVo.getItems();
        final Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> purchaseDetailEntities = items.stream().map(item -> {
            PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
            purchaseDetailEntity.setId(item);
            purchaseDetailEntity.setPurchaseId(finalPurchaseId);
            purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.ASSIGNED.getCode());
            return purchaseDetailEntity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(purchaseDetailEntities);

    }

    @Transactional
    @Override
    public void receivedPurchase(List<Long> ids) {
        List<PurchaseEntity> purchaseEntities = this.listByIds(ids).stream()
                .filter(item -> {
                    Integer status = item.getStatus();
                    return WareConstant.PurchaseStatusEnum.CREATE.getCode().equals(status) ||
                            WareConstant.PurchaseStatusEnum.ASSIGNED.getCode().equals(status);
                })
                .peek(item -> {
                    item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVE.getCode());

                    // 修改采购项
                    List<PurchaseDetailEntity> purchaseDetailEntities =
                            purchaseDetailService.listDetailByPurchaseId(item.getId());
                    purchaseDetailEntities.forEach(detailEntity ->
                            detailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.BUYING.getCode())
                    );
                    purchaseDetailService.updateBatchById(purchaseDetailEntities);

                })
                .collect(Collectors.toList());

        // 修改采购单
        this.updateBatchById(purchaseEntities);
    }

    @Transactional
    @Override
    public void finishPurchase(PurchaseDoneVo purchaseDoneVo) {

        // vo -> entity 收集
        List<PurchaseDetailEntity> purchaseDetailEntities = new LinkedList<>();

        // 判断采购项是否全部完成
        boolean hasError = false;
        List<PurchaseItemDoneVo> items = purchaseDoneVo.getItems();
        if (!CollectionUtils.isEmpty(items)) {
            for (PurchaseItemDoneVo item : items) {
                Integer status = item.getStatus();
                PurchaseDetailEntity purchaseDetailEntity = new PurchaseDetailEntity();
                // 失败判读
                if (WareConstant.PurchaseDetailStatusEnum.ERROR.getCode().equals(status)) {
                    hasError = true;
                    purchaseDetailEntity.setStatus(status);
                } else {  // 成功
                    purchaseDetailEntity.setStatus(WareConstant.PurchaseDetailStatusEnum.FINISH.getCode());

                    // 商品入库
                    PurchaseDetailEntity addStockEntity = purchaseDetailService.getById(item.getItemId());
                    wareSkuService.addStock(
                            addStockEntity.getSkuId(),
                            addStockEntity.getWareId(),
                            addStockEntity.getSkuNum()
                    );

                }
                purchaseDetailEntity.setId(item.getItemId());
                purchaseDetailEntities.add(purchaseDetailEntity);
            }
        }

        // 改变采购项状态
        if (!CollectionUtils.isEmpty(purchaseDetailEntities)) {
            purchaseDetailService.updateBatchById(purchaseDetailEntities);
        }

        // 改变采购单状态
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseDoneVo.getId());
        if (hasError) {
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.ERROR.getCode());
        } else {
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.FINISH.getCode());
        }
        this.updateById(purchaseEntity);

    }

}