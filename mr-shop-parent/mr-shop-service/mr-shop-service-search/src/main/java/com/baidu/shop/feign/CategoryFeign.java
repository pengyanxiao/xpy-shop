package com.baidu.shop.feign;

import com.baidu.shop.service.CategoryService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @ClassName CategoryFeign
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/9/21
 * @Version V1.0
 **/
@FeignClient(contextId = "CategoryService",value = "xxx-service")
public interface CategoryFeign extends CategoryService {
}
