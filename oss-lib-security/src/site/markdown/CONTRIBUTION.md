
# 如何生成RSA密钥
```
# 设置key长度
key_size=1024;

# 生成PKCS#1私钥
openssl genrsa -out rsa_${key_size}_priv_pkcs1.pem ${key_size}
# 根据私钥生成公钥, 默认为x509格式
openssl rsa -pubout -in rsa_${key_size}_priv_pkcs1.pem -out rsa_${key_size}_pub.pem

# 生成 用于配置文件的, 去掉注释的, 单行的私钥
cat rsa_${key_size}_priv_pkcs1.pem | grep -v '\-' | sed -e :a -e '$!N; s/\n//; ta'
# or
cat rsa_${key_size}_priv_pkcs1.pem | grep -v '\-' | awk '!/0$/{printf $0}/0$/'

# 生成 用于配置文件的, 去掉注释的, 单行的公钥
cat rsa_${key_size}_pub.pem | grep -v '\-' | sed -e :a -e '$!N; s/\n//; ta'
# or
cat rsa_${key_size}_pub.pem | grep -v '\-' | awk '!/0$/{printf $0}/0$/'

# 生成 用于配置文件的, 去掉注释的, 单行的密钥对
echo "$(cat rsa_${key_size}_priv_pkcs1.pem | grep -v '\-' | sed -e :a -e '$!N; s/\n//; ta'):$(cat rsa_${key_size}_pub.pem | grep -v '\-' | sed -e :a -e '$!N; s/\n//; ta')"

# 将PKCS#1私钥转成PKCS#8私钥
openssl pkcs8 -topk8 -in rsa_${key_size}_priv_pkcs1.pem -out rsa_${key_size}_priv_pkcs8.pem -nocrypt
```

# Javascript 加密库
[支持PKCS#1的RSA库](https://github.com/travist/jsencrypt)

密钥文件最终将数据通过Base64编码进行存储
RFC2045中规定:
The encoded output stream must be represented in lines of no more than 76 characters each
Base64编码的数据每行最多不超过76字符, 超长数据要按行分割

https://github.com/vibornoff/asmcrypto.js

## 参考文档

### security 登陆页
http://docs.spring.io/spring-security/site/docs/4.1.3.RELEASE/guides/html5/form-javaconfig.html
https://spring.io/guides/gs/securing-web/

### Security 持久层

http://www.mkyong.com/spring-security/spring-security-hibernate-annotation-example/

### Security EL注解

http://docs.spring.io/spring-security/site/docs/4.1.0.RELEASE/reference/htmlsingle/#el-pre-post-annotations
http://websystique.com/spring-security/spring-security-4-method-security-using-preauthorize-postauthorize-secured-el/

### Security 测试

https://www.petrikainulainen.net/programming/spring-framework/integration-testing-of-spring-mvc-applications-security/

### Security参考实现

https://docs.stormpath.com/java/spring-boot-web/about.html
org.springframework.security.web.access.ExceptionTranslationFilter

### DEBUG

重点查看

configurer的顺序:
com.yirendai.oss.lib.security.starter.WebApplicationSecurityAutoConfiguration#securityConfigurers

filterChains的内容和filters, 尤其注意只应有一个AnyRequestMatcher.
org.springframework.security.web.FilterChainProxy#getFilters
org.springframework.security.web.FilterChainProxy#doFilterInternal




短信验证码 字段 verifycode

curl -i -c COOKIE --referer "http://127.0.0.1:8080" http://127.0.0.1:8080/login/sendCode


# Security
see: https://spring.io/guides/tutorials/spring-security-and-angular-js/
see: https://spring.io/blog/2015/01/12/the-login-page-angular-js-and-spring-security-part-ii
see: http://stackoverflow.com/questions/32498868/custom-login-form-configure-spring-security-to-get-a-json-response

#测试http basic auth
curl -i -X GET -u admin:admin        http://127.0.0.1:8080/api/hello
curl -i -X GET -u admin:badpassword  http://127.0.0.1:8080/api/hello
curl -i -X GET -u admin:badpassword 'http://127.0.0.1:8080/api/hello?type=xml'

# 本地开发环境 登陆再登出
curl -X POST -L -c COOKIE --data "username=lendAuditor&password=lendAuditor&verifycode=code" http://127.0.0.1:8080/login
curl -X GET -L -b COOKIE -c COOKIE http://127.0.0.1:8080/logout

    # when no csrf
    curl -i -X POST -c COOKIE --data "username=test_admin&password=admin_pass" http://127.0.0.1:8080/login
    curl -X GET -b COOKIE http://127.0.0.1:8080/secured/forAdminOnly
    

    #-H "Accept: */*" -H "Content-Type: application/json"
    curl -i -X GET -c COOKIE http://127.0.0.1:8080/login
    JSESSIONID=$(cat COOKIE | grep JSESSIONID | awk '{print $7}')
    XSRF_TOKEN=$(cat COOKIE | grep XSRF-TOKEN | awk '{print $7}')
    curl -i -X POST -L -b COOKIE -H "X-CSFR-TOKEN: ${XSRF_TOKEN}" --data "username=admin&password=admin" http://127.0.0.1:8080/login
    #curl -i -X POST -L --cookie "JSESSIONID=${JSESSIONID};" -H "X-CSFR-TOKEN: ${XSRF_TOKEN}" --data "username=admin&password=admin" http://127.0.0.1:8080/login
    curl -i -X POST -L --cookie "JSESSIONID=${JSESSIONID};" -H "X-XSRF-TOKEN: ${XSRF_TOKEN}" --data "username=admin&password=admin" http://127.0.0.1:8080/login

curl -i http://127.0.0.1:8080/api/privilegeTest/requireRoleAdmin
curl -i http://127.0.0.1:8080/api/privilegeTest/requireRoleLendAuditor
curl -i http://127.0.0.1:8080/api/privilegeTest/requirePrivilegeCouponSend
curl -i http://127.0.0.1:8080/api/privilegeTest/requirePrivilegeCouponView


## JWT

https://jwt.io/
https://github.com/jwtk/jjwt

http://connect2id.com/products/nimbus-jose-jwt/generator
https://github.com/mitreid-connect/json-web-key-generator
http://technicalrex.com/2015/02/20/stateless-authentication-with-spring-security-and-jwt
