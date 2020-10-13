package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.response.GoodsResponse;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Delete;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @ClassName ShopElasticsearchService
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/16
 * @Version V1.0
 **/
@Api(tags = "es接口")
public interface ShopElasticsearchService {

    @ApiOperation(value = "清空ES中的商品数据")
    @GetMapping(value = "es/clearGoodsEsData")
    Result<JsonObject> clearGoodsEsData();


    @ApiOperation(value = "ES商品数据初始化")
    @GetMapping(value = "es/initGoodsEsData")
    Result<JSONObject> initGoodsEsData();

    @ApiOperation(value = "查询搜索")
    @GetMapping(value = "es/search")
    GoodsResponse search(String search, Integer page, String filter);

    @ApiOperation(value = "新增数据到es")
    @PostMapping(value = "es/saveData")
    Result<JSONObject> saveData(Integer spuId);

    @ApiOperation(value = "通过id删除es数据")
    @DeleteMapping(value = "es/delData")
    Result<JSONObject> delData(Integer spuId);
}
