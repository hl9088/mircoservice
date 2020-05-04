package com.lhl.micro.config;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ribbon负载均衡算法配置 启动类排除下 不让它自动注入
 *
 * @author lhl9088
 * @date 2020/5/4 14:56
 */
@Configuration
@IgnoreScan
public class RibbonConfig {

    private static Logger logger = LoggerFactory.getLogger(RibbonConfig.class);

    @Bean
    public BeanPostProcessor beanPostProcessor(){
        logger.error("初始化beanPostProcessor");
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                logger.error("after 加载了{}", beanName);
                return bean;
            }

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                logger.error("before 加载了{}", beanName);
                return bean;
            }
        };
    }

    @Bean
    public IRule ribbonRule() {
        // 使用随机算法
        return new RandomRule();
    }
}
