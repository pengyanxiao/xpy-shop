package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.CategoryMapper;
import com.baidu.shop.mapper.SpecGroupMapper;
import com.baidu.shop.mapper.SpuGoodsMapper;
import com.baidu.shop.entity.SpecGroupEntity;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.service.CategoryService;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.utils.ObjectUtil;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/8/27
 * @Version V1.0
 **/
@RestController
public class CategoryServiceImpl extends BaseApiService implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpecGroupMapper specGroupMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpuGoodsMapper spuGoodsMapper;

    @Override
    public Result<List<CategoryEntity>> getCategoryByPid(Integer pid) {

        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.setParentId(pid);
        List<CategoryEntity> list = categoryMapper.select(categoryEntity);

        return this.setResultSuccess(list);
    }

    @Transactional
    @Override
    public Result<JSONObject> saveCategory(CategoryEntity entity) {

        CategoryEntity parentCategoryEntity = new CategoryEntity();
        parentCategoryEntity.setId(entity.getParentId());
        parentCategoryEntity.setIsParent(1);

        categoryMapper.updateByPrimaryKeySelective(parentCategoryEntity);

        categoryMapper.insertSelective(entity);
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> editCategory(CategoryEntity entity) {
        categoryMapper.updateByPrimaryKeySelective(entity);
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> deleteCategory(Integer id) {

        CategoryEntity categoryEntity = categoryMapper.selectByPrimaryKey(id);
        if(ObjectUtil.isNull(id)){
            return this.setResultError("id不存在");
        }

        //判断选中的节点是否是父级节点
        if(categoryEntity.getIsParent() == 1){
            return this.setResultError("当前为父级节点 不能删除");
        }

        //构造函数
        Example example = new Example(CategoryEntity.class);
        example.createCriteria().andEqualTo("parentId",categoryEntity.getParentId());
        List<CategoryEntity> list = categoryMapper.selectByExample(example);


        //分类绑定商品删除
        Example example3 = new Example(SpuEntity.class);
        example3.createCriteria().andEqualTo("cid3",id);
        List<SpuEntity> list3 = spuGoodsMapper.selectByExample(example3);
        if(list3.size() > 0){
            return this.setResultError("当前节点下绑定商品,不能被删除");
        }

        //分类绑定规格组删除
        Example example1 = new Example(SpecGroupEntity.class);
        example1.createCriteria().andEqualTo("cid",id);
        List<SpecGroupEntity> list1 = specGroupMapper.selectByExample(example1);
        if(list1.size() == 1){
            return this.setResultError("当前父节点下有规格组,不能删除");
        }

        //分类绑定品牌删除
        Example example2 = new Example(CategoryBrandEntity.class);
        example2.createCriteria().andEqualTo("categoryId",id);
        List<CategoryBrandEntity> list2 = categoryBrandMapper.selectByExample(example2);
        if(list2.size() > 0){
            return this.setResultError("当前父节点绑定品牌,不可删除");
        }

        //查询
        if(!list.isEmpty() && list.size() == 1){
            CategoryEntity parenCategoryEntity = new CategoryEntity();
            parenCategoryEntity.setId(categoryEntity.getParentId());
            parenCategoryEntity.setIsParent(0);
            categoryMapper.updateByPrimaryKeySelective(parenCategoryEntity);

        }

        categoryMapper.deleteByPrimaryKey(id);//删除
        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<List<CategoryEntity>> getByBrand(Integer brandId) {

        List<CategoryEntity> byBrand = categoryMapper.getByBrand(brandId);

        return this.setResultSuccess(byBrand);
    }

    @Override
    public Result<List<CategoryEntity>> getCateByIds(String cidStr) {

        List<Integer> cateIdsArr = Arrays.asList(cidStr.split(","))
                .stream().map(idStr -> Integer.parseInt(idStr)).collect(Collectors.toList());

        List<CategoryEntity> list = categoryMapper.selectByIdList(cateIdsArr);

        return this.setResultSuccess(list);
    }


}
