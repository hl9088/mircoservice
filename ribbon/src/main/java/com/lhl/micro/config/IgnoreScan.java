package com.lhl.micro.config;

import java.lang.annotation.*;

/**
 * 标记springboot启动过程中排除哪些类被自动加载
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreScan {
}
