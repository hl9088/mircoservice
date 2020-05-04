package com.lhl.micro.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * path 用于指定服务提供方的contextPath
 */
@FeignClient(name = "PROVIDER-DEMO", path = "provider")
public interface FeignServiceClient {

    @RequestMapping("/")
    String doService();
}
