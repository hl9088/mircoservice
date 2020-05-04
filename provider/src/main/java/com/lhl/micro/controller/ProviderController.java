package com.lhl.micro.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lhl9088
 * @date 2020/5/2 23:33
 */
@RestController
public class ProviderController {

    @Autowired
    private EurekaClient client;

    @RequestMapping("/")
    public String doService() {
        return "Hello world from provider";
    }

    /**
     * 测试获取自己的服务信息
     * @return
     */
    @GetMapping("/test")
    public String test() {
        InstanceInfo server = client.getNextServerFromEureka("provider-demo", false);
        return server.getHomePageUrl();
    }
}
