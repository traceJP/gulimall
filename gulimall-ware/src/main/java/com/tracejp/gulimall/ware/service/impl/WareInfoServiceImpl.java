package com.tracejp.gulimall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracejp.common.utils.PageUtils;
import com.tracejp.common.utils.Query;
import com.tracejp.common.utils.R;
import com.tracejp.gulimall.ware.dao.WareInfoDao;
import com.tracejp.gulimall.ware.entity.WareInfoEntity;
import com.tracejp.gulimall.ware.feign.MemberFeignService;
import com.tracejp.gulimall.ware.service.WareInfoService;
import com.tracejp.gulimall.ware.vo.FareVo;
import com.tracejp.gulimall.ware.vo.MemberAddressVo;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    private MemberFeignService memberFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        LambdaQueryWrapper<WareInfoEntity> wrapper = new LambdaQueryWrapper<>();

        // 模糊查询处理
        String queryKey = (String) params.get("key");
        if (!StringUtils.isEmpty(queryKey)) {
            wrapper.eq(WareInfoEntity::getId, queryKey)
                    .or().like(WareInfoEntity::getName, queryKey)
                    .or().like(WareInfoEntity::getAddress, queryKey)
                    .or().like(WareInfoEntity::getAreacode, queryKey);
        }

        IPage<WareInfoEntity> page = this.page(new Query<WareInfoEntity>().getPage(params), wrapper);
        return new PageUtils(page);
    }

    @Override
    public FareVo getFare(Long addrId) {
        // 业务要求：运费是根据收货地址，距离最近的仓库来计算的
        // TODO 这里模拟只根据地址来计算运费
        FareVo fareVo = new FareVo();
        R r = memberFeignService.getAddrInfo(addrId);
        if (r.getCode() == 0) {
            MemberAddressVo memberReceiveAddress =
                    JSON.parseObject(JSON.toJSONString(r.get("memberReceiveAddress")), MemberAddressVo.class);
            // TODO 计算运费 这里默认都返回10块钱运费
            BigDecimal fare = new BigDecimal("10.00");
            fareVo.setFare(fare);
            fareVo.setAddress(memberReceiveAddress);
            return fareVo;
        }

        return null;
    }

}