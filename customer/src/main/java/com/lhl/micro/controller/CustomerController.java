package com.lhl.micro.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author lhl9088
 * @date 2020/5/2 23:25
 */
@RestController
public class CustomerController {

    private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private static final String URL = "http://localhost:8002/provider";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient client;

    // 使用配置方式
    @Value("${provider.url}")
    private String url;

    @RequestMapping("/")
    public String test() {
        // 将url写死了 灵活性较差
        String result1 = restTemplate.getForObject(URL, String.class);
        logger.info("first way : {}", result1);

        // 使用配置的方式 灵活性有所提高 但是针对多个服务端 配置的方式也不好实现
        String result2 = restTemplate.getForObject(url, String.class);
        logger.info("second way : {}", result2);

        // 通过服务注册方式
        InstanceInfo server = client.getNextServerFromEureka("service", false);
        // contextPath 需要拼接 这个地方还是有点问题 而且需要拼接接口地址
        String serverUrl = server.getHomePageUrl() + "provider";
        logger.info("server url = {}", serverUrl);
        String result3 = restTemplate.getForObject(serverUrl, String.class);
        logger.info("third way : {}", result3);
        return result3;
    }
}
