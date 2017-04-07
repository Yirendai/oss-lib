package com.yirendai.oss.lib.errorhandle.starter;

import com.google.common.collect.ImmutableMap;

import com.yirendai.oss.lib.errorhandle.api.ApplicationException;
import com.yirendai.oss.lib.errorhandle.api.ConcreteExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator;
import com.yirendai.oss.lib.errorhandle.api.StackTraceIndicator;
import com.yirendai.oss.lib.errorhandle.internal.CompositeExceptionResolver;
import com.yirendai.oss.lib.errorhandle.internal.resolver.ApplicationErrorExceptionResolver;
import com.yirendai.oss.lib.errorhandle.internal.resolver.ConstraintViolationExceptionResolver;
import com.yirendai.oss.lib.errorhandle.internal.resolver.DefaultExceptionResolver;
import com.yirendai.oss.lib.errorhandle.internal.resolver.HttpMediaTypeNotSupportedExceptionResolver;
import com.yirendai.oss.lib.errorhandle.internal.resolver.HttpRequestMethodNotSupportedExceptionResolver;
import com.yirendai.oss.lib.errorhandle.internal.resolver.MethodArgumentNotValidExceptionResolver;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.util.ClassUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

@Setter
@Slf4j
public class CompositeExceptionResolverFactoryBean implements FactoryBean<ExceptionResolver<Throwable>> {

  public static final String CONSTRAINT_VIOLATION_EXCEPTION = "javax.validation.ConstraintViolationException";

  @Autowired
  private ExceptionTranslator exceptionTranslator;
  @Autowired
  private StackTraceIndicator stackTraceIndicator;

  @SuppressWarnings({"rawtypes", "unchecked"})
  static Map<Class<? extends Throwable>, ConcreteExceptionResolver> buildInResolvers( //
    final ConversionService conversionService, //
    final ExceptionTranslator exceptionTranslator, //
    final StackTraceIndicator stackTraceIndicator //
  ) {
    final ImmutableMap.Builder<Class<? extends Throwable>, ConcreteExceptionResolver> builder = ImmutableMap.builder();

    if (ClassUtils.isPresent( //
      CONSTRAINT_VIOLATION_EXCEPTION, //
      Thread.currentThread().getContextClassLoader() //
    )) {
      try {
        final Class clazz = Class.forName(CONSTRAINT_VIOLATION_EXCEPTION);
        builder.put(clazz, new ConstraintViolationExceptionResolver());
      } catch (final ClassNotFoundException ex) {
        // ignore
        if (log.isDebugEnabled()) {
          log.debug("class {} not found", CONSTRAINT_VIOLATION_EXCEPTION, ex);
        }
      }
    }

    builder.put(HttpMediaTypeNotSupportedException.class, new HttpMediaTypeNotSupportedExceptionResolver());
    builder.put(HttpRequestMethodNotSupportedException.class, new HttpRequestMethodNotSupportedExceptionResolver());
    builder.put(MethodArgumentNotValidException.class, new MethodArgumentNotValidExceptionResolver());
    builder.put(ApplicationException.class, new ApplicationErrorExceptionResolver());

    final Map<Class<? extends Throwable>, ConcreteExceptionResolver> map = builder.build();
    map.values().forEach(exceptionResolver -> {
      exceptionResolver.setConversionService(conversionService);
      exceptionResolver.setExceptionTranslator(exceptionTranslator);
      exceptionResolver.setStackTraceIndicator(stackTraceIndicator);
    });

    return map;
  }

  @Override
  public ExceptionResolver<Throwable> getObject() {
    final ConversionService conversionService = new DefaultConversionService(); // TODO conversionService

    @SuppressWarnings("rawtypes")
    final Map<Class<? extends Throwable>, ConcreteExceptionResolver> buildInResolvers = buildInResolvers( //
      conversionService, //
      this.exceptionTranslator, //
      this.stackTraceIndicator //
    );

    final DefaultExceptionResolver defaultResolver = new DefaultExceptionResolver();
    defaultResolver.setConversionService(conversionService);
    defaultResolver.setExceptionTranslator(this.exceptionTranslator);
    defaultResolver.setStackTraceIndicator(this.stackTraceIndicator);

    final CompositeExceptionResolver compositeExceptionResolver = new CompositeExceptionResolver();
    compositeExceptionResolver.setDefaultResolver(defaultResolver);
    compositeExceptionResolver.setResolverMap(buildInResolvers);

    return compositeExceptionResolver;
  }

  @Override
  public Class<?> getObjectType() {
    return ExceptionResolver.class;
  }

  @Override
  public boolean isSingleton() {
    return false;
  }
}
