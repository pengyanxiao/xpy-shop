package com.baidu.shop.entity;

import com.baidu.shop.validate.group.MrOperation;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @ClassName CategoryEntity
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/8/27
 * @Version V1.0
 **/
@Data
@ApiModel(value = "商品分类实体类")
@Table(name = "tb_category")
public class CategoryEntity {

    @Id
    @ApiModelProperty(value = "主键", example = "1")
    @NotNull(message = "id不能为空", groups = {MrOperation.Update.class})
    private Integer id;

    @ApiModelProperty(value = "类目名称")
    @NotEmpty(message = "名称不能为空" ,groups = {MrOperation.Add.class,MrOperation.Update.class})
    private String name;

    @ApiModelProperty(value = "父类目id", example = "1")
    @NotNull(message = "父类id不能为null" , groups = {MrOperation.Add.class})
    private Integer parentId;

    @ApiModelProperty(value = "是否为父节点,0为否，1为是", example = "0")
    @NotNull(message = "是否为父节点不能为null" , groups = {MrOperation.Add.class})
    private Integer isParent;

    @ApiModelProperty(value = "排序指数", example = "1")
    @NotNull(message = "排序指数不能为null",groups = {MrOperation.Add.class})
    private Integer sort;

}
