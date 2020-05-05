package com.lhl.micro.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 当本接口中方法执行出现错误时 会执行fallback配置的类的同名方法
 */
@FeignClient(name = "PROVIDER-DEMO", path = "provider", fallback = FeignServiceClientImpl.class)
public interface FeignServiceClient {

    @RequestMapping("/")
    String doService();
}
