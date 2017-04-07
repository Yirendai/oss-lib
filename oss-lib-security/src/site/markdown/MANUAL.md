# oss-lib-security  

## Characters
#### App type

  Support 4 app types.  

> MIXED: RESTful API and template pages, with all authentication method.  
> RESOURCE: RESTful resource application with token and oauth authentication method only.  
> RESTFUL: RESTful application, no template pages, with all authentication method.  
> TEMPLATE: Template based application, no RESTful API, with all authentication method.
  

#### Error handle

  Integrated with oss-lib-errorhandle.  
  Standard error info.  

#### Secure cookie

  AES based encrypted cookie. For this, yml definition ex: 
  > app.security.cookieKey: 'AES256_CBC16:YDD7uVFNpvkId8HWI6xTfOeRW3O6Wk3FDuGJdnGDhiD='

#### Form auth

  An auth mechanism that carrying auth info by application/x-www-form-urlencoded form fields.  

  > app.security.loginPage : '/login.do'
  > app.security.loginProcessingUrl: '/login'
  > app.security.logoutUrl: '/logout'
	    
  Test with command line tool. TODO  

#### RSA password encryption

  Generate RSA keypair for password encryption. loginkey for Rsa pair key and loginPublicKeyUrl for client get rsaPublicKey.   

  > app.security.loginKey: 'RSA1024_PAIR_PKCS1_X509:MIICWwIBAAKBgQCRXunHa+5hw+L39UZoIFhPwPR9gBvdlEvlXAH4biP+GNb8OH77OjMsfjfs3dXs3QqwXMajJUKZFpX1v+w/ilZ9muvty4/wfQlx5qy305Ui6mRqpIQKKxkwgAS+LTVCh+AeFWfB9G/OVA/khp73VQ3/IPoqgcPG+e/RD/kdFwTozwIDAQABAoGAALbYqzQqjaxqtxErcWOHS40FJoC0olgSL2ROViwkXSE+HSsh5JYankNYfv9wB6PmY4txJmgOdbYYsoZo4L8LQbzV02cEzAibAltvS8JUZ0XWNbnhRnSvY+GJ24hf1BHIJGjBnkQDK/XrDw8Xzt2bSDlWyWH4iEC7PgpghQpHRZECQQDB1GK0U3iDofSJkdLpypxWE6T6MBl7RJCOROj56cXIaVcamOKD1D+x+aDrP7wH2Crhn5X83cTDGekDEIzs0tHjAkEAv/+A4QP61ZgEKn+1gk7RHw/tWHBLWhb1EcIpShBNWx8sB4G8wI7brf858iLXl9NhROkcqYmbchG3D6SnYZmRJQJAR8kxBTgk2huRRaIMSyoO3JJJ95740P6Dyy0aW/SIm8Dn0aHtwoVJUdDyGC5ypTUaLJW+Jvi7dsaR1eC7ULqDoQJAHoSNRrbsOuEz4FF2V2URxl8wubr3rzUw9Qaoq3YV5aL5y6OqjeznLCwCWOOC40YdEuf+v0/5HlOEhn8Ef/X55QJAKktna3oDVsfsTH6REVLz8kjvhILJ5yk493/yjTZxpcpcVv8LGjuCKtIn3NdCRoeIC1K+oLzbBB6U+J8Uu5aeSA==:MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRXunHa+5hw+L39UZoIFhPwPR9gBvdlEvlXAH4biP+GNb8OH77OjMsfjfs3dXs3QqwXMajJUKZFpX1v+w/ilZ9muvty4/wfQlx5qy305Ui6mRqpIQKKxkwgAS+LTVCh+AeFWfB9G/OVA/khp73VQ3/IPoqgcPG+e/RD/kdFwTozwIDAQAB'
  > app.security.loginPublicKeyUrl: '/login/rsaPublicKey'                              	    
  Test with command line tool. TODO  
  
#### Verify code support

  Provide a *com.yirendai.oss.lib.security.api.VerifyCodeProvider* implementation.

    app.security.verifyCode

  Test with command line tool. TODO  
  
## Integration

#### User and extended properties

  Implements *com.yirendai.oss.lib.security.api.User* and provide extended properties using *public Map&lt;String, Object&gt; getProperties()* method.  

#### Role

  Role represents a group of users has the same privileges.  

  User ..M:N .. Role  
  
  User should provide *org.springframework.security.core.GrantedAuthority* implementations.  
  Authority name should start with "ROLE_" (*com.yirendai.oss.lib.security.api.Security.ROLE_PREFIX*).  


#### Privilege

  Privilege represents an action to a resource.  

  Role ..M:N .. Privilege  
  
  User should provide *com.yirendai.oss.lib.security.api.StaticPrivilege* implementations.  
  Authority name should start with "PRIVILEGE_" (*com.yirendai.oss.lib.security.api.StaticPrivilege.PRIVILEGE_PREFIX*).    


#### (Default) Test user

  User should provide all test users by override com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider&lt;T&gt;#protected List&lt;T&gt; testUsers().  

    app.security.defaultTestUser:'test_user'


## Basic auth

  An auth per request mechanism that carrying base64 auth info by request header.  
  Should be used with https.  
  
  Test with command line tool. TODO  

## Digest auth

  An auth per request mechanism that carrying password hashed auth info by request header.  
  Should be used with https.  
  
  Test with command line tool. TODO  

## UI / Front-end development

  Get user info. TODO  
  Request RSA public key for password encryption. TODO  
  Form auth. TODO  
  Basic auth.  TODO  
  Digest auth.  TODO  

## App security properties

    app.security.enabled=true # true | false, TODO 涉及哪些功能的开关
    app.security.cookieKey= # AES key for encrypt / decrypt cookie, TODO 如何获得, 格式
    app.security.defaultTestUser= # default test user's name, see: com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider .

#### Form login properties
    
    app.security.authEntryPoint=restful # restful | 403 | 401 | template (loginPage), TODO 何时用handler, 何时用entryPoint  
    app.security.authFailureHandler=restful # restful | template (loginPage), handler on auth failed.  
    app.security.authSucessHandler=restful # restful | template, handler on auth succeed.  
    app.security.loginKey= # RSA key pair for encrypt password. TODO 如何获得, 格式  
    app.security.loginPublicKeyUrl=/api/login/publicKey # URL for RSA public key, TODO 如何获得, 格式
    app.security.loginPage=/login
    app.security.loginProcessingUrl=/api/login # URL for login form POST request.  
    app.security.logoutUrl=/api/logout # URL for logout request.  
    app.security.verifyCode=false # true | false, enable verifyCode or not. Need to provide com.yirendai.oss.lib.security.api.VerifyCodeProvider if enabled.  

## Used spring-boot properties
    
    security.ignored= # 
    security.basic.realm=Spring # 

## Customer Filter

| Filter        | Description | Ex  |
| ------------- |:-------------:| -----:|
|PermitedRequestConfiguration|for some request without auth|/img,/js| 
|CsrfConfiguration |generate XSRF-TOKEN||
|FormAuthConfiguration|for ||
|BasicAuthConfiguration| security basic auth||
|PreAuthConfiguration |fot cookie or token||

## RPC

  TODO  

## Debug

    org.springframework.security.web.FilterChainProxy#doFilterInternal
    
    ...
    private void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    ...
        if (filters == null || filters.size() == 0) {
    ...
    see: this.filterChains

    org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource
    
    ...
    public Collection<ConfigAttribute> getAttributes(Object object) {
    ...
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap
        				.entrySet()) {
    ...
    see: this.requestMap
