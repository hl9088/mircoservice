# 微服务入门

## 使用springboot实现生产者消费者模型

* 创建父工程microservice,引入springboot相关依赖
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.6.RELEASE</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
```
* 分别创建provider服务提供者、customer服务消费者子模块, 引入依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```
```yaml
server:
  port: 8001 #端口
  servlet:
    context-path: /provider #应用名称

spring:
  application:
    name: service
```
```yaml
server:
  port: 8002 #端口
  servlet:
    context-path: /customer
provider:
  url: http://localhost:8001/provider
spring:
  application:
    name: customer #应用名称
```
* provider模块创建对外服务接口
```java
@RestController
public class ProviderController {
   @RequestMapping("/")
   public String doService() {
       return "Hello world from provider";
   }
}
```
* customer模块调用服务提供方接口 创建bean```RestTemplate```
```java
@SpringBootApplication
public class CustomerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerApplication.class, args);
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```
```java
@RestController
public class CustomerController {

    private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

    private static final String URL = "http://localhost:8001/provider";

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/")
    public String test() {
        // 将url写死了 灵活性较差
        String result1 = restTemplate.getForObject(URL, String.class);
        logger.info("first way : {}", result1);

        // 使用配置的方式 灵活性有所提高 但是针对多个服务端 配置的方式也不好实现
        String result2 = restTemplate.getForObject(url, String.class);
        logger.info("second way : {}", result2);

        return result2;
    }
}
```

## eureka服务注册与发现
* 改造microservice父工程, 引入springcloud相关依赖
```xml
<dependencyManagement>
    <dependencies>
        <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Greenwich.SR3</version>
            <type>pom</type>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
```xml
<!-- 如果原来没有引入这个依赖的话 启动会报错 引入下 -->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```
* 创建eureka服务端子模块 引入服务端依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    <version>2.2.2.RELEASE</version>
</dependency>
```
```yaml
server:
  port: 8888
eureka:
  client:
    # 配置自己不注册自己
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:${server.port}/eureka
```
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
}
```
* 改造provider服务提供方 引入eureka客户端依赖 配置eureka服务端注册地址
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    <version>2.2.2.RELEASE</version>
</dependency>
```
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8888/eureka # eureka服务端注册地址
```
```java
@SpringBootApplication
@EnableEurekaClient
public class ProviderApplication {
    // 省略
}

@RestController
public class ProviderController {

    @Autowired
    private EurekaClient client;

    @GetMapping("/info")
    public String info() {
        // 直接测试能不能获取自己的地址
        InstanceInfo server = client.getNextServerFromEureka("provider", false);
        return server.getHomePageUrl();
    }
}
```
* 改造customer消费方 引入eureka客户端依赖 配置eureka服务端注册地址
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    <version>2.2.2.RELEASE</version>
</dependency>
```
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8888/eureka
```
```java
@SpringBootApplication
@EnableEurekaClient
public class CustomerApplication {
    // 省略
}
@RestController
public class CustomerController {

    private static Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient client;

    @RequestMapping("/")
    public String test() {
        // 省略一部分
        
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
```
* 注意
```client.getNextServerFromEureka("service", false)```入参为应用名称，不是请求路径中contextPath名称

## ribbon负载均衡的配置 消费端
* 新建ribbon子模块 
1. 引入依赖 ribbon也是作为一个eureka客户端使用的 所以要引入eureka客户端依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        <version>2.2.2.RELEASE</version>
    </dependency>
    
    <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-netflix-ribbon -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
        <version>2.2.2.RELEASE</version>
    </dependency>
</dependencies>
```
```yaml
server:
  port: 8003
  servlet:
    context-path: /ribbon
spring:
  application:
    name: ribbon-demo
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8888/eureka
  instance:
    prefer-ip-address: true
```
2. 启动类上增加```@RibbonClient``` 指定服务方名称 如果有多个服务端 则默认采用轮训方式进行调用
```java
@SpringBootApplication
@EnableEurekaClient
@RibbonClient(value = "PROVIDER-DEMO")
public class RibbonApplication {

    public static void main(String[] args) {
        SpringApplication.run(RibbonApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

@RestController
public class RibbonController {

    private static Logger logger = LoggerFactory.getLogger(RibbonController.class);

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/")
    public String doCustomer() {
        return restTemplate.getForObject("http://PROVIDER-DEMO/provider/", String.class);
    }
}
```
3. 自定义负载均衡策略: 启动类上为```@RibbonClient``` 配置configuration 同时在springboot启动过程中排除掉对应配置的自动注入
```java
@SpringBootApplication
@EnableEurekaClient
@RibbonClient(value = "PROVIDER-DEMO", configuration = RibbonConfig.class)
//@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RibbonConfig.class)})
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ANNOTATION, value = IgnoreScan.class)})
public class RibbonApplication {
}

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
```

## feign的配置使用 消费端
* 创建feign子模块 引入依赖
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        <version>2.2.2.RELEASE</version>
    </dependency>

    <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-openfeign -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
        <version>2.2.2.RELEASE</version>
    </dependency>
</dependencies>
```
```java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
public class FeignApplication {
    public static void main(String[] args) {
        SpringApplication.run(FeignApplication.class, args);
    }
}

@FeignClient(name = "PROVIDER-DEMO", path = "provider")
public interface FeignServiceClient {

    @RequestMapping("/")
    String doService();
}

@RestController
public class FeignController {

    @Autowired
    private FeignServiceClient client;

    @GetMapping("/")
    public String doService(){
        return client.doService();
    }
}
```
```yaml
server:
  port: 8004
  servlet:
    context-path: /feign
spring:
  application:
    name: feign-demo
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8888/eureka
```
* feignClient的配置待完善

## hystrix的配置与使用 断路器  消费端 

* 新建hystrix子模块 引入依赖 
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        <version>2.2.2.RELEASE</version>
    </dependency>

    <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-netflix-hystrix -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        <version>2.2.2.RELEASE</version>
    </dependency>
</dependencies>
```
* 使用```@EnableCircuitBreaker```启用hystrix   配置服务请求失败后执行方法```@HystrixCommand(fallbackMethod = "fallback")```
```java
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class HystrixApplication {
    public static void main(String[] args) {
        SpringApplication.run(HystrixApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}

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
```
* 配置文件和普通消费者一致 注册到eureka即可
```yaml
server:
  port: 8005
  servlet:
    context-path: /hystrix
spring:
  application:
    name: hystrix-demo
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8888/eureka
```
