package com.yirendai.oss.lib.security.starter;

import static com.google.common.base.Preconditions.checkState;
import static com.yirendai.oss.boot.autoconfigure.AppSecurity.ENABLED;
import static com.yirendai.oss.boot.autoconfigure.AppType.MIXED;
import static com.yirendai.oss.boot.autoconfigure.AppType.RESOURCE;
import static com.yirendai.oss.boot.autoconfigure.AppType.RESTFUL;
import static com.yirendai.oss.boot.autoconfigure.AppType.TEMPLATE;
import static com.yirendai.oss.lib.errorhandle.api.ResolvedError.RESOLVED_ERROR_COOKIE;
import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_COOKIE;
import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_TOKEN;
import static com.yirendai.oss.lib.security.internal.rest.RestfulAuthenticationEntryPoint.restfulEntryPoint;
import static com.yirendai.oss.lib.security.internal.template.TemplateAuthenticationEntryPoint.templateEntryPoint;
import static com.yirendai.oss.lib.security.internal.template.TemplateAuthenticationFailureHandler.templateFailureHandler;
import static com.yirendai.oss.lib.security.internal.template.TemplateAuthenticationLogoutSuccessHandler.templateLogoutSuccessHandler;
import static com.yirendai.oss.lib.security.internal.template.TemplateAuthenticationSuccessHandler.templateSuccessHandler;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.http.HttpMethod.GET;

import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.boot.autoconfigure.AppSecurityProperties;
import com.yirendai.oss.boot.autoconfigure.AppType;
import com.yirendai.oss.boot.autoconfigure.ConditionalOnAppSecurity;
import com.yirendai.oss.boot.autoconfigure.ConditionalOnAppType;
import com.yirendai.oss.boot.autoconfigure.OnAppTypeCondition;
import com.yirendai.oss.lib.common.crypto.Cryptos;
import com.yirendai.oss.lib.common.crypto.KeyExpression;
import com.yirendai.oss.lib.common.crypto.Rsa;
import com.yirendai.oss.lib.common.crypto.RsaKey;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.errorhandle.internal.RestfulExceptionHandler;
import com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider;
import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.crypto.ReentrantBCryptPasswordEncoder;
import com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenCookieClearingLogoutHandler;
import com.yirendai.oss.lib.security.internal.rest.RestfulAuthenticationFailureHandler;
import com.yirendai.oss.lib.security.internal.rest.RestfulAuthenticationSuccessHandler;
import com.yirendai.oss.lib.security.internal.rest.RestfulLoginDisabledFilter;
import com.yirendai.oss.lib.security.internal.rest.RestfulLoginPublicKeyFilter;
import com.yirendai.oss.lib.security.internal.rest.RestfulLogoutDisabledFilter;
import com.yirendai.oss.lib.security.internal.rest.RestfulLogoutSuccessHandler;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;
import com.yirendai.oss.lib.webmvc.internal.DefaultHttpEntityMethodProcessor;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.Http401AuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.EncryptDefaultLoginPageConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Created by zhanghaolun on 16/8/19.
 */
@Order(FormAuthConfiguration.ORDER_FORM_AUTH)
@Configuration
public class FormAuthConfiguration extends SecurityConfigurerAdapter<FormAuthConfiguration> {

  public static final int ORDER_FORM_AUTH = BasicAuthConfiguration.ORDER_BASIC_AUTH - 10;

  public static final String LOGIN_CIPHER = "loginCipher";
  public static final String FORM_AUTHENTICATION_ENTRYPOINT = "formAuthenticationEntryPoint";

  @Autowired
  private AppProperties appProperties;

  @Autowired
  private Environment environment;

  @Autowired
  private RestfulExceptionHandler exceptionHandler;

  @Qualifier(RESOLVED_ERROR_COOKIE)
  @Autowired
  private TypeSafeCookie<ResolvedError> resolvedErrorCookie;

  @Qualifier(GENERIC_USER_COOKIE)
  @Autowired(required = false)
  private TypeSafeCookie<GenericUser> genericUserCookie;

  @Qualifier(GENERIC_USER_TOKEN)
  @Autowired(required = false)
  private TypeSafeToken<GenericUser> genericUserToken;

  @Autowired
  private DefaultHttpEntityMethodProcessor httpEntityMethodProcessor;

  @Autowired
  private ServerProperties serverProperties;

  @Autowired(required = false)
  @SuppressWarnings("rawtypes")
  private BaseUserDetailsAuthenticationProvider userDetailsAuthenticationProvider;

  @SneakyThrows
  @Override
  public void configure(final HttpSecurity http) {
    final AuthenticationEntryPoint formAuthenticationEntryPoint = this.formAuthenticationEntryPoint();
    if (formAuthenticationEntryPoint != null) {
      // TODO accessDeniedHandler
      http.exceptionHandling().authenticationEntryPoint(formAuthenticationEntryPoint);
    }
    // TODO RememberMeAuthenticationFilter

    final AppType appType = this.appProperties.getType();
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();
    if (appSecurityProperties.getEnabled() && appType != RESOURCE) {
      checkState(this.userDetailsAuthenticationProvider != null, //
        "must define a bean that extends " //
          + BaseUserDetailsAuthenticationProvider.class.getName() //
          + " to enable form authentication");

      final KeyExpression loginKey = appSecurityProperties.getLoginKey();
      final String publicKey;
      if (loginKey.isPresent()) {
        final Rsa rsa = Cryptos.cipher(loginKey);
        this.userDetailsAuthenticationProvider.setCipher(rsa);
        publicKey = RsaKey.extractPublicKey(rsa.getEncryptor().getKey().getKeyExpression());
      } else {
        publicKey = "";
      }

      // see: http://docs.spring.io/spring-security/site/docs/4.1.3.RELEASE/guides/html5/form-javaconfig.html
      // see: org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
      // see: org.springframework.security.config.annotation.web.configurers.DefaultLoginPageConfigurer
      // if loginPage or authenticationEntryPoint is set, there will not be loginPageGeneratingFilter
      final Boolean useDefaultLoginPage = appSecurityProperties.useDefaultLoginPage(this.environment);
      http.apply(new EncryptDefaultLoginPageConfigurer<>( //
        appSecurityProperties.getLoginPage(), //
        publicKey, //
        useDefaultLoginPage, //
        this.exceptionHandler.getExceptionResolver(), //
        this.resolvedErrorCookie //
      ));
      final FormLoginConfigurer<HttpSecurity> formLogin = http.formLogin();
      if (!useDefaultLoginPage) {
        formLogin.loginPage(appSecurityProperties.getLoginPage()); // this will disable default loginPage
        //.permitAll() // permitAll only works with HttpSecurity.authorizeRequests()
      }
      final String loginProcessingUrl = appSecurityProperties.getLoginProcessingUrl();
      final String logoutUrl = appSecurityProperties.getLogoutUrl();
      formLogin
        .loginProcessingUrl(loginProcessingUrl)
        .failureHandler(this.authenticationFailureHandler()) //
        .successHandler(this.authenticationSuccessHandler()) //
        .and() //
        .logout() //
        .logoutUrl(logoutUrl)//.permitAll() // permitAll only works with HttpSecurity.authorizeRequests()
        .addLogoutHandler(this.logoutHandler()) //
        .logoutSuccessHandler(this.logoutSuccessHandler());
    } else {
      http.formLogin().disable() //
        .logout().disable() //
      ;

      if (appType == MIXED || appType == RESTFUL) {
        http
          .addFilterBefore(this.loginDisabledFilter(), UsernamePasswordAuthenticationFilter.class) //
          .addFilterBefore(this.logoutDisabledFilter(), RestfulLoginDisabledFilter.class) //
        ;
      }
    }

    final RestfulLoginPublicKeyFilter restfulLoginPublicKeyFilter = this.restfulLoginPublicKeyFilter();
    if (restfulLoginPublicKeyFilter != null) {
      http.addFilterBefore(restfulLoginPublicKeyFilter, UsernamePasswordAuthenticationFilter.class);
    }
  }

  @Bean
  @ConditionalOnAppSecurity(ENABLED)
  @ConditionalOnAppType({MIXED, RESTFUL, TEMPLATE})
  public PreAuthTokenCookieClearingLogoutHandler logoutHandler() {
    return new PreAuthTokenCookieClearingLogoutHandler();
  }

  public LogoutSuccessHandler logoutSuccessHandler() {
    final LogoutSuccessHandler handler;
    if (this.appProperties.getType() == TEMPLATE) {
      handler = templateLogoutSuccessHandler();
    } else {
      handler = new RestfulLogoutSuccessHandler();
    }
    return handler;
  }

  public RestfulLoginPublicKeyFilter restfulLoginPublicKeyFilter() {
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();

    final RestfulLoginPublicKeyFilter filter;
    if (appSecurityProperties.getEnabled() && //
      OnAppTypeCondition.matches(this.appProperties.getType(), MIXED, RESTFUL) && //
      appSecurityProperties.getLoginKey().isPresent()) {
      final String loginPublicKeyUrl = appSecurityProperties.getLoginPublicKeyUrl();
      final KeyExpression publicKey = new RsaKey(appSecurityProperties.getLoginKey()).getPublicKey();
      filter = new RestfulLoginPublicKeyFilter(publicKey);
      filter.setEnvironment(this.environment);
      filter.setExceptionHandler(this.exceptionHandler);
      filter.setHttpEntityMethodProcessor(this.httpEntityMethodProcessor);
      filter.setRequestMatcher(new AntPathRequestMatcher(loginPublicKeyUrl, GET.name()));
    } else {
      filter = null;
    }
    return filter;
  }

  public RestfulLoginDisabledFilter loginDisabledFilter() {
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();

    final RestfulLoginDisabledFilter filter;
    if (!appSecurityProperties.getEnabled() && //
      OnAppTypeCondition.matches(this.appProperties.getType(), MIXED, RESTFUL)) {
      filter = new RestfulLoginDisabledFilter();
      filter.setEnvironment(this.environment);
      filter.setFilterProcessesUrl(appSecurityProperties.getLoginProcessingUrl());
      filter.setPostOnly(true);
      filter.setExceptionHandler(this.exceptionHandler);
    } else {
      filter = null;
    }
    return filter;
  }

  public RestfulLogoutDisabledFilter logoutDisabledFilter() {
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();

    final RestfulLogoutDisabledFilter filter;
    if (!appSecurityProperties.getEnabled() && //
      OnAppTypeCondition.matches(this.appProperties.getType(), MIXED, RESTFUL)) {

      filter = new RestfulLogoutDisabledFilter();
      filter.setEnvironment(this.environment);
      filter.setFilterProcessesUrl(appSecurityProperties.getLogoutUrl());
      filter.setExceptionHandler(this.exceptionHandler);
    } else {
      filter = null;
    }
    return filter;
  }

  @Bean
  @ConditionalOnAppSecurity(ENABLED)
  @ConditionalOnAppType({MIXED, RESTFUL, TEMPLATE})
  public AuthenticationFailureHandler authenticationFailureHandler() {
    final AppType appType = this.appProperties.getType();
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();
    final String authFailureHandler = appSecurityProperties.getAuthFailureHandler();
    final String loginPage = appSecurityProperties.getLoginPage();

    final AuthenticationFailureHandler failureHandler;
    if (isBlank(authFailureHandler)) {
      if (appType == TEMPLATE) {
        failureHandler = templateFailureHandler( //
          loginPage, this.exceptionHandler.getExceptionResolver(), this.resolvedErrorCookie);
      } else {
        failureHandler = RestfulAuthenticationFailureHandler.restfulFailureHandler(this.exceptionHandler);
      }
    } else {
      if ("restful".equalsIgnoreCase(authFailureHandler)) {
        failureHandler = RestfulAuthenticationFailureHandler.restfulFailureHandler(this.exceptionHandler);
      } else {
        failureHandler = templateFailureHandler( //
          loginPage, this.exceptionHandler.getExceptionResolver(), this.resolvedErrorCookie);
      }
    }

    return failureHandler;
  }

  @Bean
  @ConditionalOnAppSecurity(ENABLED)
  @ConditionalOnAppType({MIXED, RESTFUL, TEMPLATE})
  public AuthenticationSuccessHandler authenticationSuccessHandler() {
    final AppType appType = this.appProperties.getType();
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();
    final String authSucessHandler = appSecurityProperties.getAuthSucessHandler();

    final AuthenticationSuccessHandler successHandler;
    if (isBlank(authSucessHandler)) {
      if (appType == TEMPLATE) {
        successHandler = templateSuccessHandler("/"); // TODO redirectUrl
      } else {
        successHandler = RestfulAuthenticationSuccessHandler.restfulSuccessHandler( //
          this.genericUserCookie, //
          this.exceptionHandler, //
          this.httpEntityMethodProcessor, //
          this.genericUserToken //
        );
      }
    } else {
      if ("restful".equalsIgnoreCase(authSucessHandler)) {
        successHandler = RestfulAuthenticationSuccessHandler.restfulSuccessHandler( //
          this.genericUserCookie, //
          this.exceptionHandler, //
          this.httpEntityMethodProcessor, //
          this.genericUserToken //
        );
      } else {
        successHandler = templateSuccessHandler(authSucessHandler);
      }
    }


    return successHandler;
  }

  /**
   * always enabled.
   *
   * @return authenticationEntryPoint
   */
  @Bean(name = FORM_AUTHENTICATION_ENTRYPOINT)
  public AuthenticationEntryPoint formAuthenticationEntryPoint() {
    final AppType appType = this.appProperties.getType();
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();
    final String authEntryPoint = appSecurityProperties.getAuthEntryPoint();
    final String loginPage = appSecurityProperties.getLoginPage();

    final AuthenticationEntryPoint entryPoint;
    if (isBlank(authEntryPoint)) {
      if (appType == TEMPLATE) {
        final Boolean useDefaultLoginPage = appSecurityProperties.useDefaultLoginPage(this.environment);
        if (useDefaultLoginPage) {
          entryPoint = null;
        } else {
          // this will disable default loginPage
          entryPoint = templateEntryPoint( //
            loginPage, this.exceptionHandler.getExceptionResolver(), this.resolvedErrorCookie);
        }
      } else {
        entryPoint = restfulEntryPoint(this.exceptionHandler);
      }
    } else {
      if ("401".equals(authEntryPoint)) {
        final String sessionCookieName = this.serverProperties.getSession().getCookie().getName();
        final String headerValue = "Session realm=\"" + //
          (isNotBlank(sessionCookieName) ? sessionCookieName : "JSESSIONID") + // TODO is this realm ok?
          "\"";
        entryPoint = new Http401AuthenticationEntryPoint(headerValue);
      } else if ("403".equals(authEntryPoint)) {
        entryPoint = new Http403ForbiddenEntryPoint();
      } else if ("loginPage".equalsIgnoreCase(authEntryPoint)) {
        entryPoint = templateEntryPoint( //
          loginPage, this.exceptionHandler.getExceptionResolver(), this.resolvedErrorCookie);
      } else {
        entryPoint = restfulEntryPoint(this.exceptionHandler);
      }
    }

    return entryPoint;
  }

  /**
   * always enabled.
   *
   * @return passwordEncoder
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    //return NoOpPasswordEncoder.getInstance();
    return new ReentrantBCryptPasswordEncoder();
  }
}
