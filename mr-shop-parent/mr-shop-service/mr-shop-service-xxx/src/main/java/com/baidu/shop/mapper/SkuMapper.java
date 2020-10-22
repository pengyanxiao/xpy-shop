package com.baidu.shop.mapper;

import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.entity.SkuEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuMapper extends Mapper<SkuEntity> , DeleteByIdListMapper<SkuEntity,Long> {

    @Select(value = "SELECT s.*, s.own_spec as ownSpec ,stock FROM tb_sku s, tb_stock t WHERE s.id = t.sku_id AND s.spu_id = #{supId}")
    List<SkuDTO> selectSkuAndStockBySpuId(Integer supId);

}
