package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.SpecGroupDTO;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.validate.group.MrOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "规格组接口")
public interface SpecificationServer {

    @ApiOperation(value = "查询规格组")
    @GetMapping(value = "specGroup/getSpecGroup")
    Result<List<SpecGroupEntity>> getSpecGroup(SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "新增规格组")
    @PostMapping(value = "specGroup/save")
    Result<JSONObject> save (@Validated(MrOperation.Add.class) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "修改规格组")
    @PutMapping(value = "specGroup/save")
    Result<JSONObject> edit(@Validated(MrOperation.Update.class) @RequestBody SpecGroupDTO specGroupDTO);

    @ApiOperation(value = "删除规格组")
    @DeleteMapping(value = "specGroup/delete")
    Result<JSONObject> delete(Integer id);
}

