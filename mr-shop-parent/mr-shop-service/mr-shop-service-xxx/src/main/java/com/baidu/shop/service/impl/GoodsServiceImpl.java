package com.baidu.shop.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.component.MrRabbitMQ;
import com.baidu.shop.constant.MqMessageConstant;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.service.BrandService;
import com.baidu.shop.service.GoodsService;
import com.baidu.shop.dto.BrandDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.*;
import com.baidu.shop.mapper.*;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.BaiduBrandUtil;
import com.baidu.shop.utils.ObjectUtil;
import com.baidu.shop.utils.StringUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName GoodsServiceImpl
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/7
 * @Version V1.0
 **/
@RestController
public class GoodsServiceImpl extends BaseApiService implements GoodsService {

    @Resource
    private SpuGoodsMapper spuGoodsMapper;

    @Autowired
    private BrandService brandService;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private SpuDetailMapper spuDetailMapper;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private StockMapper stockMapper;

    @Autowired
    private MrRabbitMQ mrRabbitMQ;

    @Override
    public Result<SkuEntity> getSkuBySkuId(Long skuId) {

        SkuEntity skuEntity = skuMapper.selectByPrimaryKey(skuId);
        return this.setResultSuccess(skuEntity);
    }

    @Override
    public Result<List<SpuDTO>> getSpuInfo(SpuDTO spuDTO) {
        //分页
        if (ObjectUtil.isNotNull(spuDTO.getPage()) && ObjectUtil.isNotNull(spuDTO.getRows())) {
            PageHelper.startPage(spuDTO.getPage(), spuDTO.getRows());
        }

        //构造条件查询
        Example example = new Example(SpuEntity.class);
        //构建查询条件
        Example.Criteria criteria = example.createCriteria();
        //按标题模糊匹配
        if (StringUtil.isNotEmpty(spuDTO.getTitle())) {
            criteria.andLike("title", "%" + spuDTO.getTitle() + "%");
        }
        //如果值为2的话不进行拼接查询,默认查询所有
        if (ObjectUtil.isNotNull(spuDTO.getSaleable()) && spuDTO.getSaleable() != 2) {
            criteria.andEqualTo("saleable", spuDTO.getSaleable());
        }

        //查询spu
        if (ObjectUtil.isNotNull(spuDTO.getId())) {
            criteria.andEqualTo("id",spuDTO.getId());
        }

        //排序
        if (ObjectUtil.isNotNull(spuDTO.getSort())) {
            example.setOrderByClause(spuDTO.getOrderByClause());
        }

        List<SpuEntity> list = spuGoodsMapper.selectByExample(example);

        //代码优化
        List<SpuDTO> spuDtoList = list.stream().map(spuEntity -> {
            SpuDTO spuDTO1 = BaiduBrandUtil.copyProperties(spuEntity, SpuDTO.class);

            //设置品牌名称
            BrandDTO brandDTO = new BrandDTO();
            brandDTO.setId(spuEntity.getBrandId());
            Result<PageInfo<BrandEntity>> brandInfo = brandService.getBrandInfo(brandDTO);

            if (ObjectUtil.isNotNull(brandDTO)) {
                PageInfo<BrandEntity> data = brandInfo.getData();
                List<BrandEntity> list1 = data.getList();

                if (!list1.isEmpty() && list1.size() == 1) {
                    spuDTO1.setBrandName(list1.get(0).getName());
                }
            }

            //分类名称 拼接 通过cid1 cid2 cid3
//            List<CategoryEntity> categoryEntityList = categoryMapper.selectByIdList(Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()));
//            String caterogyName = categoryEntityList.stream().map(category -> category.getName()).collect(Collectors.joining("/"));
            //获取分类id-->转成数组-->通过idList查询出数据,得到数据集合-->调用stream函数-->调用map函数返回一个新的函数
            // -->调用Stream.collect转换集合-->调用Collectors.joining("/")将集合转换成/拼接的字符串
            String caterogyName = categoryMapper.selectByIdList(
                    Arrays.asList(spuDTO1.getCid1(), spuDTO1.getCid2(), spuDTO1.getCid3()))
                    .stream().map(category -> category.getName())
                    .collect(Collectors.joining("/"));

            spuDTO1.setCategoryName(caterogyName);
            return spuDTO1;
        }).collect(Collectors.toList());

//        List<SpuDTO> spuDTOS = new ArrayList<>();
//        list.stream().forEach(spuEntity -> {
//            //通过品牌id查询品牌名称
//            SpuDTO spuDTO1 = BaiduBeanUtil.copyProperties(spuEntity, SpuDTO.class);
//
//            BrandDTO brandDTO = new BrandDTO();
//            brandDTO.setId(spuEntity.getBrandId());
//
//            Result<PageInfo<BrandEntity>> brandInfo = brandService.getBrandInfo(brandDTO);
//            if (ObjectUtil.isNotNull(brandInfo)) {
//                PageInfo<BrandEntity> data = brandInfo.getData();
//                List<BrandEntity> list1 = data.getList();
//
//                if(!list1.isEmpty() && list1.size() == 1){
//                    spuDTO1.setBrandName(list1.get(0).getName());
//                }
//            }
//        });
//        PageInfo<SpuDTO> info = new PageInfo<>(spuDtoList);

        PageInfo<SpuEntity> spuDTOPageInfo = new PageInfo<>(list);
        long total = spuDTOPageInfo.getTotal();
        //借用一下message属性
        return this.setResult(HTTPStatus.OK,total + "",spuDtoList);
//        Map<String, Object> hashMap = new HashMap<>();
//        hashMap.put("list",spuDtoList);
//        hashMap.put("total",total);
//        return this.setResultSuccess(hashMap);
    }

    //@Transactional
    @Override
    public Result<JSONObject> save(SpuDTO spuDTO) {
        //System.out.println(spuDTO);

        Integer spuId = saveTransactional(spuDTO);

        //发送消息mq
        mrRabbitMQ.send(spuId + "", MqMessageConstant.SPU_ROUT_KEY_SAVE);

        return this.setResultSuccess();
   }

   @Transactional
   public Integer saveTransactional(SpuDTO spuDTO){
       Date date = new Date();
       //新增spu
       SpuEntity spuEntity = BaiduBrandUtil.copyProperties(spuDTO, SpuEntity.class);
       spuEntity.setSaleable(1);
       spuEntity.setValid(1);
       spuEntity.setCreateTime(date);
       spuEntity.setLastUpdateTime(date);
       spuGoodsMapper.insertSelective(spuEntity);

       Integer spuId = spuEntity.getId();
       //新增spuDetail
       SpuDetailEntity spuDetailEntity = BaiduBrandUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
       spuDetailEntity.setSpuId(spuId);
       spuDetailMapper.insertSelective(spuDetailEntity);

       //代码优化
       this.addSkusAndStocks(spuDTO.getSkus(),spuId,date);
       return spuEntity.getId();
   }

    @Override
    public Result<SpuDetailEntity> getSpuDetailBySpu(Integer spuId) {
        SpuDetailEntity spuDetailEntity = spuDetailMapper.selectByPrimaryKey(spuId);
        return this.setResultSuccess(spuDetailEntity);
    }

    @Override
    public Result<List<SkuDTO>> getSkubySpu(Integer spuId) {
        List<SkuDTO> list = skuMapper.selectSkuAndStockBySpuId(spuId);
        return this.setResultSuccess(list);
    }

    //@Transactional
    @Override
    public Result<JSONObject> edit(SpuDTO spuDTO) {
        //System.out.println(spuDTO);
        this.editTransaction(spuDTO);

        //发送消息mq
        mrRabbitMQ.send(spuDTO.getId() + "", MqMessageConstant.SPU_ROUT_KEY_UPDATE);

        return this.setResultSuccess();
    }
    @Transactional
    public void editTransaction(SpuDTO spuDTO){
        Date date = new Date();
        //修改spu
        SpuEntity spuEntity = BaiduBrandUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setLastUpdateTime(date);
        spuGoodsMapper.updateByPrimaryKeySelective(spuEntity);
        //修改spuDetail
        SpuDetailEntity spuDetailEntity = BaiduBrandUtil.copyProperties(spuDTO.getSpuDetail(), SpuDetailEntity.class);
        spuDetailMapper.updateByPrimaryKeySelective(spuDetailEntity);

        //修改sku,通过spuid删除sku,新增数据 代码优化
        this.deleteSkuIdAndSpuId(spuDTO.getId());

        //把新的数据新增到数据库 代码优化
        this.addSkusAndStocks(spuDTO.getSkus(),spuDTO.getId(),date);
    }

    //@Transactional
    @Override
    public Result<JSONObject> delete(Integer spuId) {
        this.deleteTransaction(spuId);

         //发送消息mq
        mrRabbitMQ.send(spuId + "", MqMessageConstant.SPU_ROUT_KEY_DELETE);

        return this.setResultSuccess();
    }
    @Transactional
    public void deleteTransaction(Integer spuId){
        //删除spu
        spuGoodsMapper.deleteByPrimaryKey(spuId);
        //删除spuDetail
        spuDetailMapper.deleteByPrimaryKey(spuId);

        //删除stock
        this.deleteSkuIdAndSpuId(spuId);

    }

    //商品上架与下架
    @Transactional
    @Override
    public Result<JSONObject> upAndDown(SpuDTO spuDTO) {

        SpuEntity spuEntity = BaiduBrandUtil.copyProperties(spuDTO, SpuEntity.class);
        spuEntity.setId(spuDTO.getId());

        if(spuEntity.getSaleable() == 1){
            spuEntity.setSaleable(0);
            spuGoodsMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("下架成功");
        }else{
            spuEntity.setSaleable(1);
            spuGoodsMapper.updateByPrimaryKeySelective(spuEntity);
            return this.setResultSuccess("上架成功");
        }
    }


    //删除代码优化
    private void deleteSkuIdAndSpuId(Integer spuId){
        Example example = new Example(SkuEntity.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        //通过spuId查询出来将要被删除的Sku
        List<Long> skuIdList = skuMapper.selectByExample(example)
                .stream().map(sku -> sku.getId()).collect(Collectors.toList());
        if(skuIdList.size() > 0){ //判断 以防止表删除
            //通过skuId集合删除sku
            skuMapper.deleteByIdList(skuIdList);
            //通过skuId集合删除stock
            stockMapper.deleteByIdList(skuIdList);
        }
    }

    //新增-修改提取代码重复(代码优化)
    private void addSkusAndStocks(List<SkuDTO> skus, Integer spuId, Date date){
        skus.stream().forEach(skuDTO -> {
            SkuEntity skuEntity = BaiduBrandUtil.copyProperties(skuDTO, SkuEntity.class);
            skuEntity.setSpuId(spuId);
            skuEntity.setCreateTime(date);
            skuEntity.setLastUpdateTime(date);
            skuMapper.insertSelective(skuEntity);

            StockEntity stockEntity = new StockEntity();
            stockEntity.setSkuId(skuEntity.getId());
            stockEntity.setStock(skuDTO.getStock());
            stockMapper.insertSelective(stockEntity);
        });
    }

}
