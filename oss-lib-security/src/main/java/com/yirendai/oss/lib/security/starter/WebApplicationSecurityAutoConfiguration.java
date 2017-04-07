package com.yirendai.oss.lib.security.starter;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

import com.google.common.collect.ImmutableList;

import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.lib.errorhandle.starter.ErrorHandleAutoConfiguration;
import com.yirendai.oss.lib.security.CompositeAuthenticationProvider;
import com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider;
import com.yirendai.oss.lib.security.internal.feign.FeignTokenConfiguration;
import com.yirendai.oss.lib.security.internal.zuul.ZuulTokenConfiguration;
import com.yirendai.oss.lib.webmvc.starter.WebApplicationAutoConfiguration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.security.FallbackWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.context.NullSecurityContextRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * see: {@link org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration}.
 * see: {@link org.springframework.security.config.annotation.web.builders.WebSecurity}.
 * see: {@link org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler}.
 * </p>
 * <p>
 * see: {@link org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler}.
 * see: {@link org.springframework.security.access.PermissionEvaluator}.
 * </p>
 * Created by zhanghaolun on 16/6/28.
 */
@AutoConfigureAfter({ //
  WebApplicationAutoConfiguration.class, //
  ErrorHandleAutoConfiguration.class, //
  SecurityAutoConfiguration.class})
@AutoConfigureBefore(FallbackWebSecurityAutoConfiguration.class)
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@Configuration
@Import({ //
  VerifyCodeConfiguration.class, //
  PermitedRequestConfiguration.class, //
  PreAuthConfiguration.class, //
  BasicAuthConfiguration.class, //
  FormAuthConfiguration.class, //
  CsrfConfiguration.class, //
  MethodSecurityConfiguration.class, //
  FeignTokenConfiguration.class, //
  ZuulTokenConfiguration.class, //
  SwaggerConfiguration.class})
@Order(WebApplicationSecurityAutoConfiguration.ORDER_AFTER_MANAGEMENT_BEFORE_FALLBACK)
@Slf4j
public class WebApplicationSecurityAutoConfiguration extends WebSecurityConfigurerAdapter {

  /**
   * between LOWEST_PRECEDENCE - 5 and LOWEST_PRECEDENCE - 10.
   */
  public static final int ORDER_AFTER_MANAGEMENT_BEFORE_FALLBACK = LOWEST_PRECEDENCE - 6;

  @Autowired
  private AppProperties appProperties;

  @Qualifier(PermitedRequestConfiguration.PERMITED_REQUESTS)
  @Autowired
  public Map<String, List<String>> permitedRequests;

  @Autowired
  private AuthenticationManager parentAuthenticationManager;
  @Qualifier(PreAuthConfiguration.PRE_AUTH_AUTHENTICATION_PROVIDER)
  @Autowired(required = false)
  private AuthenticationProvider preAuthAuthenticationProvider;
  @Autowired(required = false)
  @SuppressWarnings("rawtypes")
  private BaseUserDetailsAuthenticationProvider userDetailsAuthenticationProvider;

  @Autowired(required = false)
  private List<SecurityConfigurer> securityConfigurers;

  // see: ResourceOwnerPasswordTokenGranter,
  // AuthorizationServerEndpointsConfigurer,
  // ApprovalStoreUserApprovalHandler#updateAfterApproval
  @Override
  protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
    for (final SecurityConfigurer configurer : this.securityConfigurers()) {
      configurer.configure(auth);
    }

    final List<AuthenticationProvider> providers = newArrayList(
      this.preAuthAuthenticationProvider, //
      this.userDetailsAuthenticationProvider //
    ).stream().filter(Objects::nonNull).collect(toList());

    if (!providers.isEmpty()) {
      final CompositeAuthenticationProvider provider = new CompositeAuthenticationProvider();
      provider.setDelegates(ImmutableList.copyOf(providers));

      auth //
        .eraseCredentials(false) //
        .parentAuthenticationManager(this.parentAuthenticationManager) //
        // build a ProviderManager of a DaoAuthenticationProvider
        // .userDetailsService(this.userService) //
        // .passwordEncoder(this.userService.getPasswordEncoder()) //
        // .and()
        .authenticationProvider(provider);
    }
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception {
    http.securityContext().securityContextRepository(new NullSecurityContextRepository()); // not use session

    if (this.appProperties.getSecurityEnabled()) {
      //final String contextPath = this.serverProperties.getContextPath();
      //final RequestMatcher contextPatchMatcher = isNotBlank(contextPath)
      //  ? new AntPathRequestMatcher(this.serverProperties.getPath(contextPath) + "/**", null) : null;
      ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = //
        http
          //.requestMatcher(AnyRequestMatcher.INSTANCE)
          .authorizeRequests();

      for (final Map.Entry<String, List<String>> entry : this.permitedRequests.entrySet()) {
        final String method = entry.getKey();
        final HttpMethod httpMethod = isBlank(method) ? null : HttpMethod.valueOf(method);
        final String[] patterns = entry.getValue().stream().toArray(String[]::new);
        if (httpMethod == null) {
          registry = registry.antMatchers(patterns).permitAll();
        } else {
          registry = registry.antMatchers(httpMethod, patterns).permitAll();
        }
      }
    }

    for (final SecurityConfigurer configurer : this.securityConfigurers()) {
      configurer.configure(http);
    }

    if (this.appProperties.getSecurityEnabled()) {
      http //
        .authorizeRequests() //
        .anyRequest().authenticated();
    } else {
      http //
        .authorizeRequests() //
        .anyRequest().permitAll() //
      ;
    }

    http.headers().frameOptions().sameOrigin();
  }

  @Override
  public void init(final WebSecurity web) throws Exception {
    for (final SecurityConfigurer configurer : this.securityConfigurers()) {
      configurer.init(web);
    }
    super.init(web);
  }

  public List<SecurityConfigurer> securityConfigurers() {
    final List<SecurityConfigurer> all = this.securityConfigurers != null ? this.securityConfigurers : newArrayList();
    final List<SecurityConfigurer> sorted = all.stream().sorted().collect(toList());
    for (final SecurityConfigurer configurer : sorted) {
      log.info("security configurer '{}', order '{}'", configurer.getClass().getName(), configurer.getOrder());
    }
    return sorted;
  }
}
