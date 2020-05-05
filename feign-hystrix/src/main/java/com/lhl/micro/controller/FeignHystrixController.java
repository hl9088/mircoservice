package com.lhl.micro.controller;

import com.lhl.micro.client.FeignServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lhl9088
 * @date 2020/5/5 22:19
 */
@RestController
public class FeignHystrixController {

    @Autowired
    private FeignServiceClient client;

    @RequestMapping("/")
    public String doCustomer() {
        return client.doService();
    }
}
