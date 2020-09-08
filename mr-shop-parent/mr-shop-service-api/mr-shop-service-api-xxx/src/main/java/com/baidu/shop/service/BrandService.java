package com.baidu.shop.service;

import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.validate.group.MrOperation;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品品牌接口")
public interface BrandService {

    @ApiOperation("查询品牌管理")
    @GetMapping(value = "brand/getBrandInfo")
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO);

    @ApiOperation("新增品牌")
    @PostMapping(value = "brand/save")
    Result<JsonObject> saveBrand(@Validated({MrOperation.Add.class}) @RequestBody BrandDTO brandDTO);

    @ApiOperation("修改品牌")
    @PutMapping(value = "brand/save")
    Result<JsonObject> editBrand(@Validated(MrOperation.Update.class) @RequestBody BrandDTO brandDTO);

    @ApiOperation("删除品牌")
    @DeleteMapping(value = "brand/delete")
    Result<JsonObject> deleteBrand(Integer id);

    @ApiOperation(value = "通过分类id获取品牌")
    @GetMapping(value = "brand/getBrandByCategory")
    Result<List<BrandEntity>> getBrandByCategory(Integer cid);

}
