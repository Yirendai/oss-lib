
# oss-lib-hystrix

## 概述
使用该库实现hystrix通过yml格式以及configserver进行配置

## 使用方法
+ 已入依赖
        <dependency>
            <groupId>com.yirendai.oss.lib</groupId>
            <artifactId>oss-lib-hystrix</artifactId>
            // 替换成现有版本 <version>1.0.7.OSS</version>
        </dependency>

+ 创建配置文件
在application.yml中添加配置项,参考
   
    [配置项说明](src/main/site/markdown/CONFIG.md)

+ 修改配置项
```
hystrix:
  command:
    default:  # 表示默认的配置
      execution:
        timeout.enabled: true
```


```
hystrix:
  command:
    helloservice:  # 表示具体组下的配置
      execution:
        timeout.enabled: true
```

+ 通过configserver进行配置


## 给开发人员
[文档](src/main/site/markdown/MANUAL_FOR_DEV.md)介绍了设计思路,以及feignClient实现的方式
