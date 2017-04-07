# oss-lib-errorhandle  

## 研发背景  

spring框架原生的错误处理实现比较粗糙, 只能满足基本需求.  
无法很方便地适用于前后端分离的RESTful应用及部分或全部使用模版的应用.  

目前存在的问题有:  
没有一整套的跨进程(RPC或API网关/代理)错误信息跟踪机制.  
默认错误信息对用户不够直观(未经翻译).  
默认错误信息对开发人员不够具体.  
默认错误信息查看不够便捷, 经常需要去看日志.  
不能保证在错误发生时记录日志.  
很多开发人员不了解正确的处理错误的方法, 误用较多, 在不同的项目中错误信息结构不统一, 在一些项目中甚至需要包装所有接口返回的数据, 无论是否出错均使用HTTP200状态码.    

## 设计目标  
无论是本地调用还是RPC, 正常流程与异常流程分开, 错误全部归异常流程.  
处理三个层次的异常: Container, Between Container and Component, Component.  
处理两种异常: Framework/Util, Application(GenericError).  
支持resource, restful, template, mixed四种应用.  

对于mixed应用 根据请求自动决定 使用 错误页 或 Restful错误信息.  
对于restful错误信息 使用内容协商确定返回JSON或XML.  
支持spring-boot配置.  
  
使用ResolvedError表示经过解析的错误信息.  
通过实现feign.codec.ErrorDecoder, 实现RPC异常信息传递.  
支持RestTemplate异常处理机制.  
  
实现常见异常信息的转译 通过resolver进行 异常类型->ResolvedError 转换.  
ResolvedError包含trace, path[]信息 在非生成环境显示具体的错误信息.  
在生产环境通过日志记录具体的错误信息.  
  
消息映射 yml? 支持通配符? 基于类型和message映射?  
支持自定义ExceptionResolver.  

## spring-boot默认实现  
  
org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration  
org.springframework.boot.autoconfigure.web.DefaultErrorAttributes    定义了默认错误信息内容  
org.springframework.boot.autoconfigure.web.BasicErrorController      定义了默认错误页面  
  

    DefaultErrorAttributes:
    {
        timestamp: epoch (new Date()),
        status: requestAttributes.javax.servlet.error.status_code,
        error: HttpStatus.valueOf(status).getReasonPhrase(),
        exception: requestAttributes.javax.servlet.error.exception,
        message: Throwable.getMessage | BindingResult.getObjectName + BindingResult.getErrorCount | No errors | requestAttributes.javax.servlet.error.message,
        errors: BindingResult.getAllErrors,
        trace: Throwable.printStackTrace,
        path: requestAttributes.javax.servlet.error.request_uri
    }


## 我们的扩展

    ExtendedErrorAttributes:
    {
        datetime: ISO8601,
        headers:,
        localizedMessage:,
        tracks:
    }

    ApplicationException:
    {
        status:,
        template:,
        contextVariables:
    }


## App type

  Resource
  RESTful  
  Template  
  Mixed  

## RESTful error info

  JSON error info. TODO  
  XML error info. TODO  

## Template error info (Error page)

    server.error.path

## Container / Server error

  Errors that can't handle by oss-lib-errorhandle. TODO  

## Framework / Library error

  Build-in default error messages. TODO  
  User defined error messages for unfamiliar framework/library. TODO  

## Validation error

  TODO

## Status and message template

  TODO

## App error  
  
Brief introduction to *ApplicationError* TODO  
? User defined *ApplicationErrorBuilder* TODO  
About *ConcreteExceptionResolver* TODO  
  
## UI / Front-end development

  How to handle RESTful error. TODO  
  How to handle form validation error. TODO  

## App error properties

    app.error.messages=classpath:app.properties
    app.error.handlerEnabled=false # Experimental

## Used spring-boot properties

    server.error.path=/error
    server.error.includeStacktrace=NEVER # NEVER | ALWAYS | ON_TRACE_PARAM, ON_TRACE_PARAM add trace information when the "trace" request parameter is "true".

## RPC

  TODO  
