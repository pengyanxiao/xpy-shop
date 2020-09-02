package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.validate.group.MrOperation;
import com.google.gson.JsonObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName CategoryService
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/8/27
 * @Version V1.0
 **/
@Api(tags = "商品分类接口")
public interface CategoryService {

    @ApiOperation(value = "查询商品分类")
    @GetMapping(value = "category/list")
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid);

    @ApiOperation(value = "新增分类")
    @PostMapping(value = "category/add")
    Result<JSONObject> saveCategory(@Validated({MrOperation.Add.class}) @RequestBody CategoryEntity entity);

    @ApiOperation(value = "修改分类")
    @PutMapping(value = "category/edit")
    Result<JsonObject> editCategory(@Validated({MrOperation.Update.class}) @RequestBody CategoryEntity entity);

    @ApiOperation(value = "删除分类")
    @DeleteMapping(value = "category/delete")
    Result<JsonObject> deleteCategory(Integer id);

    @ApiOperation(value = "通过品牌id查询商品分类")
    @GetMapping(value = "category/getByBrand")
    public Result<List<CategoryEntity>> getByBrand(Integer brandId);

}
