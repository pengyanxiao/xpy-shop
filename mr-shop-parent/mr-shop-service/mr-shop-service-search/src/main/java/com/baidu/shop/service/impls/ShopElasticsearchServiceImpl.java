package com.baidu.shop.service.impls;

import com.alibaba.fastjson.JSONObject;
import com.baidu.shop.base.BaseApiService;
import com.baidu.shop.base.Result;
import com.baidu.shop.document.GoodsDoc;
import com.baidu.shop.dto.SkuDTO;
import com.baidu.shop.dto.SpecParamDTO;
import com.baidu.shop.dto.SpuDTO;
import com.baidu.shop.entity.BrandEntity;
import com.baidu.shop.entity.CategoryEntity;
import com.baidu.shop.entity.SpecParamEntity;
import com.baidu.shop.entity.SpuDetailEntity;
import com.baidu.shop.feign.BrandFeign;
import com.baidu.shop.feign.CategoryFeign;
import com.baidu.shop.feign.GoodsFeign;
import com.baidu.shop.feign.SpecificationFeign;
import com.baidu.shop.response.GoodsResponse;
import com.baidu.shop.service.ShopElasticsearchService;
import com.baidu.shop.status.HTTPStatus;
import com.baidu.shop.utils.ESHighLightUtil;
import com.baidu.shop.utils.JSONUtil;
import com.baidu.shop.utils.StringUtil;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ShopElasticsearchServiceImpl
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/16
 * @Version V1.0
 **/
@RestController
@Slf4j
public class ShopElasticsearchServiceImpl extends BaseApiService implements ShopElasticsearchService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private SpecificationFeign specificationFeign;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private CategoryFeign categoryFeign;

    @Autowired
    private BrandFeign brandFeign;

    private List<GoodsDoc> esGoodsInfo() {
        SpuDTO spuDTO = new SpuDTO();
//        spuDTO.setPage(1);
//        spuDTO.setRows(5);
        Result<List<SpuDTO>> spuInfo = goodsFeign.getSpuInfo(spuDTO);

        List<GoodsDoc> goodsDocs = new ArrayList<>();

        if(spuInfo.getCode() == HTTPStatus.OK){

            //spu数据
            spuInfo.getData().stream().forEach(spu -> {

                Integer spuId = spu.getId();

                GoodsDoc goodsDoc = new GoodsDoc();

                goodsDoc.setId(spuId.longValue());
                goodsDoc.setTitle(spu.getTitle());
                goodsDoc.setSubTitle(spu.getSubTitle());
                goodsDoc.setCid1(spu.getCid1().longValue());
                goodsDoc.setCid2(spu.getCid2().longValue());
                goodsDoc.setCid3(spu.getCid3().longValue());
                goodsDoc.setBrandName(spu.getBrandName());
                goodsDoc.setCategoryName(spu.getCategoryName());

                goodsDoc.setBrandId(spu.getBrandId().longValue());
                //通过spuID查询skuList
                Result<List<SkuDTO>> skuResult = goodsFeign.getSkubySpu(spu.getId());

                if (skuResult.getCode() == HTTPStatus.OK) {

                    List<SkuDTO> skuList = skuResult.getData();
                    List<Long> priceList = new ArrayList<>();

                    List<HashMap<Object, Object>> skuListMap = skuList.stream().map(sku -> {

                        HashMap<Object, Object> map = new HashMap<>();

                        map.put("id", sku.getId());
                        map.put("title", sku.getTitle());
                        map.put("image", sku.getImages());
                        map.put("price", sku.getPrice());

                        priceList.add(sku.getPrice().longValue());
                        return map;
                    }).collect(Collectors.toList());

                    goodsDoc.setPrice(priceList);
                    goodsDoc.setSkus(JSONUtil.toJsonString(skuListMap));

                }

                //获取规格参数
                SpecParamDTO specParamDTO = new SpecParamDTO();
                specParamDTO.setCid(spu.getCid3());
                Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParam(specParamDTO);

                Map<String, Object> specMap = new HashMap<>();

                if (specParamResult.getCode() == HTTPStatus.OK) {

                    List<SpecParamEntity> paramList = specParamResult.getData();

                    Result<SpuDetailEntity> spuDetailResult = goodsFeign.getSpuDetailBySpu(spu.getId());


                    if(spuDetailResult.getCode() == HTTPStatus.OK){
                        SpuDetailEntity spuDetaiInfo = spuDetailResult.getData();

                        //通用规格参数的值
                        String genericSpecStr = spuDetaiInfo.getGenericSpec();
                        Map<String, String> genericSpecMap = JSONUtil.toMapValueString(genericSpecStr);

                        //特有规格参数的值
                        String specialSpecStr = spuDetaiInfo.getSpecialSpec();
                        Map<String, List<String>> specialSpecMap = JSONUtil.toMapValueStrList(specialSpecStr);

                        paramList.stream().forEach(param -> {

                            if (param.getGeneric()) {

                                if(param.getNumeric() && param.getSearching()){
                                    specMap.put(param.getName(), this.chooseSegment(genericSpecMap.get(param.getId() + ""),param.getSegments(),param.getUnit()));
                                }else{
                                    specMap.put(param.getName(), genericSpecMap.get(param.getId() + ""));
                                }
                            } else {
                                specMap.put(param.getName(), specialSpecMap.get(param.getId().toString()));
                            }
                        });
                    }
                }
                goodsDoc.setSpecs(specMap);
                goodsDocs.add(goodsDoc);
            });
        }
        return goodsDocs;

    }

    private String chooseSegment(String value, String segments, String unit) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : segments.split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + unit + "以上";
                }else if(begin == 0){
                    result = segs[1] + unit + "以下";
                }else{
                    result = segment + unit;
                }
                break;
            }
        }
        return result;
    }

    @Override
    public Result<JsonObject> clearGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(indexOperations.exists()){
            indexOperations.delete();
            log.info("索引删除成功");
        }
        return this.setResultSuccess();
    }

    @Override
    public Result<JSONObject> initGoodsEsData() {
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsDoc.class);
        if(!indexOperations.exists()){
            indexOperations.create();
            log.info("索引创建成功");
            indexOperations.createMapping();
            log.info("映射创建成功");
        }
        //批量新增
        List<GoodsDoc> goodsDocs = this.esGoodsInfo();
        elasticsearchRestTemplate.save(goodsDocs);

        return this.setResultSuccess();
    }

    @Override
    public GoodsResponse search(String search, Integer page, String filter) {
        //判断内容不能为空
        if(StringUtils.isEmpty(search)) throw new RuntimeException("内容不能为空");

        SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(this.getSearchQueryBuilder(search,page,filter).build(),GoodsDoc.class);
        List<SearchHit<GoodsDoc>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits.getSearchHits());

        //返回的数据
        List<GoodsDoc> goodsDocList = highLightHit.stream().map(searchHit -> searchHit.getContent()).collect(Collectors.toList());

        //分页 总条数 总页数
        long total = searchHits.getTotalHits();
        long totalPage = Double.valueOf(Math.ceil(Long.valueOf(total).doubleValue() / 10)).longValue();

        //获取聚合函数数据
        Aggregations aggregations = searchHits.getAggregations();
        //获得分类集合
        Map<Integer, List<CategoryEntity>> map = this.getCategoryList(aggregations);

        List<CategoryEntity> categoryList = null;
        Integer hotCid = 0;

        //遍历map
        for(Map.Entry<Integer,List<CategoryEntity>> mapEntry : map.entrySet()){
            hotCid = mapEntry.getKey();
            categoryList = mapEntry.getValue();
        }
        //通过cid去查询规格参数
        Map<String, List<String>> specParamMap = this.getSpecParam(hotCid, search);

        //获取品牌集合
        List<BrandEntity> brandList = this.getBrandList(aggregations);

        GoodsResponse goodsResponse = new GoodsResponse(total, totalPage, brandList, categoryList,goodsDocList,specParamMap);

        return goodsResponse;
    }

    private Map<String, List<String>> getSpecParam(Integer hotCid,String search){
        SpecParamDTO specParamDTO = new SpecParamDTO();
        specParamDTO.setCid(hotCid);
        specParamDTO.setSearching(true);//只搜索有查询属性的规格参数
        Result<List<SpecParamEntity>> specParamResult = specificationFeign.getSpecParam(specParamDTO);

        if(specParamResult.getCode() == 200){
            List<SpecParamEntity> specParamList = specParamResult.getData();

            //聚合查询
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
            queryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName"));
            //分页
            queryBuilder.withPageable(PageRequest.of(0,1));

            specParamList.stream().forEach(speaParam -> {
                queryBuilder.addAggregation(AggregationBuilders.terms(speaParam.getName()).field("specs." + speaParam.getName() + ".keyword"));
            });

            SearchHits<GoodsDoc> searchHits = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsDoc.class);

            //key:paramName value-->aggr
            Map<String, List<String>> map = new HashMap<>();
            Aggregations aggregations = searchHits.getAggregations();
            specParamList.stream().forEach(speaParam -> {
                Terms terms = aggregations.get(speaParam.getName());
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                List<String> valueList = buckets.stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
                map.put(speaParam.getName(),valueList);
            });

            return map;
        }
        return null;
    }

    //构建查询条件
    private NativeSearchQueryBuilder getSearchQueryBuilder(String search, Integer page, String filter){
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();

        if(StringUtil.isNotEmpty(filter) && filter.length() > 2){

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            Map<String, String> filterMap = JSONUtil.toMapValueString(filter);

            filterMap.forEach((key,value) -> {
                MatchQueryBuilder matchQueryBuilder = null;

                //分类 品牌和 规格参数的查询方式不一样
                if(key.equals("cid3") || key.equals("brandId")){
                    matchQueryBuilder = QueryBuilders.matchQuery(key, value);
                }else{
                    matchQueryBuilder = QueryBuilders.matchQuery("specs."+ key + ".keyword",value);
                }
                boolQueryBuilder.must(matchQueryBuilder);
            });
            searchQueryBuilder.withFilter(boolQueryBuilder);
        }

        //match通过值只能查询一个字段 和 multiMatch 通过值查询多个字段???
        searchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(search,"title","brandName","categoryName"));
        //聚合(品牌-分类)
        searchQueryBuilder.addAggregation(AggregationBuilders.terms("cid_agg").field("cid3").size(10000));
        searchQueryBuilder.addAggregation(AggregationBuilders.terms("brand_agg").field("brandId").size(10000));
        //高亮
        searchQueryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));
        //分页
        searchQueryBuilder.withPageable(PageRequest.of(page-1,10));

        return searchQueryBuilder;
    }

    //获得品牌集合
    private List<BrandEntity> getBrandList(Aggregations aggregations){
        Terms brand_agg = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> brand_aggBuckets = brand_agg.getBuckets();
        List<String> brandList = brand_aggBuckets.stream().map(brandBuckets ->
                brandBuckets.getKeyAsNumber().intValue() + "").collect(Collectors.toList());
        //通过品牌id集合去查询数据
        Result<List<BrandEntity>> brandResult = brandFeign.getBrandById(String.join(",", brandList));
        return brandResult.getData();
    }

    //获得分类集合
    private Map<Integer, List<CategoryEntity>> getCategoryList(Aggregations aggregations){
        Terms cid_agg = aggregations.get("cid_agg");
        List<? extends Terms.Bucket> cid_aggBuckets = cid_agg.getBuckets();

        //热量
        List<Integer> hotCidArr  = Arrays.asList(0);
        List<Long> maxCount = Arrays.asList(0L);
        Map<Integer, List<CategoryEntity>> map = new HashMap<>();

        //返回一个id的集合-->通过id的集合去查询数据
        //不使用lambda表达式-->forEach
        List<String> cidList = cid_aggBuckets.stream().map(cidBuckets -> {
            Number keyAsNumber = cidBuckets.getKeyAsNumber();

            if(cidBuckets.getDocCount() > maxCount.get(0)){
                maxCount.set(0,cidBuckets.getDocCount());
                hotCidArr.set(0, keyAsNumber.intValue());
            }

            return keyAsNumber.intValue() + "";
            //String keyAsString = cidBuckets.getKeyAsString();
        }).collect(Collectors.toList());

        //通过分类id集合去查询数据 将List集合转换成,分隔的string字符串
        String cidsStr = String.join(",", cidList);
        Result<List<CategoryEntity>> categoryResult = categoryFeign.getCateByIds(cidsStr);

        map.put(hotCidArr.get(0),categoryResult.getData());
        return map;
    }

}
