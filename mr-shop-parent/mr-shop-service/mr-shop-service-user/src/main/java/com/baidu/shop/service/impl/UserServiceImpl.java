package com.baidu.shop.service.impl;

import ch.qos.logback.classic.Logger;
import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.constant.PhoneConstant;
import com.baidu.shop.constant.UsernameOrPhone;
import com.baidu.shop.dto.UserDTO;
import com.baidu.shop.entity.UserEntity;
import com.baidu.shop.mapper.UserMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.UserService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BCryptUtil;
import com.baidu.shop.utils.BaiduBrandUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName UserServiceImpl
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/10/13
 * @Version V1.0
 **/
@RestController
@Slf4j
public class UserServiceImpl extends BaseApiService implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisRepository redisRepository;

    @Override
    public Result<JSONObject> register(UserDTO userDTO) {

        UserEntity userEntity = BaiduBrandUtil.copyProperties(userDTO, UserEntity.class);
        userEntity.setPassword(BCryptUtil.hashpw(userEntity.getPassword(),BCryptUtil.gensalt()));

        userEntity.setCreated(new Date());

        userMapper.insertSelective(userEntity);
        return this.setResultSuccess();
    }

    @Override
    public Result<List<UserEntity>> checkUsernameOrPhone(String value, Integer type) {

        Example example = new Example(UserEntity.class);
        Example.Criteria criteria = example.createCriteria();

        if(type == UsernameOrPhone.USERNAME_CHECK_CONSTANT){
            //通过用户名验证
            criteria.andEqualTo("username",value);
        }else if(type == UsernameOrPhone.PHONE_CHECK_CONSTANT){
            //通过手机号验证
            criteria.andEqualTo("phone",type);
        }

        List<UserEntity> userEntities = userMapper.selectByExample(example);

        return this.setResultSuccess(userEntities);
    }

    @Override
    public Result<JSONObject> sendValidCode(UserDTO userDTO) {

        //随机生成6位验证码
        String code = (int)((Math.random() * 9 + 1) * 100000) + "";

        log.debug("发送手机验证码:手机号{} 短信验证码{}",userDTO.getPhone(),code);

        redisRepository.set(PhoneConstant.PHONE_VALID_CODE + userDTO.getPhone(),code);//短信验证码发送时间
        redisRepository.expire(PhoneConstant.PHONE_VALID_CODE + userDTO.getPhone(),120);//短信验证码有效时间

        //发送短信验证码
        //LuosimaoDuanxinUtil.sendSpeak(userDTO.getPhone(), code);

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> checkCode(String phone, String code) {

        String s = redisRepository.get(PhoneConstant.PHONE_VALID_CODE + phone);
        if(!code.equals(s)){
            return this.setResultError(HTTPStatus.VALIDATE_CODE_ERROR,"短信验证码输入错误");
        }

        return this.setResultSuccess();
    }

}
