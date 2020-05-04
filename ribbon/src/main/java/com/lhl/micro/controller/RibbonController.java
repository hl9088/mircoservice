package com.lhl.micro.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author lhl9088
 * @date 2020/5/4 11:00
 */
@RestController
public class RibbonController {

    private static Logger logger = LoggerFactory.getLogger(RibbonController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient client;

    @GetMapping("/")
    public String doCustomer() {
        return restTemplate.getForObject("http://PROVIDER-DEMO/provider/", String.class);
    }

    /**
     * 测试获取调用的哪个服务方
     * @return
     */
    @GetMapping("/test")
    public String test(){
        // 测试是哪个服务方提供服务
        ServiceInstance choose = client.choose("PROVIDER-DEMO");
        logger.error("serviceId = {}, host = {}, port = {}", choose.getServiceId(), choose.getHost(), choose.getPort());
        ServiceInstance choose2 = client.choose("PROVIDER-DEMO");
        logger.error("serviceId = {}, host = {}, port = {}", choose2.getServiceId(), choose2.getHost(), choose2.getPort());
        return "success";
    }
}
