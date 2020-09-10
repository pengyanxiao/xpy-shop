package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.SpuDetailEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "商品列表接口")
public interface GoodsService {

    @ApiOperation(value = "查询spu")
    @GetMapping(value = "goods/getSpuInfo")
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO);

    @ApiOperation(value = "新增商品")
    @PostMapping(value = "goods/add")
    Result<JSONObject> save(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "获取spu详细信息")
    @GetMapping(value = "goods/getSpuDetailBySpu")
    Result<SpuDetailEntity> getSpuDetailBySpu(Integer spuId);

    @ApiOperation(value = "获取sku信息")
    @GetMapping(value = "goods/getSkubySpu")
    Result<List<SkuDTO>> getSkubySpu(Integer spuId);

    @ApiOperation(value = "修改商品")
    @PutMapping(value = "goods/add")
    Result<JSONObject> edit(@RequestBody SpuDTO spuDTO);

    @ApiOperation(value = "删除商品")
    @DeleteMapping(value = "goods/delete")
    Result<JSONObject> delete(Integer spuId);

    @ApiOperation(value = "商品上下架")
    @PutMapping(value = "goods/upAndDown")
    Result<JSONObject> upAndDown(@RequestBody SpuDTO spuDTO);

}
