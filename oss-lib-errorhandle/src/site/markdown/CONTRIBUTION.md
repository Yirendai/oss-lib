
## 手动测试  

BasicErrorController /error 在某些情况产生循环?

@ResponseBody的/error 可能产生 org.springframework.web.HttpMediaTypeNotAcceptableException: Could not find acceptable representation
导致jetty显示 错误页 Problem accessing /error
断点在 org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod#invokeAndHandle
curl -i -X GET -H 'Accept: application/xml;charset=utf-8' 'http://127.0.0.1:8080/api/error/com.yirendai.oss.lib.errorhandle.test.UnfamiliarException'

jetty的HpptChannel 拿到 error_page 后 没有改变请求方法 导致错误页无法访问
curl -i -X METHOD_NOT_IN_RFC2068 'http://127.0.0.1:8080'

测试 错误页模版不存在
curl -i -X GET -H 'Accept: text/html' 'http://127.0.0.1:8080/errorServlet/com.yirendai.oss.lib.errorhandle.test.ErrorPageNotExistException'

突破requestHeaderSize (maximum header size allowed by the container) 协议解析阶段出错 不会进入正常错误处理流程
curl -i -X GET -H "Big-Header: $(< /dev/urandom tr -dc _A-Z-a-z-0-9 | head -c8192)" 'http://127.0.0.1:8080/index.html'

触发url解码错误 协议解析阶段出错 不会进入正常错误处理流程
curl -i -X GET 'http://127.0.0.1:8080/urlDecode/%25%GG'

触发模版引擎错误 会直接进到 org.eclipse.jetty.servlet.ErrorPageErrorHandler
curl -i -X GET 'http://127.0.0.1:8080/invalidTemplate.html'

触发Servlet抛出指定异常 会直接进到 org.eclipse.jetty.servlet.ErrorPageErrorHandler
curl -i -X GET 'http://127.0.0.1:8080/errorServlet/java.lang.IllegalArgumentException'
curl -i -X GET -H 'Accept: text/html' 'http://127.0.0.1:8080/errorServlet/java.lang.IllegalArgumentException'

触发Controller抛出指定异常 会进到resolveException
curl -i -X GET 'http://127.0.0.1:8080/api/error/java.lang.IllegalArgumentException'

触发UnfamiliarException
curl -i -X GET 'http://127.0.0.1:8080/api/error/com.yirendai.oss.lib.errorhandle.test.UnfamiliarException'

触发404 会直接进到 org.eclipse.jetty.servlet.ErrorPageErrorHandler
curl -i -X GET http://127.0.0.1:8080/pageNotExists

触发405 Method Not Allowed 会进到resolveException
curl -i -X POST --data "x" 'http://127.0.0.1:8080/?trace'

制造超时
curl -i -X GET 'http://127.0.0.1:8080/api/sleep?millis=600000'
