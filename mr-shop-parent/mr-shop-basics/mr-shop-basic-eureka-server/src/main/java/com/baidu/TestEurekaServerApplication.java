package com.baidu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @ClassName TestEurekaServerApplication
 * @Description: TODO
 * @Author xiaopengyan
 * @Date 2020/8/27
 * @Version V1.0
 **/
@SpringBootApplication
@EnableEurekaServer
public class TestEurekaServerApplication {

    public static void main(String[] args) {
        System.out.println("");
        SpringApplication.run(TestEurekaServerApplication.class);
    }

}
