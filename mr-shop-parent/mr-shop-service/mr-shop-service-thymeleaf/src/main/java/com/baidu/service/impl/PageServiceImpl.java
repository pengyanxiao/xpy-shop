package com.baidu.service.impl;

import com.baidu.feign.BrandFeign;
import com.baidu.feign.CategoryFeign;
import com.baidu.feign.GoodsFeign;
import com.baidu.feign.SpecificationFeign;
import com.baidu.service.PageService;
import com.baidu.shop.base.Result;
import com.baidu.shop.dto.*;
import com.baidu.shop.entity.*;
import com.baidu.shop.utils.BaiduBrandUtil;
import com.github.pagehelper.PageInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName PageServiceImpl
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/24
 * @Version V1.0
 **/
//@Service
public class PageServiceImpl implements PageService {

    //@Autowired
    private GoodsFeign goodsFeign;

    //@Autowired
    private BrandFeign brandFeign;

    //@Autowired
    private CategoryFeign categoryFeign;

//    @Autowired
    private SpecificationFeign specificationFeign;

    @Override
    public Map<String, Object> getPageInfoBySupId(Integer spuId) {

        Map<String, Object> map = new HashMap<>();

        SpuDTO spuDTO = new SpuDTO();
        spuDTO.setId(spuId);
        Result<List<SpuDTO>> spuInfoResult  = goodsFeign.getSpuInfo(spuDTO);
        if (spuInfoResult.getCode() == 200) {
            if(spuInfoResult.getData().size() == 1){
                //spu信息
                SpuDTO spuInfo = spuInfoResult.getData().get(0);
                map.put("spuInfo",spuInfo);
                //品牌信息
                BrandDTO brandDTO = new BrandDTO();
                brandDTO.setId(spuInfo.getBrandId());

                Result<PageInfo<BrandEntity>> brandInfoResult = brandFeign.getBrandInfo(brandDTO);
                if (brandInfoResult.getCode() == 200) {

                    PageInfo<BrandEntity> data = brandInfoResult.getData();

                    List<BrandEntity> brandList = data.getList();
                    if (brandList.size() == 1) {
                        map.put("brandInfo",brandList.get(0));
                    }
                }

                //分类信息
                Result<List<CategoryEntity>> categotyResult = categoryFeign.getCateByIds(String.join(",",
                        Arrays.asList(spuInfo.getCid1() + "", spuInfo.getCid2() + "", spuInfo.getCid3() + "")));
                if (categotyResult.getCode() == 200) {
                    List<CategoryEntity> categoryList = categotyResult.getData();
                    map.put("categoryList",categoryList);
                }

                //skus 通过spuId查询sku集合
                Result<List<SkuDTO>> skuResult = goodsFeign.getSkubySpu(spuInfo.getId());
                if (skuResult.getCode() == 200) {
                    List<SkuDTO> skuList = skuResult.getData();
                    map.put("skuList",skuList);
                }

                //特有规格参数
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spuInfo.getCid3());
                specParamDTO.setGeneric(false);
                Result<List<SpecParamEntity>> specParamInfoResult = specificationFeign.getSpecParam(specParamDTO);

                if (specParamInfoResult.getCode() == 200) {
                    List<SpecParamEntity> specParamList = specParamInfoResult.getData();
                    HashMap<Integer, String> specParamMap = new HashMap<>();
                    specParamList.stream().forEach(param -> {
                        specParamMap.put(param.getId(),param.getName());
                    });
                    map.put("specParamMap",specParamMap);
                }

                //spuDetail
                Result<SpuDetailEntity> detailResult = goodsFeign.getSpuDetailBySpu(spuInfo.getId());
                if (detailResult.getCode() == 200) {

                    SpuDetailEntity spuDetailInfo = detailResult.getData();
                    map.put("spuDetailInfo",spuDetailInfo);
                }

                //规格
                SpecGroupDTO specGroupDTO = new SpecGroupDTO();
                specGroupDTO.setCid(spuInfo.getCid3());
                Result<List<SpecGroupEntity>> specGroupResult = specificationFeign.getSpecGroup(specGroupDTO);
                if (specGroupResult.getCode() == 200) {

                    List<SpecGroupEntity> specGroupList = specGroupResult.getData();
                    List<SpecGroupDTO> specGroupDTOList = specGroupList.stream().map(specGroupEntity -> {
                        SpecGroupDTO specGroup = BaiduBrandUtil.copyProperties(specGroupEntity, SpecGroupDTO.class);

                        //GroupDTO
                        SpecParamDTO paramDTO = new SpecParamDTO();
                        paramDTO.setGroupId(specGroup.getId());
                        paramDTO.setGeneric(true);

                        Result<List<SpecParamEntity>> specParam = specificationFeign.getSpecParam(paramDTO);
                        if (specParam.getCode() == 200) {
                            specGroup.setParamList(specParam.getData());
                        }
                        return specGroup;
                    }).collect(Collectors.toList());
                    map.put("specGroupDTOList",specGroupDTOList);
                }
            }
        }

        return map;
    }
}
