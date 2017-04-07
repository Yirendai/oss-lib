# oss-lib-security

## 概述
> lib-security为了方便应用对资源的权限控制而生,基于spring-security,做了很多功能增强和自动化配置，来简化应用对spring-security的集成成本，使开发者很快的将权限控制的功能集成到自己的应用中。 

## 环境依赖

#### 配置JCE

    curl -s -k -L -C - -b "oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jce/8/jce_policy-8.zip > /tmp/policy.zip
    sudo sh -c "unzip -p /tmp/policy.zip UnlimitedJCEPolicyJDK8/local_policy.jar > ${JAVA_HOME}/jre/lib/security/local_policy.jar"
    sudo sh -c "unzip -p /tmp/policy.zip UnlimitedJCEPolicyJDK8/US_export_policy.jar > ${JAVA_HOME}/jre/lib/security/US_export_policy.jar"

#### 配置Maven依赖
- 项目需要先引入oss-release,然后使用如下方式引入lib-security。

    <dependency>
        <groupId>com.yirendai.oss</groupId>
        <artifactId>oss-lib-security-spring-boot-${spring-boot.version}</artifactId>
    </dependency>

- 使用lib-security，需要指定`spring-boot.version`,目前支持的springboot版本如下,
- 后续会随着springboot的演进，持续加入对新版springboot的支持。（这里推荐使用1.4.1）
  
    + 1.3.5.RELEASE
    + 1.3.6.RELEASE
    + 1.3.7.RELEASE
    + 1.4.1.RELEASE
  
> 为方便测试，可以加入`spring-security-test`的依赖。

    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>

## 快速上手
#### application.yml配置

    app.security:
	    enabled: true                # 是否开启lib-security权限认证
	    
>  更多的配置项说明，请参见@关于lib-security一章中的介绍    

- 用户模型
> 继承`com.yirendai.oss.lib.security.api.AbstractUser`类并实现父类抽象方法，按需覆写父类方法。关于额外的用户信息，可以通过扩展`public Map<String, Object> 
getProperties()`来实现 
  
- service实现
    + `com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider`类已经实现了`UserDetailsService`接口并hook了`ContextRefreshedEvent`事件。
    + lib-security不假定用户的存储逻辑，用户只需重写 findByName、save、delete等方法即可。
	
> 可以重写该类的 testUsers 方法，生成测试用户方便测试。
	
    @Override
    protected List<User> testUsers() {
        return ImmutableList.of(
        User.userBuilder().email("user@somedomain.com").enabled(true) //
            .name("test_user").password("user_pass") //
            .roles(ImmutableSet.of(UserRole.ROLE_USER_AUTHORITY)) //
            .build(),
        User.userBuilder().email("admin@somedomain.com").enabled(true) //
            .name("test_admin").password("admin_pass") //
            .roles(ImmutableSet.of(UserRole.ROLE_ADMIN_AUTHORITY)) //
            .build());
    }
  
- 测试登录

    
    curl -i -X POST -L -c COOKIE --data "username=test_user&password=user_pass" http://127.0.0.1:8080:8080/auth/login
      
    HTTP/1.1 200 OK
      
    Date: Mon, 12 Dec 2016 07:01:36 GMT
    Set-Cookie: generic_user=%7B%22accountNonExpired%22%3Atrue%2C%22accountNonLocked%22%3Atrue%2C%22authorities%22%3A%5B%22ROLE_USER%22%2C%22PRIVILEGE_DATA_VIEW%22%5D%2C%22credentialsNonExpired%22%3Atrue%2C%22enabled%22%3Atrue%2C%22password%22%3Anull%2C%22username%22%3A%22UT_USER%3E1%3Etest_user%22%2C%22properties%22%3A%7B%22email%22%3A%22user%40somedomain.com%22%7D%2C%22timestamp%22%3A%222016-12-12T15%3A01%3A36.918%2B08%3A00%22%2C%22uuid%22%3A%22b8dd33cca0a6422aace507aaf6d06206%22%2C%22id%22%3A%221%22%7D;Version=1;Path=/;Expires=Mon, 12-Dec-2016 08:01:37 GMT;Max-Age=3600;HttpOnly;Comment=generic_user
    Expires: Thu, 01 Jan 1970 00:00:00 GMT
    X-Auth-Token: %7B%22accountNonExpired%22%3Atrue%2C%22accountNonLocked%22%3Atrue%2C%22authorities%22%3A%5B%22ROLE_USER%22%2C%22PRIVILEGE_DATA_VIEW%22%5D%2C%22credentialsNonExpired%22%3Atrue%2C%22enabled%22%3Atrue%2C%22password%22%3Anull%2C%22username%22%3A%22UT_USER%3E1%3Etest_user%22%2C%22properties%22%3A%7B%22email%22%3A%22user%40somedomain.com%22%7D%2C%22timestamp%22%3A%222016-12-12T15%3A01%3A36.918%2B08%3A00%22%2C%22uuid%22%3A%22b8dd33cca0a6422aace507aaf6d06206%22%2C%22id%22%3A%221%22%7D
    Content-Type: application/json;charset=UTF-8
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: SAMEORIGIN
    Transfer-Encoding: chunked
      
    {"authorities":["ROLE_USER","PRIVILEGE_DATA_VIEW"],"enabled":true,"id":"1","name":"test_user","password":"[PROTECTED]","username":"test_user"}
  
> lib-security分别在 `Cookie` 和 `X-Auth-Token` header中，返回了token信息,这里由于没有配置加密，所以token仅仅做了URLEncode。在Response 
	Body中返回用户基本信息。
  
- 资源访问
  
	支持客户端在请求资源时，以如下两种方式携带token信息(token信息即用户登录成功后，在cookie和X-Auth-Token中返回的数据)。  
> Cookie方式

    
	curl -i -X GET -L --COOKIE "generic_user=%7B%22accountNonExpired%22%3Atrue%2C%22accountNonLocked%22%3Atrue%2C%22authorities%22%3A%5B%22ROLE_USER%22%2C%22PRIVILEGE_DATA_VIEW%22%5D%2C%22credentialsNonExpired%22%3Atrue%2C%22enabled%22%3Atrue%2C%22password%22%3Anull%2C%22username%22%3A%22UT_USER%3E1%3Etest_user%22%2C%22properties%22%3A%7B%22email%22%3A%22user%40somedomain.com%22%7D%2C%22timestamp%22%3A%222016-12-12T15%3A01%3A36.918%2B08%3A00%22%2C%22uuid%22%3A%22b8dd33cca0a6422aace507aaf6d06206%22%2C%22id%22%3A%221%22%7D;Version=1;Path=/;Expires=Mon, 12-Dec-2016 08:01:37 GMT;Max-Age=3600;HttpOnly;Comment=generic_user" http://127.0.0.1:8080:8080/users/current
	  
	HTTP/1.1 200 OK
	  
    Date: Mon, 12 Dec 2016 07:13:05 GMT
    X-Application-Context: application-quick:8080
    Content-Type: application/json;charset=UTF-8
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    Cache-Control: no-cache, no-store, max-age=0, must-revalidate
    Pragma: no-cache
    Expires: 0
    X-Frame-Options: SAMEORIGIN
    Transfer-Encoding: chunked
      
    {"authorities":["ROLE_USER","PRIVILEGE_DATA_VIEW"],"enabled":true,"id":"1","name":"test_user","password":"[PROTECTED]","username":"test_user"}

> X-Auth-Token方式

    
	curl -i -X GET -L --HEADER "X-Auth-Token: %7B%22accountNonExpired%22%3Atrue%2C%22accountNonLocked%22%3Atrue%2C%22authorities%22%3A%5B%22ROLE_USER%22%2C%22PRIVILEGE_DATA_VIEW%22%5D%2C%22credentialsNonExpired%22%3Atrue%2C%22enabled%22%3Atrue%2C%22password%22%3Anull%2C%22username%22%3A%22UT_USER%3E1%3Etest_user%22%2C%22properties%22%3A%7B%22email%22%3A%22user%40somedomain.com%22%7D%2C%22timestamp%22%3A%222016-12-12T15%3A01%3A36.918%2B08%3A00%22%2C%22uuid%22%3A%22b8dd33cca0a6422aace507aaf6d06206%22%2C%22id%22%3A%221%22%7D" http://127.0.0.1:8080:8080/users/current
      	
	HTTP/1.1 200 OK
	  
    Date: Mon, 12 Dec 2016 07:15:31 GMT
    X-Application-Context: application-quick:8080
    Content-Type: application/json;charset=UTF-8
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    Cache-Control: no-cache, no-store, max-age=0, must-revalidate
    Pragma: no-cache
    Expires: 0
    X-Frame-Options: SAMEORIGIN
    Transfer-Encoding: chunked
      
    {"authorities":["ROLE_USER","PRIVILEGE_DATA_VIEW"],"enabled":true,"id":"1","name":"test_user","password":"[PROTECTED]","username":"test_user"}
  
- 测试登出

    
	curl -i -X GET -L -b COOKIE http://127.0.0.1:8080:8080/auth/logout
	
	HTTP/1.1 200 OK
    Date: Mon, 12 Dec 2016 07:16:41 GMT
    Set-Cookie: generic_user=;Version=1;Path=/;Expires=Thu, 01-Jan-1970 00:00:00 GMT;Max-Age=0;HttpOnly;Comment=generic_user
    Expires: Thu, 01 Jan 1970 00:00:00 GMT
    X-Content-Type-Options: nosniff
    X-XSS-Protection: 1; mode=block
    X-Frame-Options: SAMEORIGIN
    Content-Length: 0
> 登出请求成功后，用户的cookie和token信息都被清空了，

