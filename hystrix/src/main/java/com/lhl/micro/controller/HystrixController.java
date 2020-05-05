package com.lhl.micro.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.ribbon.proxy.annotation.Hystrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author lhl9088
 * @date 2020/5/5 18:05
 */
@RestController
public class HystrixController {

    @Autowired
    private EurekaClient client;

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/")
    @HystrixCommand(fallbackMethod = "fallback")
    public String doCoustomer() {
        InstanceInfo server = client.getNextServerFromEureka("provider-demo", false);
        return restTemplate.getForObject(server.getHomePageUrl() + "provider/", String.class);
    }

    private String fallback(){
        return "all server is down";
    }
}
