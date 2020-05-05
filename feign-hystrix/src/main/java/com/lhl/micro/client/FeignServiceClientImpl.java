package com.lhl.micro.client;

import org.springframework.stereotype.Component;

/**
 * @author lhl9088
 * @date 2020/5/5 22:16
 */
@Component
public class FeignServiceClientImpl implements FeignServiceClient {

    @Override
    public String doService() {
        return "hello world from feign-hystrix";
    }
}
