package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @ClassName TemplateService
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/25
 * @Version V1.0
 **/
@Api(tags = "页面静态化模板接口")
public interface TemplateService {

    @ApiOperation(value = "生成静态HTML文件")
    @GetMapping(value = "template/createStaticHTMLTemplate")
    Result<JSONObject> createStaticHTMLTemplate(Integer spuId);

    @ApiOperation(value = "初始化静态HTML")
    @GetMapping(value = "template/initStaticHTMLTemplate")
    Result<JSONObject> initStaticHTMLTemplate();

}
