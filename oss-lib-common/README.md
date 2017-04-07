
# oss-lib-common

oss-lib成员共同使用的库, 同时包含一些spring-boot配置的扩展, 密码库等.

## spring-boot配置扩展

包括一些自定义配置属性和annotation.

我们自定义了一些配置属性, 你可以在 `AppProperties` 类中找到它们.
我们自定义属性的原则是不与spring-boot提供的重复, 如果spring-boot已经提供了, 那么我们就直接使用它而不是再定义一个.

我们还提供了一些annotation来控制Bean初始化

+ `@ConditionalOnAppType` 识别 `app.type`
+ `@ConditionalOnAppSecurity` 识别 `app.security.enabled`
+ `@ConditionalOnNotEnvProduction` 识别 `spring.profile.active`

## 密码库

简化复杂的密码库的使用, 并且可以在spring-boot自动配置时初始化加密/解密器.

支持的方法有:
+ AES
+ RSA
+ JWT

## 模板interpolate

基于EL表达式实现, 可用在很多地方.

## 其它小工具

过于琐碎, 在此不一一介绍, 可通过查看javadoc或源码了解详情.
