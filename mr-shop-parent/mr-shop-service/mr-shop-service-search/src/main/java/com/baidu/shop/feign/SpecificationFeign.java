package com.baidu.shop.feign;

import com.baidu.shop.service.SpecificationServer;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(contextId = "SpecificationServer", value = "xxx-service")
public interface SpecificationFeign extends SpecificationServer {
}
