package com.yirendai.oss.lib.errorhandle.starter;

import com.yirendai.oss.lib.errorhandle.api.ExceptionResolver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * handlerExceptionResolver
 * (org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver) used to
 * resolve exceptions through @ExceptionHandler methods. It must be registered first.
 *
 * <p>
 * see:{@link org.springframework.web.servlet.handler.HandlerExceptionResolverComposite}
 * </p>
 *
 * <p>
 * see: https://github.com/jirutka/spring-rest-exception-handler
 * </p>
 *
 * <p>
 * Created by zhanghaolun on 16/8/9.
 * </p>
 */
@Configuration
public class ExceptionResolverConfiguration {

  @Bean(name = ExceptionResolver.COMPOSITE_EXCEPTION_RESOLVER)
  public CompositeExceptionResolverFactoryBean exceptionResolver() {
    return new CompositeExceptionResolverFactoryBean();
  }
}
