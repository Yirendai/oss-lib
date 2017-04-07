# RestFul应用接入lib-security

## 简介
> RestFul方式下，我们约定大部分的接口都是RestFul接口，但是不限制全部接口都必须是Restful接口，允许Template方式的接口存在。

#### 接入示例
##### application.yml配置

    app:
      security:RestFul
        enabled: true                # 是否启用
        basePath: '/auth'            # auth相关的url前缀，下面相关的url默认都会拼接此前缀，默认是 /auth
        loginProcessingUrl: '/login' # form表单提交的用户登录url，默认是 /login
        logoutUrl: '/logout'         # 用户登出的url，默认是 /logout
        loginPublicKeyUrl: /login/publicKey # 获取RSA公钥的url，默认是 /login/publicKey
        defaultTestUser: test_user   # 默认测试用户
        cookieKey: 'keySpec:value'   # 对cookie进行aes加密计算用的key
        jwtKey: 'keySpec:value'      # 对jwt进行签名计算用的key

##### 用户模型
> 继承`com.yirendai.oss.lib.security.api.AbstractUser`类并实现父类抽象方法，按需覆写父类方法。关于额外的用户信息，可以通过扩展`public Map<String, Object> 
getProperties()`来实现 
  
##### service实现
+ `com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider`类已经实现了`UserDetailsService`接口并hook了`ContextRefreshedEvent`事件。
+ lib-security不假定用户的存储逻辑，用户只需重写 findByName、save、delete等方法即可。
	
> 可以重写该类的 testUsers 方法，生成测试用户方便测试。
> lib-security配置中的defaultTestUser属性需要配合该方法使用，测试用户必须在用户提供的测试用户列表中进行选择。系统启动时，会将测试用户插入到持久化存储或内存中，具体依赖于用户实现的`UserDetailsService`中的`save(final User user)` 方法，自定义tsetUser方法如下:  
    
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
    
##### 测试登录
  
    
	  curl -i -X POST -L -c COOKIE --data "username=test_user&password=user_pass" http://127.0.0.1:8080:8080/auth/login
	  
	  HTTP返回值如下所示：    
	  
	  HTTP/1.1 200 OK
	  Date: Tue, 06 Dec 2016 12:26:35 GMT
	  Set-Cookie: generic_user=QPBhNLLca0YvYt0l29hFCP1foCi8ca2FPy7uk0On80TlEY5%2BqUasIzEp6rjc7crxahUJIdbGig9f66iyf2WbpVGTxFwY44hA04Gz6Z4M77P9fhNYyYx7XrUh8BC1J8l%2Fx0FIcxaQ5ASwNB%2FVSu00xJh8%2BizCYvdXBumKckzrclXrEvDlvhkAZzo4jWgm%2BsrbpUDA13S1GzfspG9fOzMBaZ7ais2NsHq%2FWL49NOzMcHXxJKruHeK7jIUk%2B1KNv5RpDZ%2BKtBAWysWKjvgTG%2B3xgbGRjWArCQZwi2YK2AY1Pj6MDeRfdEsdUwY%2F%2B3GejqWAdhYIvQMKJFEZ4KDzJXeX5wqYuYC%2FkwxnVG1hCrIQr7IUoOkXGUMRXoAvtMRKVTLD2creOLCbvDM3ig2%2F3VxYBxyCG0nrwHpkzmDe33E0JpQb1v0hxvYhnC7yRAQwUIYiiVoGRKaK9dAuYtAJIDKDM33R3%2FbZkpN%2Bt7on5%2FEDFwh3qkWVif3F0Iy%2FquyAsQKm2QZ7v5Iiod9EVwEcK3hy%2FoXr6Z874Zg%2B8vtr9EZ4QvYucxHren1apojB0NFCxIuUBvIEX0dZd4qNQsITFWi4JR51FBMfy7LXOY%2BqspfSZzLQKWkg%2BBQEtVaaXh1hcDfcc8YKau5AHN69Z8pKjm%2Bl%2BWi%2Fx2q5dz2sYoJ9%2FZLn1HymgjefyyoQspvKaK3vOxIWd%2FvJa5EsfknAoIMKDrTN3A1TuNK%2B5EZz8UprSikJt1N6xV0FY3aoOQjyKhxkaQ%2BZmNj27bjc6X1f%2BbuIBmV85%2BuN3ZhINcDV9hSfqdJqu06aOuUGq8icuL14h6leyugrBsPF2kbiLppy%2Fj27Srdag5xfx6J%2BJtaKwVc1IWuTy63fWMhc%2Bk%2Fzuks6wd594OEU;Version=1;Path=/;Expires=Tue, 06-Dec-2016 13:26:35 GMT;Max-Age=3600;HttpOnly;Comment=generic_user
	  Expires: Thu, 01 Jan 1970 00:00:00 GMT
	  X-Auth-Token: r%2BrlCMT9lZSHW%2BoOp2Lp2OXvR%2Bq%2FrOVGIBe39RxZIkZgz7lC8WrjXhI%2B40nN89W2aB0JZO4X7sT0%2BWxo6Aglv%2FqyvwjUYB8Q2SCLqqx%2BmpaoOXBXLbwQQqdG7EBGc8MPl%2BkWR4g69pZEFurtMUEOSYu9dEXpKN2yVyFGo9EcX1kE9YfKvNTBrSyS1S2Vh0l1SFtwKVBFUJ5XQSrX%2FoSn0syaIAw8dPPetDHns9s0AfYyqStbcH5uOlr3QVTsN%2Fx4jS0%2F9%2FlK9tIvBFQCdhqcNsmgZXTqM36Zxw2Y4sV6Ri6u22c2Hk6Y4jPWVcs%2F3KfLzQmD4b1zK2U2yxYQ6gqEeDBdDzyke9ozqRJuelT%2BK7G28zbANXRr91q%2FgGlG3AkUYo7Ak%2FBhq88O8z799iDfmHxca1Jfb8jKy1qGJn77jwIKxYpZzEpZNm1T%2BkWpMe0lRvqZ2eHg7VYFKWiHgPQleNwdosxbmOrbZqtEejNVutrgVN%2Fm1JytPnTldZzueyexIw2MrFDZvZSrRuzt0CMWoCA9nlriTaUksILZjFuJeeGjGHhzQXI1X3xPhet6qk8pkrj4BNiHKZyiAcOw6mtyeLiKuyVoWWMbBN3mJsU7LWpj3Kg18%2Bu%2BcF%2F%2BbPH%2BDiz%2BPrnhyYEeHOeavN8uOkvQU%2Fws12VSkY8zC9SlS3RIfZSh2RYoVFXScQgDXjPYxFCJjULEV5ARfqmqEyOXNm%2Fp2YnEG3H%2F4EQ8nJMTDJlD7UjK%2FVxRSgTzlFyNSVTkRuDV9mSXyVlpdxStMgCbCcVYOKDKA5MI%2Fhtk7kB%2BsBjXjXE2cPGt3%2B9A2%2FKOj%2BL8pkFR%2BO8vDxTFuP4f4Wy%2BZvb6G4iHnwBF1kSnnEHVCPDjsvJKvvDYg7eGGAWCcNMDi9JL
	  Content-Type: application/json;charset=UTF-8
	  X-Content-Type-Options: nosniff
	  X-XSS-Protection: 1; mode=block
	  X-Frame-Options: SAMEORIGIN
	  Transfer-Encoding: chunked
	  
	  {"authorities":["ROLE_USER","PRIVILEGE_DATA_VIEW"],"enabled":true,"id":"1","name":"test_user","password":"[PROTECTED]","username":"test_user"}
    
  
> lib-security分别在 `Cookie` 和 `X-Auth-Token` header中，返回了token信息。在Response Body中返回用户基本信息。
  
##### 资源访问
  
支持客户端在请求资源时，以如下两种方式携带token信息(token信息即用户登录成功后，在cookie和X-Auth-Token中返回的数据)。  
> Cookie方式
    
	curl -i -X GET -L --COOKIE "generic_user=WFaaCs%2Fp2l62F%2BxfJi%2BZ6CPAYDHWzGR0pYO8Fz3tb7KC%2BrYn5bMd9SEUAeWRxC3XcKBqwhulTmNaiO4lnTz0L9mS49SaNbY6P3f0ApVXcIJASqTberscebJbm%2BLixkkHXWUsBt5Hwlm8ibmYynvm9j9ukWe2DzL%2Fy38EI5Aoa3KxQ1Izti2E3nolmhUNz4Nxfc4xu3JB2RXsjhvICOiCo1hTa1LxgKnORe8BsHmqFFV8C9LCCyiQOH%2FMH2dSUKK1bjDLcPjfu4D4f2ZcadghhOjJ0DAh51wrUoi0vo8gwj86XpKg0UZOEHfMdAZYpeW%2FC9ly1zG8th29hDlBfTK9iOkvgOVP4NDY0X%2BmJtbZjDlaB79jaDzACUiMndFWsdIMxFC1Lo09U9QYhcSarO6zJPR2Af1teu%2BScb8m51gmeNo%2FLnJfu2gf867ndfzE4cC5KWlwD9OlVWcF%2FEm0tiLQtM1SRs7HNxn7Q4t0jZOHeCuIPr9POhuK6WT0%2Fy3csvG%2BLLrr6AluFfQWHa0nlKhKopv5x%2BFhrwFZM5xfJfxsIsT%2FH%2FAHBTcNRogI5P4pGx5Es6WyTNi%2BhICan%2BjAXFMmSSWsaMlS2eel4B3%2BPJF5ERafLFJm%2FkXQRx9bKgKGQ%2BquE%2FPiDb2ugocC5EdQBaK1SBTwlFWH6B5tl1QQgEPHMZS2B8QK4s0DfnmV3gnm0RtFEQ6e49%2FdvGBe1LJc8tJ1pDy%2FmOTV%2F%2F7v4VkleGQnBhH%2FTURwFHmn%2B4Eu82k%2BvfYryLG6fuMIBwmRwB8BW5ELaoaEu9urQR%2FG6A8i0gkkPyuuJMpLOFZIIsz51wx6mgVzP1CZ0SYObBseIj9tMKtiZJP6DfvTmxtCHuo0ktlCvW%2FoP1vi0dfoELS9qYbWzZu0;Version=1;Path=/;Expires=Tue, 06-Dec-2016 13:40:56 GMT;Max-Age=3600;HttpOnly;Comment=generic_user" http://127.0.0.1:8080:8080/auth/login/rsaPublicKey
	  
	HTTP返回值如下所示：  
	  
	HTTP/1.1 200 OK
	Date: Tue, 06 Dec 2016 13:00:10 GMT
	Content-Type: application/json;charset=UTF-8
	X-Content-Type-Options: nosniff
	X-XSS-Protection: 1; mode=block
	Cache-Control: no-cache, no-store, max-age=0, must-revalidate
	Pragma: no-cache
	Expires: 0
	X-Frame-Options: SAMEORIGIN
	Transfer-Encoding: chunked
	  
	{"spec":"RSA1024_PUB_X509","value":"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRXunHa+5hw+L39UZoIFhPwPR9gBvdlEvlXAH4biP+GNb8OH77OjMsfjfs3dXs3QqwXMajJUKZFpX1v+w/ilZ9muvty4/wfQlx5qy305Ui6mRqpIQKKxkwgAS+LTVCh+AeFWfB9G/OVA/khp73VQ3/IPoqgcPG+e/RD/kdFwTozwIDAQAB","present":true}
      
> X-Auth-Token方式

 	curl -i -X GET -L --HEADER "X-Auth-Token: KTzX7b%2BYce9STH4LdI%2BreV1UcwTXd7ACgROYNOaLf9RoBdOLC9s4fkYM%2ByJLHzZfzjUUoWbpbwGS6%2Ft8yLJpXgin7BGAgGaMTXoi%2B7%2BM8ue4rcWecgssIuzxftn%2BTvniprnYiEGqt3rfG0lqTSptkR6PZ%2F3laPkMsFSm6clDi4vQ4uYUSfkUwhC7EFLSERD9aa%2B1kjJDpXiuQVx9f37qSa%2FtT7OTcoVIwXy%2FQCcx6D4m7Ksdu7G0p%2FF7vyhXqcZ0fQNL%2FeCM9bH%2F%2FOzpPo0ryaa7EcYhVHpw2XLV%2FC6uxgrrwrx488%2BgSYqcVhHiFSdRFUii8m7eH0pPtW%2FNvuJl067FhT5QU5k%2BH%2F4vxUrqcdXEgThXxzmmIWOP0mYVLHM9TuXgOSdEjxUi9Dp4AsDIHe1%2FbQIvEYXJSVIFAv1g7iU31YSLBdyQXcMMjW1LFd2UOeu6EmigGKUJUQpES4KN4eKsN9RZYzGxQZr1LZQFksSyUY%2Fz6gtKS%2Ff3Ux7T9VrU1TfbI6XDV8%2Bi1pC%2Bm601eb6Vb1jx9QIboIGypVIE1FRVDaKNMw6TtPTZ1LhulipgrmfI8NZw23EwsE8v8Mywm3rWxHdh9waDhIfPAfBUODIUWXAjuACP1estvWF7st8SdQ%2B26gxPQ%2FZzbc%2B5hvBDuBLrUtRXZcIM6H65tkU3LRmaCxJ7JHROxzqFvTsk0Gk57oCVk7t%2BTKU2xA2z8SdDu1nmiU1rjqydv0uz8iANg%2FdRPRUqH8VHhoo28YeNrGUzl%2FvoF7nwGHIvgM%2BxxbC3c4VW%2B3422bq22%2B6B3kkeJVPtCUo0NhZWffssM4lAohrYA0Jzx4fpPD5Y00qf6%2By%2FDNDFsOO9kIU5LSMU4Lfn1VzfR%2BnpO77HHmfjRYKT2Grk" http://127.0.0.1:8080:8080/auth/login/rsaPublicKey
	  
	HTTP返回值如下所示：
	  
	HTTP/1.1 200 OK
	Date: Tue, 06 Dec 2016 13:02:42 GMT
	Content-Type: application/json;charset=UTF-8
	X-Content-Type-Options: nosniff
	X-XSS-Protection: 1; mode=block
	Cache-Control: no-cache, no-store, max-age=0, must-revalidate
	Pragma: no-cache
	Expires: 0
	X-Frame-Options: SAMEORIGIN
	Transfer-Encoding: chunked
	  
	{"spec":"RSA1024_PUB_X509","value":"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRXunHa+5hw+L39UZoIFhPwPR9gBvdlEvlXAH4biP+GNb8OH77OjMsfjfs3dXs3QqwXMajJUKZFpX1v+w/ilZ9muvty4/wfQlx5qy305Ui6mRqpIQKKxkwgAS+LTVCh+AeFWfB9G/OVA/khp73VQ3/IPoqgcPG+e/RD/kdFwTozwIDAQAB","present":true}
  
##### 测试登出
  
	curl -i -X GET -L -b COOKIE -c COOKIE http://127.0.0.1:8080:8080/auth/logout
	  
	返回值如下：
	  
	HTTP/1.1 200 OK
	Date: Tue, 01 Nov 2016 14:26:17 GMT
	Set-Cookie: generic_user=;Version=1;Path=/;Expires=Thu, 01-Jan-1970 00:00:00 GMT;Max-Age=0;Comment=generic_user
	Expires: Thu, 01 Jan 1970 00:00:00 GMT
	X-Content-Type-Options: nosniff
	X-XSS-Protection: 1; mode=block
	X-Frame-Options: SAMEORIGIN
	Content-Length: 0
    
##### 示例代码
> [todomvc-app](https://github.com/Yirendai/oss-samples/tree/develop/oss-todomvc/oss-todomvc-app)
