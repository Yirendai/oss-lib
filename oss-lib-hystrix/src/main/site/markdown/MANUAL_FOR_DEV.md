# 内部实现方式

## 设计思路
前期设计只考虑到以下两种使用场景
+ hystrix原生态方式使用
+ 使用注解方式

所以设计上参考了[官方文档](https://github.com/Netflix/Hystrix/wiki/Plugins#propertiesstrategy)


    Properties Strategy
    
    If you implement a custom HystrixPropertiesStrategy, this gives you full control over how properties are defined for the system.
    
    The default implementation uses Archaius.

通过查看源码,实现两个接口
HystrixDynamicProperties
HystrixDynamicProperty<T>
通过spring的environment获取真正的配置值

## 后续改进
后续看feignclient实现时
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-netflix-core</artifactId>
  <version>1.2.2.RELEASE</version>
</dependency>

发现实现了该功能,如果大家使用feignclient时,可使用原生的方式
/org/springframework/cloud/spring-cloud-netflix-core/1.2.2.RELEASE/spring-cloud-netflix-core-1.2.2.RELEASE
.jar!/META-INF/spring.factories 文件中

    org.springframework.boot.autoconfigure.EnableAutoConfiguration=org.springframework.cloud.netflix.hystrix.HystrixAutoConfiguration
    
    org.springframework.cloud.netflix.hystrix.HystrixAutoConfiguration
    
    @Configuration
    @ConditionalOnClass({ Hystrix.class, HealthIndicator.class })
    @AutoConfigureAfter({ HealthIndicatorAutoConfiguration.class })
    public class HystrixAutoConfiguration {
    
        @Bean
        @ConditionalOnEnabledHealthIndicator("hystrix")
        public HystrixHealthIndicator hystrixHealthIndicator() {
            return new HystrixHealthIndicator();
        }
    
    }
    
通过spring boot自动配置的方式实现了一系列扩展