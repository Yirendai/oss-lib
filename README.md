
-----
如果你正在通过git服务查看此文档，请移步项目网站或gitbook查看文档，因为git服务生成的文档链接有问题。
+ [gitbook](http://mvn-site.internal/oss-develop/gitbook)
+ [RELEASE版网站](http://mvn-site.internal/oss/staging)
+ [SNAPSHOT版网站](http://mvn-site.internal/oss-develop/staging)
-----

# oss-lib微服务程序库

建立本程序库的目的是为了对开源软件进行定制或扩展, 以使其更符合我们的需求.

使用开源软件时应尽量避免以复制的方式修改它的源码.
最好将新特性或bug修复push给原作者, 但有时候因为开源软件的设计与我们自己的目标不尽相同, 或者受限于作者的精力与水平, 
不会合并我们的pull/merge request. 这时我们需要用自己的方法对其进行扩展或修改.

oss-lib的成员有:
+ [oss-lib-adminclient](./oss-lib-adminclient/) 改进的spring-boot-admin客户端.
+ [oss-lib-common](./oss-lib-common/) oss-lib成员共同使用的库, 同时包含一些spring-boot配置的扩展, 密码库等.
+ [oss-lib-errorhandle](./oss-lib-errorhandle/) 改进spring错误处理机制的库, 提供针对Feign RPC的支持, 支持swagger.
+ [oss-lib-log4j2](./oss-lib-log4j2/) 扩展log4j2的JMX能力以使其兼容spring-boot-admin.
+ [oss-lib-security](./oss-lib-security/) 更简单的, 无状态的基于spring-security的安全库, 支持swagger.
+ [oss-lib-swagger](./oss-lib-swagger/) 根据环境开启或关闭swagger API文档, 提供默认的API分组.
+ [oss-lib-test](./oss-lib-test/) 支持测试的库, 改进了TestRestTemplate的兼容性, 以及其它小功能.
+ [oss-lib-webmvc](./oss-lib-webmvc/) 对spring-webmvc进行一些配置, 提供支持web应用的小工具.
