package com.baidu;

import com.baidu.entity.GoodsEntity;
import com.baidu.repository.GoodsEsRepository;
import com.baidu.utils.ESHighLightUtil;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName testEs
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/14
 * @Version V1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RunEsApplication.class})
public class TestEs {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private GoodsEsRepository goodsEsRepository;


    @Test
    public void createGoodsIndex(){
        IndexOperations indexname = elasticsearchRestTemplate.indexOps(IndexCoordinates.of("goods"));
        indexname.create();//创建索引
        System.out.println(indexname.exists()?"索引创建成功":"索引创建失败");
    }

    @Test
    public void createGoodsMapper(){
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsEntity.class);
        indexOperations.createMapping();
        System.out.println("映射创建成功");
    }

    @Test
    public void deleteGoodsIndex(){
        IndexOperations indexOperations = elasticsearchRestTemplate.indexOps(GoodsEntity.class);
        indexOperations.delete();
        System.out.println("索引删除成功");
    }

    //新增文档
    @Test
    public void save(){
        GoodsEntity entity = new GoodsEntity();
        entity.setId(2L);
        entity.setBrand("小米");
        entity.setCategory("手机");
        entity.setImages("xiaomi.jpg");
        entity.setPrice(1000D);
        entity.setTitle("小米1");

        goodsEsRepository.save(entity);
        System.out.println("新增成功");
    }

    //更新文档
    @Test
    public void update(){

        GoodsEntity entity = new GoodsEntity();
        entity.setId(1L);
        entity.setBrand("华为");
        entity.setCategory("手机");
        entity.setImages("华为.jpg");
        entity.setPrice(2000D);
        entity.setTitle("华为手机");

        goodsEsRepository.save(entity);
        System.out.println("修改成功");
    }

    //删除
    @Test
    public void delete(){
        GoodsEntity entity = new GoodsEntity();
        entity.setId(2L);
        goodsEsRepository.delete(entity);
        System.out.println("删除成功");
    }

    //批量新增
    @Test
    public void saveAll(){
        GoodsEntity entity = new GoodsEntity();
        entity.setId(2L);
        entity.setBrand("苹果");
        entity.setCategory("手机");
        entity.setImages("pingguo.jpg");
        entity.setPrice(5000D);
        entity.setTitle("iphone11手机");

        GoodsEntity entity2 = new GoodsEntity();
        entity2.setId(3L);
        entity2.setBrand("三星");
        entity2.setCategory("手机");
        entity2.setImages("sanxing.jpg");
        entity2.setPrice(3000D);
        entity2.setTitle("w2019手机");

        GoodsEntity entity3 = new GoodsEntity();
        entity3.setId(4L);
        entity3.setBrand("华为");
        entity3.setCategory("手机");
        entity3.setImages("huawei.jpg");
        entity3.setPrice(4000D);
        entity3.setTitle("华为mate30手机");

//        List<GoodsEntity> list = new ArrayList<>();
//        list.add(entity);
//        list.add(entity2);
//        list.add(entity3);
//        goodsEsRepository.saveAll(list);

        goodsEsRepository.saveAll(Arrays.asList(entity,entity2,entity3));

        System.out.println("批量新增成功");
    }

    //查询所有数据
    @Test
    public void searchAll(){
        //查询总条数
        long count = goodsEsRepository.count();
        System.out.println(count);
        //查询所有数据
        Iterable<GoodsEntity> all = goodsEsRepository.findAll();
        all.forEach(goods -> {
            System.out.println(goods);
        });
    }

    //条件查询
    @Test
    public void searchByParam(){
        List<GoodsEntity> list = goodsEsRepository.findByTitle("手机");
        System.out.println(list);

        //区间查询
        List<GoodsEntity> byPriceBetween = goodsEsRepository.findByAndPriceBetween(1000D,3000D);
        System.out.println(byPriceBetween);
    }

    //自定义查询
    @Test
    public void customizeSearch(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("title","华为"))
                        .must(QueryBuilders.rangeQuery("price").gte(1000).lte(10000))
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);
        search.getSearchHits().stream().forEach(hit -> {
            System.out.println(hit.getContent());
        });
    }

    //高亮
    @Test
    public void customizeSearchHighLight(){
        //构建高亮查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //构建高亮
//        HighlightBuilder highlightBuilder = new HighlightBuilder();
//        HighlightBuilder.Field title = new HighlightBuilder.Field("title");
//        title.preTags("<span stype=color:#c81623>");
//        title.postTags("</span>");
//        highlightBuilder.field(title);
//
//       queryBuilder.withHighlightBuilder(highlightBuilder);//设置高亮
        queryBuilder.withHighlightBuilder(ESHighLightUtil.getHighlightBuilder("title"));//设置高亮

        //boolQuery把各种其它查询通过`must`（与）、`must_not`（非）、`should`（或）的方式进行组合
        //matchQuery类型查询，会把查询条件进行分词，然后进行查询,多个词条之间是or的关系
        queryBuilder.withQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchQuery("title","华为"))
                .must(QueryBuilders.rangeQuery("price").gte(1000).lte(8000))
        );

        //排序
        queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
        //分页 当前页-1
        queryBuilder.withPageable(PageRequest.of(1-1,2));

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        List<SearchHit<GoodsEntity>> searchHits = search.getSearchHits();
        //重新设置title
//        List<SearchHit<GoodsEntity>> result = searchHits.stream().map(hit -> {
//
//            Map<String, List<String>> highlightFields = hit.getHighlightFields();
//            hit.getContent().setTitle(highlightFields.get("title").get(0));
//            return hit;
//
//        }).collect(Collectors.toList());
        List<SearchHit<GoodsEntity>> highLightHit = ESHighLightUtil.getHighLightHit(searchHits);
        System.out.println(highLightHit);
    }

    //聚合
    @Test
    public void searchAgg(){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brand_agg").field("brand.keyword")
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        Aggregations aggregations = search.getAggregations();
        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());
        });
        System.out.println(search);
    }

    //嵌套聚合，聚合函数值
    @Test
    public void searchAggMethod(){

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.addAggregation(
                AggregationBuilders.terms("brand_agg")
                        .field("brand.keyword")
                        //聚合函数
                        .subAggregation(AggregationBuilders.max("max_price").field("price"))
        );

        SearchHits<GoodsEntity> search = elasticsearchRestTemplate.search(queryBuilder.build(), GoodsEntity.class);

        Aggregations aggregations = search.getAggregations();

        Terms terms = aggregations.get("brand_agg");

        List<? extends Terms.Bucket> buckets = terms.getBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.getKeyAsString() + ":" + bucket.getDocCount());

            //获取聚合
            Aggregations aggregations1 = bucket.getAggregations();
            //得到map
            Map<String, Aggregation> map = aggregations1.asMap();
            //需要强转,Aggregations是一个接口 Terms是他的子接口,Aggregation是一个接口Max是他的子接口,而且Max是好几个接口的子接口
            Max max_price = (Max) map.get("max_price");
            System.out.println(max_price.getValue());
        });
    }

}
