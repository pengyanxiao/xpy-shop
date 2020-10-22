package com.baidu.shop.entity;

import io.swagger.models.auth.In;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName OrderStatusEntity
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/10/21
 * @Version V1.0
 **/
@Data
@Table(name = "tb_order_status")
public class OrderStatusEntity {

    @Id
    private Long orderId;

    private Integer status;

    private Date createTime;

    private Date paymentTime;

}
