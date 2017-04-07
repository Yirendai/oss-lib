# oss-lib-adminclient说明文档
oss-admin是spring boot应用的一个监控和管理平台，作为oss-admin的被监控对象，需要这些spring boot应用提供一些额外的信息到admin，比如通过加密的`security.user
.name`和`security.user.password`. 该lib主要提供了数据加密、装配等功能，让客户端轻松实现秘钥传输的功能。
## 功能简介
目前该lib已经添加了部分功能，新的功能正在开发当中。
 + 集成spring admin client的其他依赖: 比如eurek
 + 定制/info接口,添加security.user.name和security.user.password的密文到/info接口
 + admin客户端的配置详细文档

## 使用说明
  
### 引入依赖
####
    <dependency>
        <groupId>com.yirendai.oss.lib</groupId>
        <artifactId>oss-lib-adminclient-spring-boot-${spring-boot.version}</artifactId>
    </dependency>
