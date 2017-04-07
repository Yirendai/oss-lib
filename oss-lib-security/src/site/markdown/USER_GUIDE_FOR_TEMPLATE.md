# Template应用接入lib-security

## 简介
> 模版应用包括Velocity、Thymeleaf等模版构建的web应用，前后端的资源集成在一起部署。
  
#### 接入示例
##### application.yml配置如下
    
	app:
	  type: TEMPLATE
	  security:
	    basePath: '/auth'
	    cookieKey: 'AES256_CBC16:YDD7uVFNpvkId8HWI6xTfOeRW3O6Wk3FDuGJdnGDhiD='
	    defaultTestUser: 'test_user'
	    enabled: true
	    jwtKey: 'HS512:Ve+/vU5u77+977+977+977+977+9Acu/77+977+977+9OXrvv71XH++/vRLvv73vv73vv73vv71577+9fQLvv73vv71eB++/vW7vv71g77+977+977+9L++/vWDvv73vv73vv71577+9VO+/ve+/vR3vv73vv73Coemfv++/ve+/vQ=='
	    loginKey: 'RSA1024_PAIR_PKCS1_X509:MIICWwIBAAKBgQCRXunHa+5hw+L39UZoIFhPwPR9gBvdlEvlXAH4biP+GNb8OH77OjMsfjfs3dXs3QqwXMajJUKZFpX1v+w/ilZ9muvty4/wfQlx5qy305Ui6mRqpIQKKxkwgAS+LTVCh+AeFWfB9G/OVA/khp73VQ3/IPoqgcPG+e/RD/kdFwTozwIDAQABAoGAALbYqzQqjaxqtxErcWOHS40FJoC0olgSL2ROViwkXSE+HSsh5JYankNYfv9wB6PmY4txJmgOdbYYsoZo4L8LQbzV02cEzAibAltvS8JUZ0XWNbnhRnSvY+GJ24hf1BHIJGjBnkQDK/XrDw8Xzt2bSDlWyWH4iEC7PgpghQpHRZECQQDB1GK0U3iDofSJkdLpypxWE6T6MBl7RJCOROj56cXIaVcamOKD1D+x+aDrP7wH2Crhn5X83cTDGekDEIzs0tHjAkEAv/+A4QP61ZgEKn+1gk7RHw/tWHBLWhb1EcIpShBNWx8sB4G8wI7brf858iLXl9NhROkcqYmbchG3D6SnYZmRJQJAR8kxBTgk2huRRaIMSyoO3JJJ95740P6Dyy0aW/SIm8Dn0aHtwoVJUdDyGC5ypTUaLJW+Jvi7dsaR1eC7ULqDoQJAHoSNRrbsOuEz4FF2V2URxl8wubr3rzUw9Qaoq3YV5aL5y6OqjeznLCwCWOOC40YdEuf+v0/5HlOEhn8Ef/X55QJAKktna3oDVsfsTH6REVLz8kjvhILJ5yk493/yjTZxpcpcVv8LGjuCKtIn3NdCRoeIC1K+oLzbBB6U+J8Uu5aeSA==:MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRXunHa+5hw+L39UZoIFhPwPR9gBvdlEvlXAH4biP+GNb8OH77OjMsfjfs3dXs3QqwXMajJUKZFpX1v+w/ilZ9muvty4/wfQlx5qy305Ui6mRqpIQKKxkwgAS+LTVCh+AeFWfB9G/OVA/khp73VQ3/IPoqgcPG+e/RD/kdFwTozwIDAQAB'
	    loginProcessingUrl: '/login'
	    loginPublicKeyUrl: '/login/rsaPublicKey'
	    logoutUrl: '/logout'
	    permited: '/fonts/*,/img/*,/images/*,/js/*,/login*,/css/*'
	    loginPage : '/login.do'
	    authSucessHandler : '/cluster/list.do'

##### 用户模型
> 继承`com.yirendai.oss.lib.security.api.AbstractUser`类并实现父类抽象方法，按需覆写父类方法。关于额外的用户信息，可以通过扩展`public Map<String, Object> 
getProperties()`来实现

##### 角色模型  
> 继承 `com.yirendai.oss.lib.security.api.AbstractRole` 类，来存储用户角色的信息，

##### 权限模型  
> 继承`com.yirendai.oss.lib.security.api.StaticPrivilege`类，来做静态资源权限的定义,

##### Service层实现  
1. 继承`com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider` 抽象类,
该类已经实现了`UserDetailsService`接口并hook了`ContextRefreshedEvent`事件.
2. lib-security不假定用户的存储逻辑，用户只需重写 findByName、save、delete等方法即可.  
3. 可以重写该类的 testUsers 方法，生成测试用户方便测试。 
4. lib-security配置中的defaultTestUser属性需要配合该方法使用，测试用户必须在用户提供的测试用户列表中进行选择。
 
##### Controller层实现
> 需要实现进入登陆页的入口和登陆成功后跳转的页面，这里成功后跳转到的页面没有列出
    
	@Controller
	@RequestMapping("/auth")
	public class AuthAction extends AbstractAction {
	    /**
	     * 登录页跳转
	     *
	     * @param map
	     * @param request
	     * @return
	     */
	    @RequestMapping(value = "/login", method = RequestMethod.GET)
	    public String login(ModelMap map, HttpServletRequest request) {
	        map.put(getFlag(), "active");
	        map.put(TITLE, "login");
	        return "login";
	    }
	}
