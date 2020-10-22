package com.baidu.shop.dto;

import com.baidu.shop.validate.group.MrOperation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @ClassName Car
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/10/19
 * @Version V1.0
 **/
@ApiModel(value = "购物车数据")
@Data
public class Car {

    @ApiModelProperty(value = "用户ID",example = "1")
    private Integer userId;

    @ApiModelProperty(value = "skuId",example = "1")
    @NotNull(message = "skuID不能为空",groups = {MrOperation.Add.class})
    private Long skuId;

    @ApiModelProperty(value = "商品标题")
    @NotEmpty(message = "商品标题不能为空",groups = {MrOperation.Add.class})
    private String title;

    @ApiModelProperty(value = "商品图片")
    @NotEmpty(message = "商品图片不能为空",groups = {MrOperation.Add.class})
    private String image;

    @ApiModelProperty(value = "商品价格")
    @NotNull(message = "商品价格不能为空",groups = {MrOperation.Add.class})
    private Long price;

    @ApiModelProperty(value = "购买数量")
    @NotNull(message = "购买数量不能为空",groups = {MrOperation.Add.class})
    private Integer num;

    @ApiModelProperty(value = "规格参数")
    @NotEmpty(message = "规格参数不能为空",groups = {MrOperation.Add.class})
    private String ownSpec;
}
