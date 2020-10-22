package com.baidu.shop.business.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.business.OrderService;
import com.baidu.shop.config.JwtConfig;
import com.baidu.shop.constant.PhoneConstant;
import com.baidu.shop.dto.Car;
import com.baidu.shop.dto.OrderDTO;
import com.baidu.shop.dto.UserInfo;
import com.baidu.shop.entity.OrderDetailEntity;
import com.baidu.shop.entity.OrderEntity;
import com.baidu.shop.entity.OrderStatusEntity;
import com.baidu.shop.mapper.OrderDetailMapper;
import com.baidu.shop.mapper.OrderMapper;
import com.baidu.shop.mapper.OrderStatusMapper;
import com.baidu.shop.redis.repository.RedisRepository;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.IdWorker;
import com.baidu.shop.utils.JwtUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName OrderServiceImpl
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/10/21
 * @Version V1.0
 **/
@RestController
public class OrderServiceImpl extends BaseApiService implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Resource
    private OrderStatusMapper orderStatusMapper;

    @Resource
    private JwtConfig jwtConfig;

    @Resource
    private IdWorker idWorker;

    @Resource
    private RedisRepository redisRepository;

    @Transactional
    @Override
    public Result<String> createOrder(OrderDTO orderDTO, String token) {
        long orderId = idWorker.nextId();

        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtConfig.getPublicKey());


            Date date = new Date();

            OrderEntity orderEntity = new OrderEntity();

            orderEntity.setOrderId(orderId);
            orderEntity.setUserId(userInfo.getId() + "");
            orderEntity.setPaymentType(orderDTO.getPayType());//支付类型
            orderEntity.setBuyerMessage("还行...");//买家留言
            orderEntity.setBuyerNick(userInfo.getUsername());
            orderEntity.setBuyerRate(1);//买家是否评论:1没有
            orderEntity.setSourceType(1);//订单来源
            orderEntity.setInvoiceType(1);//发票类型
            orderEntity.setCreateTime(date);//订单生成时间

            //detail
            List<Long> longs = Arrays.asList(0L);

            List<OrderDetailEntity> orderDetailEntityList = Arrays.asList(orderDTO.getSkuIds().split(",")).stream().map(skuIdStr -> {
                //通过skuId查询redis --> sku数据
                Car hash = redisRepository.getHash(PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId(), skuIdStr, Car.class);

                if(hash == null){
                    throw new RuntimeException("数据异常");
                }

                OrderDetailEntity orderDetailEntity = new OrderDetailEntity();
                orderDetailEntity.setOrderId(orderId);
                orderDetailEntity.setSkuId(Long.valueOf(skuIdStr));
                orderDetailEntity.setNum(hash.getNum());
                orderDetailEntity.setTitle(hash.getTitle());
                orderDetailEntity.setPrice(hash.getPrice());
                orderDetailEntity.setImage(hash.getImage());

                longs.set(0, hash.getPrice() * hash.getNum() + longs.get(0));

                return orderDetailEntity;
            }).collect(Collectors.toList());

            orderEntity.setActualPay(longs.get(0));
            orderEntity.setTotalPay(longs.get(0));

            //status
            OrderStatusEntity orderStatusEntity = new OrderStatusEntity();
            orderStatusEntity.setOrderId(orderId);
            orderStatusEntity.setCreateTime(date);
            orderStatusEntity.setStatus(1);//没有支付

            //入库
            orderMapper.insertSelective(orderEntity);
            orderDetailMapper.insertList(orderDetailEntityList);
            orderStatusMapper.insertSelective(orderStatusEntity);

            //通过用户id和skuid删除购物车中的数据
            Arrays.asList(orderDTO.getSkuIds().split(",")).stream().forEach(skuidStr -> {
                redisRepository.delHash(PhoneConstant.USER_GOODS_CAR_PRE + userInfo.getId(),skuidStr);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this.setResult(HTTPStatus.OK,"",orderId + "");
    }
}
