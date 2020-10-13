package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @ClassName RunUserApplication
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/10/13
 * @Version V1.0
 **/
@EnableEurekaClient
@SpringBootApplication
@MapperScan(value = "com.baidu.shop.mapper")
public class RunUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunUserApplication.class);
    }
}
