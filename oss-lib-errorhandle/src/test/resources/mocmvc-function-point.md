# error-handle mocmvc 测试功能点

## 三种类型

### 基础功能验证

+ 返回json格式验证
+ localizedMessage 异常转译验证
+ message   异常信息验证
+ 303&&349
    - local support
    - field 对应
    - 
### template 

+ 出错应该重定向到error page ，匹配相应data
+ 自定义错误页能否拿到数据

### restful
+ 出错应返回json/xml格式
+ 具体格式根据accept内容协商 

### mixed
+ 出错返回的类型应该根据accept的内容协商

## 异常转译

### 继承优先
### 文件优先

## 异常来源 

### 框架
### 应用
### 容器

### rpc 异常传递

### zuul 代理异常能否捕获
 
 


