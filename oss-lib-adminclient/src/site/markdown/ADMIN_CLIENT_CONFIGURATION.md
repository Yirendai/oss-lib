# oss-admin客户端配置说明
## 背景
微服务下，各个业务节点都会连接到服务发现应用，比如eureka， 这就需要一些基本的eureka客户端配置。另外，admin的一些基本功能比如实时log查看也需要进行必要的配置和，如果这些配置错误或者配置不够完整，就会出现各种问题。
## eureka服务发现配置
### 服务端口
任何spring boot应用都有两类端口， `server.port`, `management.port`。
### 服务路径
任何spring boot应用都有两类path， `server.context-path`, `management.context-path`。
### 配置详情
通过测试，我们发现如果配置两个端口的话，`statusPageUrlPath`和`healthCheckUrlPath`的配置方式与配置一个端口时差别较大，建议服务端口、管理端口使用同一个端口。
 * `eureka.client.serviceUrl.defaultZone`
该配置指明客户端要连接的eureka服务的路径。可以配置默认值。
 * `eureka.client.instance.hostname`
该配置指明当前客户端的运行主机的主机名。`statusPageUrlPath`和`healthCheckUrlPath`要使用该hostname进行拼接。
 * `eureka.client.instance.instance-id`
 该配置定义了eureka的实例id。
 * `eureka.client.instance.prefer-ip-address`
 是否已IP作为交互方式，一般选择false。
 * `eureka.client.instance.statusPageUrlPath`
 配置eureka客户端的状态路径访问路径，不能出错。
 * `eureka.client.instance.healthCheckUrlPath`
 配置eureka客户端的健康路径。eureka首页的客户端链接，以及oss-admin都会访问该url来判断系统的运行状态。
 * `eureka.client.instance.metadata-map.management.context-path`
 将客户端的`management.context-path`配置添加到eureka的metadata中。
 * `eureka.client.instance.metadata-map.management.port`
 将客户端的`management.port`配置添加到eureka的metadata中。建议和`server.port`配置为同一个端口。
### admin client metadata配置
>如果admin客户端项目使用了configserver， 其中commonconfig中自动添加了对接admin server的配置，使用者只需要在docker-compose文件中配置对应的环境变量即可。具体配置示例：

    version: '2.1'
    services:
      todomvc-app:
        image: ${DOCKER_REGISTRY:-registry.docker.local}/oss-todomvc-app:latest
        restart: 'always'
        container_name: ${SPRING_APPLICATION_NAME:-oss-todomvc-app}_${EUREKA_INSTANCE_HOSTNAME:-local-oss-todomvc-app}
        hostname: ${EUREKA_INSTANCE_HOSTNAME:-local-oss-todomvc-app}
        ports:
        - "${EUREKA_INSTANCE_NONSECUREPORT:-8080}:${SERVER_PORT:-8080}"
        volumes:
        - oss-todomvc-app-volume:/root/data
        environment:
        - EUREKA_INSTANCE_NONSECUREPORT=${EUREKA_INSTANCE_NONSECUREPORT:-8080}
        - EUREKA_INSTANCE_HOSTNAME=${EUREKA_INSTANCE_HOSTNAME:-local-oss-todomvc-app}
        - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:-http://user:user_pass@local-eureka:8761/eureka/}
        - MANAGEMENT_CONTEXTPATH=${MANAGEMENT_CONTEXTPATH}
        - MANAGEMENT_PORT=${MANAGEMENT_PORT:-8080}
        - SERVER_CONTEXTPATH=${SERVER_CONTEXTPATH}
        - SERVER_PORT=${SERVER_PORT:-8080}
        - SPRING_CLOUD_CONFIG_DISCOVERY_SERVICEID=${SPRING_CLOUD_CONFIG_DISCOVERY_SERVICEID:-local-configserver}
#### 注意事项
虽然可以任意配置server,但在使用feign client的时候, 在制定RequestMapping的时候要把server context path和provider本身的RequestMapping拼接起来,使用的时候要尤其注意。
##### provider接口定义
    @GetMapping("/server/{id}")
    public User findById(@PathVariable Long id) {
      User findOne = this.userRepository.findOne(id);
      return findOne;
    }
##### provider启动脚本 
    java -Dspirng.profiles.active=same.port -DSERVER_CONTEXTPATH=/svr -DSERVER_PORT=10088 -jar target/microservice-provider-user-*.jar

##### feignclient定义
    @FeignClient(name = "microservice-provider-user")
    public interface UserFeignClient {
      @RequestMapping("/svr/server/{id}")
      public User findByIdFeign(@PathVariable("id") Long id);
    }
### 示例
以下配置是oss-turbine的application.yml关于eureka的配置，当`server.port`和`management
.port`为同一个端口时，配置如下面profile的配置，当二者不同时，`statusPageUrlPath`， `healthCheckUrlPath`， `metadata-map.management
.context-path`三个配置值均不同， **为了尽可能减少出错，建议两个端口一致，并按照如下值进行配置。**

#### `server.port`和`management.port`为同一个端口时的配置
    server:
      port: ${SERVER_PORT:8989}
      context-path: ${SERVER_CONTEXTPATH:}
    
    management:
      context-path: ${MANAGEMENT_CONTEXTPATH:}
      port: ${MANAGEMENT_PORT:${server.port:${SERVER_PORT:8989}}}
    
    eureka:
      client:
        serviceUrl:
          defaultZone: ${EUREKA_CLIENT_SERVICEURL_DEFAULTZONE:http://user:user_pass@local-eureka:8761/eureka/}
      instance:
        hostname: ${EUREKA_INSTANCE_HOSTNAME:local-${spring.application.name}}
        nonSecurePort: ${EUREKA_INSTANCE_NONSECUREPORT:8080}
        prefer-ip-address: ${EUREKA_INSTANCE_PREFER_IP_ADDRESS:false}
        instance-id: ${spring.application.name}:${EUREKA_INSTANCE_HOSTNAME:local-${spring.application.name}}:${EUREKA_INSTANCE_NONSECUREPORT:${SERVER_PORT:8080}}:${random.value}
        leaseRenewalIntervalInSeconds: 10
        statusPageUrlPath: ${server.context-path:${SERVER_CONTEXTPATH:}}${management.context-path:${MANAGEMENT_CONTEXTPATH:}}${endpoints.info.path:/info}
        healthCheckUrlPath: ${server.context-path:${SERVER_CONTEXTPATH:}}${management.context-path:${MANAGEMENT_CONTEXTPATH:}}${endpoints.health.path:/health}
        metadataMap:
          management.context-path: ${server.context-path:${SERVER_CONTEXTPATH:}}${management.context-path:${MANAGEMENT_CONTEXTPATH:}}
          management.port: ${management.port:${MANAGEMENT_PORT:${server.port:${SERVER_PORT:8080}}}}

#### `server.port`和`management.port`为不同端口时的配置
    server:
      port: ${SERVER_PORT:8080}
      context-path: ${SERVER_CONTEXTPATH:}
    
    management:
      context-path: ${MANAGEMENT_CONTEXTPATH:}
      port: ${MANAGEMENT_PORT:8081}
    
    eureka:
      instance:
        preferIpAddress: false
        status-page-url: http://${application.name:${APPLICATION_NAME:localhost:8080}}:${management.port:${MANAGEMENT_PORT:8081}}${management.context-path:${MANAGEMENT_CONTEXTPATH:}}${endpoints.info.path:/info}
        health-check-url: http://${application.name:${APPLICATION_NAME:localhost:8080}}:{management.port:${MANAGEMENT_PORT:8081}}${management.context-path:${MANAGEMENT_CONTEXTPATH:}}${endpoints.health.path:/health}
        metadata-map:
          management.context-path: ${management.context-path:${MANAGEMENT_CONTEXTPATH:}}
          management.port: ${management.port:${MANAGEMENT_PORT:${server.port:${SERVER_PORT:8080}}}}
## 实时日志配置
admin支持客户端日志的实时查看，需要添加以下两个配置到
### 配置详情
  * `logging.file`
  Log file name. For instance `myapp.log`
  * `logging.path`
  Location of the log file. For instance `/var/log`

### 示例
    logging:
      file: ${LOGGING_FILE:oss-admin}.log
      path: ${LOGGING_PATH:${user.home}/data/oss-admin/logs}
      
## management security配置
该配置为spring boot应用的管理接口（主要是actuator提供的各个endpoints）提供基本的basic authentication。在没有其他安全策略的spring 
boot应用中，该配置还承担了service接口的安全控制。
### 配置详情
 * `security.user.role` 为该默认用户授权的角色， 有USER和ADMIN两个值。

### 示例
    security.user:
         name: admin
         password: ${SECURITY_USER_PASSWORD:admin_pass}
         role: ADMIN
         
  > NOTES:不使用lib-security做应用的权限认证时，可以添加上面的管理用户和密码。如果项目同时使用了lib-security，则一定要注意，
  这里的admin的管理用户可以和lib-security的用户使用相同的数据源，只要用户具有'ADMIN'角色即可。另外，如果一定要在这里单独配置
  management的用户和密码，一定要注意，**这里的用户不要和lib-security的数据源的用户数据重复**。
  
## 定义springbeans endpoint
定制actuator endpoints, 比如beans接口,添加如下配置后,访问/springbeans 返回系统所有实例化bean信息

    endpoints.beans.id=springbeans
    endpoints.beans.sensitive=true
    endpoints.beans.enabled=true
