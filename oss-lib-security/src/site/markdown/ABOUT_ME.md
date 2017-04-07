# 关于lib-security

## 特性
#### 应用类型

> 支持如下四种  

    MIXED: RESTful API and template pages, with all authentication method.  
    RESOURCE: RESTful resource application with token and oauth authentication method only.  
    RESTFUL: RESTful application, no template pages, with all authentication method.  
    TEMPLATE: Template based application, no RESTful API, with all authentication method.

#### ErrorHandle错误处理

> 集成oss-lib-errorhandle框架，标准化输出错误信息。

#### 加密的Cookie

> 使用AES算法进行cookie加密，可再配置文件做如下配置开启：

    app.security.cookieKey: 'AES256_CBC16:YDD7uVFNpvkId8HWI6xTfOeRW3O6Wk3FDuGJdnGDhiD='

#### 表单验证

> 表单验证的相关配置如下,页面提交表单数句遵从`application/x-www-form-urlencoded`

    app.security.loginPage : '/login.do'
    app.security.loginProcessingUrl: '/login'
    app.security.logoutUrl: '/logout'
	    
#### 使用RSA算法对密码字段进行加密

> 生成RSA密钥对，客户端通过指定的url获取加密用的公钥，服务端收到请求后，会使用私钥进行解密处理
    
    app.security.loginKey: 'RSA1024_PAIR_PKCS1_X509:MIICWwIBAAKBgQCRXunHa+5hw+L39UZoIFhPwPR9gBvdlEvlXAH4biP+GNb8OH77OjMsfjfs3dXs3QqwXMajJUKZFpX1v+w/ilZ9muvty4/wfQlx5qy305Ui6mRqpIQKKxkwgAS+LTVCh+AeFWfB9G/OVA/khp73VQ3/IPoqgcPG+e/RD/kdFwTozwIDAQABAoGAALbYqzQqjaxqtxErcWOHS40FJoC0olgSL2ROViwkXSE+HSsh5JYankNYfv9wB6PmY4txJmgOdbYYsoZo4L8LQbzV02cEzAibAltvS8JUZ0XWNbnhRnSvY+GJ24hf1BHIJGjBnkQDK/XrDw8Xzt2bSDlWyWH4iEC7PgpghQpHRZECQQDB1GK0U3iDofSJkdLpypxWE6T6MBl7RJCOROj56cXIaVcamOKD1D+x+aDrP7wH2Crhn5X83cTDGekDEIzs0tHjAkEAv/+A4QP61ZgEKn+1gk7RHw/tWHBLWhb1EcIpShBNWx8sB4G8wI7brf858iLXl9NhROkcqYmbchG3D6SnYZmRJQJAR8kxBTgk2huRRaIMSyoO3JJJ95740P6Dyy0aW/SIm8Dn0aHtwoVJUdDyGC5ypTUaLJW+Jvi7dsaR1eC7ULqDoQJAHoSNRrbsOuEz4FF2V2URxl8wubr3rzUw9Qaoq3YV5aL5y6OqjeznLCwCWOOC40YdEuf+v0/5HlOEhn8Ef/X55QJAKktna3oDVsfsTH6REVLz8kjvhILJ5yk493/yjTZxpcpcVv8LGjuCKtIn3NdCRoeIC1K+oLzbBB6U+J8Uu5aeSA==:MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRXunHa+5hw+L39UZoIFhPwPR9gBvdlEvlXAH4biP+GNb8OH77OjMsfjfs3dXs3QqwXMajJUKZFpX1v+w/ilZ9muvty4/wfQlx5qy305Ui6mRqpIQKKxkwgAS+LTVCh+AeFWfB9G/OVA/khp73VQ3/IPoqgcPG+e/RD/kdFwTozwIDAQAB'
    app.security.loginPublicKeyUrl: '/login/rsaPublicKey'                              	    
  
#### 支持验证码

> 应用需要实现`com.yirendai.oss.lib.security.api.VerifyCodeProvider`，来自定义验证码的获取地址和匹配规则

    app.security.verifyCode: true

  
## 集成

#### 用户和扩展信息

> 实现 `com.yirendai.oss.lib.security.api.User` 可以通过 `public Map&lt;String, Object&gt; 
  getProperties()` 方法返回用户的其他扩展信息.  

#### 角色
    
> 角色是对用户身份的一种抽象。  
> 应用通过继承 `com.yirendai.oss.lib.security.api.AbstractRole` 来定义自己的用户角色.  
  `AbstractRole`会默认插入前缀 `ROLE_`到角色的描述信息中 (*com.yirendai.oss.lib.security.api.Security.ROLE_PREFIX*).  

#### 权限

> 权限是对用户针对某种资源的是否可以访问给出的定义。  
> 应用通过实现接口 *com.yirendai.oss.lib.security.api.StaticPrivilege* 来定义自己的权限.  
   `StaticPrivilege`默认会插入前缀`PRIVILEGE_`到权限的描述信息中 (*com.yirendai.oss.lib.security.api.StaticPrivilege
   .PRIVILEGE_PREFIX*).

#### 默认的测试用户

> 应用通过继承实现 `com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider#testUsers().`来添加测试账户    
    
    app.security.defaultTestUser:'test_user'

#### Basic Auth

> 支持对请求的Basic Auth授权认证

    curl -i -X GET -L http://test_user:user_pass@127.0.0.1:8080/users/current

#### Digest auth

> 支持摘要认证

#### Used spring-boot properties
    
    security.ignored= # 
    security.basic.realm=Spring # 

## lib-security yml配置项说明

    app:
      type:TEMPLATE					# 类型，可选的值为 MIXED|TEMPLATE|RESTFUL|RESOURCE 
      security:
        enabled: true                # 开关
        basePath: '/auth'            # auth相关的url前缀，下面相关的url默认都会拼接此前缀，默认是 /auth
        loginProcessingUrl: '/login' # form表单提交的用户登录url，默认是 /auth/login post方式
        logoutUrl: '/logout'         # 用户登出的url，默认是 /auth/logout
        loginPublicKeyUrl: '/login/publicKey' # 获取RSA公钥的url，默认是 /auth/login/publicKey
        defaultTestUser: test_user   # 测试用户,为方便测试，请求如果没有认证，会根据defaultTestUser生成token
        cookieKey: 'keySpec:value'   # 对cookie进行aes加密计算用的key，key描述:key值
        loginKey: 'keySpec:value'   # 对表单字段进行rsa非对称加密的key
        jwtKey: 'keySpec:value'      # 对jwt进行签名计算用的key
        permited: '/permited/**,/img/*' # 无须认证授权的url,多个用,隔开
        loginPage: ’/login‘			# 自定义的进入登录页面的url  /auth/login get方式 TEMPLATE类型需要定义
        authSucessHandler:'/index' # 登陆成功后跳转到的页面，template应用可以在此制定跳转页
        verifyCode: true            # 是否开启验证码功能，默认false，开启需要实现com.yirendai.oss.lib.security.api.VerifyCodeProvider
