package com.baidu.shop.service.impl;

import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryBrandEntity;
import com.baidu.shop.mapper.BrandMapper;
import com.baidu.shop.mapper.CategoryBrandMapper;
import com.baidu.shop.mapper.SpuGoodsMapper;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.entity.SpuEntity;
import com.baidu.shop.utils.BaiduBrandUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.PinyinUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.gson.JsonObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BrandServiceImpl
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/8/31
 * @Version V1.0
 **/
@RestController
public class BrandServiceImpl extends BaseApiService implements BrandService {

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    @Resource
    private SpuGoodsMapper spuGoodsMapper;

    //通过分类id获取品牌
    @Override
    public Result<List<BrandEntity>> getBrandByCategory(Integer cid) {

        List<BrandEntity> list = brandMapper.getBrandByCategory(cid);

        return this.setResultSuccess(list);
    }

    @Override
    public Result<List<BrandEntity>> getBrandById(String brandStr) {

        List<Integer> brandIdList = Arrays.asList(brandStr.split(",")).stream().map(brandIds ->
                Integer.parseInt(brandIds)).collect(Collectors.toList());

        List<BrandEntity> list = brandMapper.selectByIdList(brandIdList);

        return this.setResultSuccess(list);
    }

    @Override
    public Result<PageInfo<BrandEntity>> getBrandInfo(BrandDTO brandDTO) {
        //分页
        if(ObjectUtil.isNotNull(brandDTO.getPage()) && ObjectUtil.isNotNull(brandDTO.getRows())){
            PageHelper.startPage(brandDTO.getPage(),brandDTO.getRows());
        }
        //排序 条件查询
        Example example = new Example(BrandEntity.class);
        //if(ObjectUtil.isNotNull(sort)) example.setOrderByClause(sort +" "+(desc?"desc":""));
        if(StringUtil.isNotEmpty(brandDTO.getSort())) example.setOrderByClause(brandDTO.getOrderByClause());

        //条件查询
/*      Example.Criteria criteria = example.createCriteria();
        if (StringUtil.isNotEmpty(brandDTO.getName())) criteria.andLike("name","%" + brandDTO.getName() + "%");*/
        if(StringUtil.isNotEmpty(brandDTO.getName()))
            example.createCriteria().andLike("name","%" + brandDTO.getName()+ "%");

        Example.Criteria criteria = example.createCriteria();
        if(ObjectUtil.isNotNull(brandDTO.getId())){
            criteria.andEqualTo("id",brandDTO.getId());
        }

        //查询
        List<BrandEntity> list = brandMapper.selectByExample(example);
        //数据封装
        PageInfo<BrandEntity> pageInfo = new PageInfo<>(list);

        return this.setResultSuccess(pageInfo);
    }

    @Transactional
    @Override
    public Result<JsonObject> saveBrand(BrandDTO brandDTO) {
        //新增品牌并且可以返回主键
        BrandEntity brandEntity = BaiduBrandUtil.copyProperties(brandDTO, BrandEntity.class);

        //获取到品牌名称
        //获取到品牌名称第一个字符  将第一个字符转换为pinyin
        //获取拼音的首字母  统一转为大写
       /* String name = brandEntity.getName();
        char c = name.charAt(0);
        String upperCase = PinyinUtil.getUpperCase(String.valueOf(c), PinyinUtil.TO_FIRST_CHAR_PINYIN);
        brandEntity.setLetter(upperCase.charAt(0));*/

        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0)),
                PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        //新增数据
        brandMapper.insertSelective(brandEntity);
            //通过split方法分割字符串的Array
            //Arrays.asList将Array转换为List
            //使用JDK1,8的stream
            //使用map函数返回一个新的数据
            //collect 转换集合类型Stream<T>
            //Collectors.toList())将集合转换为List类型
           /* String[] cidArr = brandDTO.getCategory().split(",");
            List<String> list = Arrays.asList(cidArr);
            List<CategoryBrandEntity> categoryBrandEntities = new ArrayList<>();
            list.stream().forEach(cid -> {
                CategoryBrandEntity categoryBrandEntity = new CategoryBrandEntity();
                categoryBrandEntity.setCategoryId(StringUtil.toInteger(cid));
                categoryBrandEntity.setBrandId(brandEntity.getId());

                categoryBrandEntities.add(categoryBrandEntity);
            });*/
            //批量新增
           /*for (String s : cidArr) {
            CategoryBrandEntity entity = new CategoryBrandEntity();
            entity.setCategoryId(StringUtil.toInteger(s));
            entity.setBrandId(brandEntity.getId());

            categoryBrandMapper.insertSelective(entity);
         }*/
        //代码优化
        this.insertCategoryAndBrand(brandDTO,brandEntity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> editBrand(BrandDTO brandDTO) {

        BrandEntity brandEntity = BaiduBrandUtil.copyProperties(brandDTO, BrandEntity.class);
        //首字母
        brandEntity.setLetter(PinyinUtil.getUpperCase(String.valueOf(brandEntity.getName().charAt(0)),
                PinyinUtil.TO_FIRST_CHAR_PINYIN).charAt(0));

        //修改数据
        brandMapper.updateByPrimaryKeySelective(brandEntity);

        //删除 商品与品牌 中间表数据  代码优化
        this.deleteCategoryAndBrand(brandEntity.getId());

        //新增数据  代码优化
        this.insertCategoryAndBrand(brandDTO,brandEntity);

        return this.setResultSuccess();
    }

    @Transactional
    @Override
    public Result<JsonObject> deleteBrand(Integer id) {

        //品牌绑定商品,不能删除,
        Example example = new Example(SpuEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        List<SpuEntity> list = spuGoodsMapper.selectByExample(example);
        if(list.size() > 0){
            return this.setResultError("该品牌已绑定商品,不能被删除");
        }

        //删除操作
        brandMapper.deleteByPrimaryKey(id);
        //删除 商品与品牌 中间表数据  代码优化
        this.deleteCategoryAndBrand(id);

        return this.setResultSuccess();
    }

    //代码优化  (新增-修改)
    private void insertCategoryAndBrand(BrandDTO brandDTO,BrandEntity brandEntity){

        if(brandDTO.getCategory().contains(",")){
            //分割字符并转换类型....
            List<CategoryBrandEntity> categoryBrandEntities = Arrays.asList(brandDTO.getCategory().split(","))
                    .stream().map(cid -> {

                        CategoryBrandEntity entity = new CategoryBrandEntity();
                        entity.setCategoryId(StringUtil.toInteger(cid));
                        entity.setBrandId(brandEntity.getId());

                        return entity;
                    }).collect(Collectors.toList());
            //批量新增
            categoryBrandMapper.insertList(categoryBrandEntities);

        }else{
            //新增
            CategoryBrandEntity entity = new CategoryBrandEntity();
            entity.setCategoryId(StringUtil.toInteger(brandDTO.getCategory()));
            entity.setBrandId(brandEntity.getId());

            categoryBrandMapper.insertSelective(entity);
        }

    }

    //删除 商品与品牌 中间表数据  (修改-删除)
    private void deleteCategoryAndBrand(Integer id) {

        //删除中间表数据
        Example example = new Example(CategoryBrandEntity.class);
        example.createCriteria().andEqualTo("brandId",id);
        categoryBrandMapper.deleteByExample(example);

    }


}
