package com.baidu.shop.service;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.validate.group.MrOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @ClassName UserService
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/10/13
 * @Version V1.0
 **/
@Api(tags = "用户接口")
public interface UserService {

    @ApiOperation(value = "用户注册")
    @PostMapping(value = "user/register")
    Result<JSONObject> register(@Validated({MrOperation.Add.class}) @RequestBody UserDTO userDTO);

    @ApiOperation(value = "验证用户名和手机号")
    @GetMapping(value = "user/check/{value}/{type}")
    Result<List<UserEntity>> checkUsernameOrPhone(@PathVariable(value = "value") String value, @PathVariable(value = "type") Integer type);

    @ApiOperation(value = "通过手机号发送短信验证码")
    @PostMapping(value = "user/sendValidCode")
    Result<JSONObject> sendValidCode(@RequestBody UserDTO userDTO);

    @ApiOperation(value = "验证短信验证码")
    @GetMapping(value = "user/checkCode")
    Result<JSONObject> checkCode(String phone,String code);
}
