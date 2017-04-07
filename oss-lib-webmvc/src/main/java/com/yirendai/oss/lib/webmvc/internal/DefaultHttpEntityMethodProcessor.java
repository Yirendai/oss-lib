package com.yirendai.oss.lib.webmvc.internal;

import static org.springframework.web.HttpMessageConverterUtils.defaultContentNegotiationManager;
import static org.springframework.web.HttpMessageConverterUtils.defaultHttpMessageConverters;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.PropertyResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMessageConverterUtils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.FixedContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor;

import java.util.List;

/**
 * Created by zhanghaolun on 16/8/22.
 */
@Slf4j
public class DefaultHttpEntityMethodProcessor implements HandlerMethodReturnValueHandler, InitializingBean {

  /**
   * The {@link ContentNegotiationManager} to use to resolve acceptable media types. If not
   * provided, the default instance of {@code ContentNegotiationManager} with
   * {@link org.springframework.web.accept.HeaderContentNegotiationStrategy
   * HeaderContentNegotiationStrategy} and
   * {@link org.springframework.web.accept.FixedContentNegotiationStrategy
   * FixedContentNegotiationStrategy} (with {@link #setDefaultContentType(MediaType)
   * defaultContentType}) will be used.
   */
  @Getter
  private ContentNegotiationManager contentNegotiationManager;
  /**
   * The default content type that will be used as a fallback when the requested content type is not
   * supported.
   */
  @Getter
  @Setter
  private MediaType defaultContentType;
  /**
   * The message body converters to use for converting an error message into HTTP response body. If
   * not provided, the default converters will be used (see
   * {@link HttpMessageConverterUtils#defaultHttpMessageConverters(Object)}
   * getDefaultHttpMessageConverters()}).
   */
  @NonNull
  private List<HttpMessageConverter<?>> messageConverters;

  // package visibility for tests
  private HandlerMethodReturnValueHandler responseProcessor;
  // package visibility for tests
  private HandlerMethodReturnValueHandler fallbackResponseProcessor;

  @Override
  public boolean supportsReturnType(final MethodParameter returnType) {
    return this.responseProcessor.supportsReturnType(returnType) || //
      this.fallbackResponseProcessor.supportsReturnType(returnType);
  }

  /**
   * handleReturnValue.
   *
   * @param returnValue  must pass a HttpEntity here
   * @param returnType   returnType
   * @param mavContainer mavContainer
   * @param webRequest   webRequest
   * @throws Exception exception
   */
  @Override
  public void handleReturnValue( //
    final Object returnValue, final MethodParameter returnType, //
    final ModelAndViewContainer mavContainer, final NativeWebRequest webRequest //
  ) throws Exception {
    try {
      this.responseProcessor.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    } catch (final HttpMediaTypeNotAcceptableException ex) {
      if (log.isDebugEnabled()) {
        log.debug("Requested media type is not supported, falling back to default one", ex);
      }
      this.fallbackResponseProcessor.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    this.responseProcessor = new HttpEntityMethodProcessor( //
      this.messageConverters, //
      this.contentNegotiationManager //
    );
    this.fallbackResponseProcessor = new HttpEntityMethodProcessor( //
      this.messageConverters, //
      new ContentNegotiationManager( //
        new FixedContentNegotiationStrategy(this.defaultContentType) //
      ) //
    );
  }

  public void setContentNegotiationManager(final ContentNegotiationManager contentNegotiationManager) {
    this.contentNegotiationManager = contentNegotiationManager;
  }

  public List<HttpMessageConverter<?>> getMessageConverters() {
    return this.messageConverters;
  }

  public void setMessageConverters(final List<HttpMessageConverter<?>> messageConverters) {
    this.messageConverters = messageConverters;
  }

  public static DefaultHttpEntityMethodProcessor defaultHttpEntityMethodProcessor( //
    final PropertyResolver propertyResolver, //
    final Object objectMapper //
  ) {
    // before WebMvcAutoConfiguration, cant autowire contentNegotiationManager and httpMessageConverters
    // Requested bean is currently in creation: Is there an unresolvable circular reference

    final ContentNegotiationManager defaultContentNegotiationManager = defaultContentNegotiationManager();
    final List<HttpMessageConverter<?>> defaultHttpMessageConverters = defaultHttpMessageConverters( //
      propertyResolver, objectMapper);

    final DefaultHttpEntityMethodProcessor processor = new DefaultHttpEntityMethodProcessor();
    processor.setContentNegotiationManager(defaultContentNegotiationManager);
    processor.setMessageConverters(defaultHttpMessageConverters);
    return processor;
  }
}
