package com.lhl.micro.controller;

import com.lhl.micro.feign.FeignServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lhl9088
 * @date 2020/5/4 23:26
 */
@RestController
public class FeignController {

    @Autowired
    private FeignServiceClient client;

    @GetMapping("/")
    public String doService(){
        return client.doService();
    }
}
