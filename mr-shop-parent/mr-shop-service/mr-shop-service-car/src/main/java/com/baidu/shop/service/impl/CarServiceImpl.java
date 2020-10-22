package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.PhoneConstant;
import com.baidu.shop.dto.Car;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.SkuEntity;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.service.CarService;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.JwtUtils;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @ClassName CarServiceImpl
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/10/19
 * @Version V1.0
 **/
@RestController
@Slf4j
public class CarServiceImpl extends BaseApiService implements CarService {

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private JwtConfig jwtConfig;

    @Override
    public Result<List<Car>> updateNum(Long skuId, Integer type, String token) {

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());
            Car redisCar = redisRepository.getHash(PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId(),skuId+"",Car.class);

//            if(type == 1){
//                redisCar.setNum(redisCar.getNum() + 1);
//            }else{
//                redisCar.setNum(redisCar.getNum() -1);
//            }
            redisCar.setNum(type == PhoneConstant.CAR_OPERATION_INCREMENT ? redisCar.getNum() + 1 : redisCar.getNum() - 1);
            redisRepository.setHash(PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                    ,skuId + "",JSONUtil.toJsonString(redisCar));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultSuccess();
    }

    @Override
    public Result<List<Car>> userGoodsCar(String token) {

        try {

            List<Car> carList = new ArrayList<>();
            //获取当前d登录用户
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());
            //通过用户id从redis获取购物车数据
            Map<String, String> map = redisRepository.getHash(PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId());

            map.forEach((key,value) -> {
                Car car = JSONUtil.toBean(value, Car.class);
                carList.add(car);

            });

            return this.setResultSuccess(carList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResultSuccess("内部错误");
    }

    @Override
    public Result<JSONObject> mergeCar(String clientCarList, String token) {

        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(clientCarList);

        List<Car> carList = com.alibaba.fastjson.JSONObject.parseArray(jsonObject.get("clientCarList").toString(), Car.class);

        carList.stream().forEach(car -> {
            this.addCar(car,token);
        });

        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> addCar(Car car, String token) {

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());

            //通过userid和skuid获取商品数据
            Car redisCar = redisRepository.getHash(PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                    , car.getSkuId() + "", Car.class);
            log.debug("通过key : {} ,skuid : {} 获取到的数据为 : {}",PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                    ,car.getSkuId(),redisCar);

            if (ObjectUtil.isNotNull(redisCar)) {
                redisCar.setNum(car.getNum() + redisCar.getNum() );

                redisRepository.setHash(PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                        ,car.getSkuId() + "",JSONUtil.toJsonString(redisCar));
                log.debug("当前用户购物车中有将要新增的商品，重新设置num : {}" , redisCar.getNum());

            }else{
                Result<SkuEntity> skuResult = goodsFeign.getSkuBySkuId(car.getSkuId());
                if (skuResult.getCode() == 200) {

                    SkuEntity data = skuResult.getData();
                    car.setTitle(data.getTitle());
                    car.setImage(StringUtil.isNotEmpty(data.getImages()) ? data.getImages().split(",")[0] : "");
                    car.setOwnSpec(data.getOwnSpec());
                    car.setPrice(Long.valueOf(data.getPrice()));
                    car.setUserId(userInfo.getId());

                    redisRepository.setHash(PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                            ,car.getSkuId() + "",JSONUtil.toJsonString(car));

                    log.debug("新增商品到购物车redis,KEY : {} , skuid : {} , car : {}",PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId(),car.getSkuId(),JSONUtil.toJsonString(car));
                }
            }

            redisRepository.setHash(PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId()
                    ,car.getSkuId() + "",JSONUtil.toJsonString(car));
            log.debug("新增到redis数据成功");

        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println("========");
        return this.setResultSuccess();
    }
}
