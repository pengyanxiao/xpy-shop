package com.baidu.repository;

import com.baidu.entity.GoodsEntity;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


/**
 * @ClassName GoodsEsRepository
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/14
 * @Version V1.0
 **/
public interface GoodsEsRepository extends ElasticsearchRepository<GoodsEntity , Long> {

    List<GoodsEntity> findByTitle(String title);

    List<GoodsEntity> findByTitleAndBrand(String title,String brand);

    List<GoodsEntity> findByAndPriceBetween(Double start,Double end);

}
