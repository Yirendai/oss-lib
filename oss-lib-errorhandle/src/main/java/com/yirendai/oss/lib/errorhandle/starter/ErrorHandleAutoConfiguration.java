package com.yirendai.oss.lib.errorhandle.starter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static com.yirendai.oss.boot.autoconfigure.AppErrorProperties.SearchStrategy.HIERARCHY_FIRST;
import static com.yirendai.oss.lib.common.Jackson2Utils.getJackson2Present;
import static com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator.Location.HIERARCHY_FIRST_COMPARATOR;
import static com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator.Location.ORDER_FIRST_COMPARATOR;
import static com.yirendai.oss.lib.errorhandle.api.ResolvedError.RESOLVED_ERROR_COOKIE;
import static java.util.concurrent.TimeUnit.DAYS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirendai.oss.boot.autoconfigure.AppErrorProperties;
import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.boot.autoconfigure.ConditionalOnNotEnvProduction;
import com.yirendai.oss.lib.common.msginterpolate.SpelMessageInterpolator;
import com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator;
import com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator.Location;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.errorhandle.internal.BaseErrorController;
import com.yirendai.oss.lib.errorhandle.internal.ContentCachingRequestFilter;
import com.yirendai.oss.lib.errorhandle.internal.DefaultStackTraceIndicator;
import com.yirendai.oss.lib.errorhandle.internal.ExtendedErrorAttributes;
import com.yirendai.oss.lib.errorhandle.internal.rpc.FeignErrorDecoderConfiguration;
import com.yirendai.oss.lib.errorhandle.internal.translator.DefaultExceptionTranslator;
import com.yirendai.oss.lib.webmvc.api.DomainResolver;
import com.yirendai.oss.lib.webmvc.api.JsonToken;
import com.yirendai.oss.lib.webmvc.api.TokenBasedCookie;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;
import com.yirendai.oss.lib.webmvc.api.UrlEncodedToken;
import com.yirendai.oss.lib.webmvc.internal.DefaultHttpEntityMethodProcessor;
import com.yirendai.oss.lib.webmvc.starter.WebApplicationAutoConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.annotation.ResponseStatusExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.servlet.Servlet;

/**
 * A replacement of {@link org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration}.
 * see: {@link org.springframework.boot.autoconfigure.web.DefaultErrorAttributes}
 * see: {@link org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration}
 * Extends WebMvcConfigurerAdapter make this visable to MockMvc tests,
 * does not override it's method.
 */
@AutoConfigureBefore({ErrorMvcAutoConfiguration.class, WebMvcAutoConfiguration.class, SecurityAutoConfiguration.class})
@AutoConfigureAfter({WebApplicationAutoConfiguration.class})
@ComponentScan(basePackages = {"com.yirendai.oss.lib.errorhandle.starter"})
@ConditionalOnClass({Servlet.class, DispatcherServlet.class})
@ConditionalOnWebApplication
@Configuration
@EnableConfigurationProperties(value = {AppProperties.class, DefaultStackTraceIndicator.class})
@Import({ //
  ExceptionResolverConfiguration.class, //
  FeignErrorDecoderConfiguration.class, //
  ExceptionHandlerConfiguration.class})
@ServletComponentScan(basePackages = {"com.yirendai.oss.lib.errorhandle.filter"})
@Slf4j
public class ErrorHandleAutoConfiguration extends WebMvcConfigurerAdapter implements ApplicationContextAware {

  private ApplicationContext applicationContext;

  @Autowired
  private AppProperties appProperties;

  @Autowired
  private DefaultHttpEntityMethodProcessor defaultHttpEntityMethodProcessor;

  @Autowired
  private DomainResolver domainResolver;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private ServerProperties serverProperties;

  @Bean
  @ConditionalOnNotEnvProduction
  public ContentCachingRequestFilter contentCachingRequestFilter() {
    return new ContentCachingRequestFilter();
  }

  @Bean(name = RESOLVED_ERROR_COOKIE)
  @ConditionalOnMissingBean(name = RESOLVED_ERROR_COOKIE)
  public TypeSafeCookie<ResolvedError> resolvedErrorCookie() {
    return buildResolvedErrorCookie(this.domainResolver, this.objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean(value = ErrorAttributes.class, search = SearchStrategy.CURRENT)
  public ExtendedErrorAttributes errorAttributes() {
    return new ExtendedErrorAttributes();
  }

  @Bean
  //@ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
  @ConditionalOnProperty(prefix = "app", name = "type", havingValue = "RESTFUL", matchIfMissing = false)
  public BaseErrorController.RestfulErrorController restfulErrorController( //
    final ExtendedErrorAttributes errorAttributes) {
    return new BaseErrorController.RestfulErrorController(errorAttributes, this.serverProperties.getError());
  }

  @Bean
  //@ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
  @ConditionalOnProperty(prefix = "app", name = "type", havingValue = "TEMPLATE", matchIfMissing = false)
  public BaseErrorController.TemplateErrorController templateErrorController( //
    final ExtendedErrorAttributes errorAttributes) {
    return new BaseErrorController.TemplateErrorController(errorAttributes, this.serverProperties.getError());
  }

  @Bean
  //@ConditionalOnMissingBean(value = ErrorController.class, search = SearchStrategy.CURRENT)
  @ConditionalOnProperty(prefix = "app", name = "type", havingValue = "MIXED", matchIfMissing = true)
  public BaseErrorController.MixedErrorController mixedErrorController( //
    final ExtendedErrorAttributes errorAttributes) {
    return new BaseErrorController.MixedErrorController(errorAttributes, this.serverProperties.getError());
  }

  @Bean
  public ExceptionTranslator exceptionTranslator() {
    return buildExceptionTranslator(this.appProperties.getError().getSearchStrategy());
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  // ------------------------------ experimental WebMvcConfigurerAdapter ------------------------------

  /**
   * see: {@link WebMvcConfigurationSupport#handlerExceptionResolver()}.
   *
   * @param resolvers resolvers
   */
  @Override
  public void configureHandlerExceptionResolvers(final List<HandlerExceptionResolver> resolvers) {
    // call this addDefaultHandlerExceptionResolvers ?
  }

  /**
   * @param resolvers exceptionResolvers
   * @deprecated see: {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
   * #addDefaultHandlerExceptionResolvers(List) addDefaultHandlerExceptionResolvers}
   */
  @Deprecated
  protected final void addDefaultHandlerExceptionResolvers( //
    final List<HandlerExceptionResolver> resolvers //
  ) {
    final List<ResponseBodyAdvice<?>> interceptors = newArrayList();
    if (getJackson2Present()) {
      interceptors.add(new JsonViewResponseBodyAdvice());
    }
    final ExceptionHandlerExceptionResolver handlerExceptionResolver = new ExceptionHandlerExceptionResolver();
    handlerExceptionResolver.setMessageConverters(this.defaultHttpEntityMethodProcessor.getMessageConverters());
    handlerExceptionResolver.setResponseBodyAdvice(interceptors);
    handlerExceptionResolver.setApplicationContext(this.applicationContext);
    handlerExceptionResolver.afterPropertiesSet();

    final ResponseStatusExceptionResolver responseStatusExceptionResolver = new ResponseStatusExceptionResolver();
    responseStatusExceptionResolver.setMessageSource(this.applicationContext);

    resolvers.add(handlerExceptionResolver);
    // resolvers add responseStatusExceptionResolver ?
    // resolvers add new DefaultHandlerExceptionResolver ?
  }

  public static ExceptionTranslator buildExceptionTranslator(final AppErrorProperties.SearchStrategy searchStrategy) {
    final Optional<MessageSource> messageSourceOptional = messageSource("classpath:/errorhandle/default");
    final MessageSource defaultMessageSource = messageSourceOptional.isPresent() ? messageSourceOptional.get() : null;
    checkNotNull(defaultMessageSource, "message source classpath:/errorhandle/default not present");

    final Optional<MessageSource> applicationMessageSource = messageSource("classpath:/errorhandle/application");
    final List<MessageSource> messageSources = applicationMessageSource.isPresent() ? //
      newArrayList(applicationMessageSource.get(), defaultMessageSource) : //
      newArrayList(defaultMessageSource);

    final Comparator<Location> locationComparator = HIERARCHY_FIRST == searchStrategy ? //
      HIERARCHY_FIRST_COMPARATOR : ORDER_FIRST_COMPARATOR;

    final DefaultExceptionTranslator defaultExceptionTranslator = new DefaultExceptionTranslator();
    defaultExceptionTranslator.setLocationComparator(locationComparator);
    defaultExceptionTranslator.setMessageSources(messageSources);
    defaultExceptionTranslator.setMessageInterpolator(new SpelMessageInterpolator());
    return defaultExceptionTranslator;
  }

  public static TypeSafeCookie<ResolvedError> buildResolvedErrorCookie( //
    final DomainResolver domainResolver, final ObjectMapper objectMapper) {
    final JsonToken<ResolvedError> jsonToken = new JsonToken<>(ResolvedError.class, objectMapper);
    final UrlEncodedToken<ResolvedError> urlEncodedToken = new UrlEncodedToken<>(jsonToken);
    final int maxAge = (int) DAYS.toSeconds(1L);
    return new TokenBasedCookie<>(domainResolver, false, maxAge, "resolved_error", false, urlEncodedToken);
  }

  static Optional<MessageSource> messageSource(final String location) {
    final Optional<MessageSource> result;
    if (StringUtils.isNotBlank(location)) {
      final ReloadableResourceBundleMessageSource messages = new ReloadableResourceBundleMessageSource();
      messages.setBasename(location);
      messages.setDefaultEncoding("UTF-8");
      messages.setFallbackToSystemLocale(false);
      result = Optional.of(messages);
    } else {
      log.info("errorhandle messageSource not found at {}", location);
      result = Optional.empty();
    }
    return result;
  }
}
