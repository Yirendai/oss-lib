
[官网配置项说明](https://github.com/Netflix/Hystrix/wiki/Configuration)

| 线程池相关 | 说明 | 默认值 | 可选值 |
 --- | --- | --- | --- |
| coreSize | 线程池核心大小 | 10 |  |
| maxQueueSize | 设置BlockingQueue的实现<br>-1 SynchronousQueue<br>否则LinkedBlockingQueue. | -1 | 数字 |
| queueSizeRejectionThreshold | 最大等待队列大小,默认5 | 5 | 数字 |
| keepAliveTimeMinutes | 线程池存活时间,单位分钟,默认1分钟 | 1 | 数字,分钟 |



| 执行相关 | 说明 | 默认值 | 可选值 |
 --- | --- | --- | --- |
| execution.isolation.strategy | 使用命令调用隔离方式,默认:采用线程隔离 | THREAD  | *THREAD线程 |
|  |  |  | SEMAPHORE信号 |
| execution.isolation.thread.timeoutInMilliseconds | 使用线程隔离时，调用超时时间，默认:1秒 | 1000 | 整数 |
| execution.timeout.enabled | 是否启用调用超时 | TRUE | TRUE |
|  |  |  | FALSE |
| execution.isolation.thread.interruptOnTimeout | 使用线程隔离时，是否对命令执行超时的线程调用中断（Thread.interrupt()）操作.默认:true | TRUE | TRUE |
|  |  |  | FALSE |
| execution.isolation.thread.interruptOnCancel | 使用线程隔离时，是否对命令执行取消的线程调用中断（Thread.interrupt()）操作.默认:true | TRUE | TRUE |
|  |  |  | FALSE |
| execution.isolation.semaphore.maxConcurrentRequests | 使用信号量隔离时，命令调用最大的并发数,默认:10  | 10 | 数字 |


| 失败服务降级相关 | 说明 | 默认值 | 可选值 |
 --- | --- | --- | --- |
| fallback.isolation.semaphore.maxConcurrentRequests | 使用信号量隔离时，命令fallback(降级)调用最大的并发数,默认:10 | 10 | 数字 |
| fallback.enabled | 是否开启fallback降级策略 默认:true   | TRUE | TRUE |
|  |  |  | FALSE |


| 命令合并(Collapser)配置 | 说明 | 默认值 | 可选值 |
 --- | --- | --- | --- |
| maxRequestsInBatch | 请求合并是允许的最大请求数,默认: Integer.MAX_VALUE | Integer.MAX_VALUE | 数字 |
| timerDelayInMilliseconds | 批处理过程中每个命令延迟的时间,默认:10毫秒 | 10 | 数字,毫秒 |
| requestCache.enabled | 批处理过程中是否开启请求缓存,默认:开启  | TRUE | TRUE |
|  |  |  | FALSE |
|  |  |  |  |
| circuitBreaker.enabled | 是否启用熔断器,默认true. 启动  | TRUE | TRUE |
|  |  |  | FALSE |
| circuitBreaker.requestVolumeThreshold | 熔断器在整个统计时间内是否开启的阀值，默认20秒。也就是10秒钟内至少请求20次，熔断器才发挥起作用   | 20 | 数字 |
| circuitBreaker.sleepWindowInMilliseconds | 熔断器默认工作时间,默认:5秒.熔断器中断请求5秒后会进入半打开状态,放部分流量过去重试 | 5000 | 数字毫秒数 |
| circuitBreaker.errorThresholdPercentage | 默认:50%。当出错率超过50%后熔断器启动. | 50 | 数字,百分比 |
| circuitBreaker.forceOpen | 是否强制开启熔断器阻断所有请求,默认:false,不开启 | FALSE | TRUE |
|  |  |  | FALSE |
| circuitBreaker.forceClosed | 是否允许熔断器忽略错误,默认false, 不开启  | FALSE | TRUE |
|  |  |  | FALSE |


| 请求上下文 | 说明 | 默认值 | 可选值 |
 --- | --- | --- | :---: |
| requestCache.enabled | 是否开启请求缓存,默认打开 | TRUE | TRUE |
|  |  |  | FALSE |
| requestLog.enabled | 是否开启请求日志,默认打开 | TRUE | TRUE |
|  |  |  | FALSE |


转换成yml格式
```
hystrix:
  command:
    default:
      execution:
        timeout.enabled: true
        isolation:
          strategy: THREAD # 使用命令调用隔离方式,默认:采用线程隔离 *THREAD线程 SEMAPHORE信号
          thread:
            timeoutInMilliseconds: 1443 # 使用线程隔离时，调用超时时间，默认:1秒
            interruptOnTimeout: true # 使用线程隔离时，是否对命令执行超时的线程调用中断（Thread.interrupt()）操作.默认:true
          semaphore.maxConcurrentRequests: 10 # 使用信号量隔离时，命令调用最大的并发数,默认:10
      fallback:
        enabled: true # 是否开启fallback降级策略 默认:true
        isolation.semaphore.maxConcurrentRequests: 10 # 使用信号量隔离时，命令fallback(降级)调用最大的并发数,默认:10
      circuitBreaker:
        enabled: true # 是否启用熔断器,默认true. 启动
        requestVolumeThreshold: 20 # 熔断器在整个统计时间内是否开启的阀值，默认20秒。也就是10秒钟内至少请求20次，熔断器才发挥起作用
        sleepWindowInMilliseconds: 5000 # 熔断器默认工作时间,默认:5秒.熔断器中断请求5秒后会进入半打开状态,放部分流量过去重试
        errorThresholdPercentage: 50 # 默认:50%。当出错率超过50%后熔断器启动.
        forceOpen: false # 是否强制开启熔断器阻断所有请求,默认:false,不开启
        forceClosed: false # 是否允许熔断器忽略错误,默认false, 不开启
      metrics:
        rollingStats:
          timeInMilliseconds: 10000
          numBuckets: 10
        rollingPercentile:
          enabled: true
          timeInMilliseconds: 60000
          bucketSize: 100
      healthSnapshot.intervalInMilliseconds: 500
      requestCache.enabled: true
      requestLog.enabled: true



