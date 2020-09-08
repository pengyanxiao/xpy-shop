package com.baidu.shop.mapper;

import com.baidu.shop.entity.BrandEntity;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<BrandEntity> {

    @Select(value = "SELECT b.* FROM tb_brand b , tb_category_brand c WHERE b.id = c.brand_id and c.category_id = #{cid}")
    List<BrandEntity> getBrandByCategory(Integer cid);
}
