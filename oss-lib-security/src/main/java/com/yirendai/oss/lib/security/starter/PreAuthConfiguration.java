package com.yirendai.oss.lib.security.starter;

import static com.yirendai.oss.boot.autoconfigure.AppSecurity.ENABLED;
import static com.yirendai.oss.boot.autoconfigure.AppSecurityProperties.APP_SECURITY;
import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_COOKIE;
import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_TOKEN;
import static com.yirendai.oss.lib.security.starter.PermitedRequestConfiguration.PERMITED_REQUEST_MATCHER;
import static java.lang.Boolean.FALSE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.boot.autoconfigure.AppSecurityProperties;
import com.yirendai.oss.boot.autoconfigure.ConditionalOnAppSecurity;
import com.yirendai.oss.lib.common.crypto.Cryptos;
import com.yirendai.oss.lib.common.crypto.EncodeCipher;
import com.yirendai.oss.lib.common.crypto.Jwt;
import com.yirendai.oss.lib.security.api.BaseUserDetailsAuthenticationProvider;
import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.User;
import com.yirendai.oss.lib.security.internal.preauth.PreAuthTestUserFilter;
import com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenAuthenticationProvider;
import com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenFilter;
import com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenProcessingFilter;
import com.yirendai.oss.lib.webmvc.api.DomainResolver;
import com.yirendai.oss.lib.webmvc.api.JsonToken;
import com.yirendai.oss.lib.webmvc.api.JsonWebToken;
import com.yirendai.oss.lib.webmvc.api.RequestResolver;
import com.yirendai.oss.lib.webmvc.api.SecureToken;
import com.yirendai.oss.lib.webmvc.api.TokenBasedCookie;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;
import com.yirendai.oss.lib.webmvc.api.UrlEncodedToken;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhanghaolun on 16/8/19.
 */
@Order(PreAuthConfiguration.ORDER_PRE_AUTH)
@Configuration
@ConfigurationProperties(prefix = APP_SECURITY)
@Slf4j
public class PreAuthConfiguration extends SecurityConfigurerAdapter<PreAuthConfiguration> {

  public static final int ORDER_PRE_AUTH = BasicAuthConfiguration.ORDER_BASIC_AUTH + 1;

  static final String PRE_AUTH_AUTHENTICATION_PROVIDER = "preAuthAuthenticationProvider";

  @Autowired
  private AppProperties appProperties;
  @Autowired
  private DomainResolver domainResolver;
  @Autowired
  private Environment environment;
  @Autowired
  private ObjectMapper objectMapper;
  @Qualifier(PERMITED_REQUEST_MATCHER)
  @Autowired(required = false)
  private RequestMatcher permitedRequestMatcher;
  @Autowired
  private RequestResolver requestResolver;
  @Autowired
  private ServerProperties serverProperties;

  @Autowired(required = false)
  @SuppressWarnings("rawtypes")
  private BaseUserDetailsAuthenticationProvider userDetailsAuthenticationProvider;

  public static int getMaxAge(final ServerProperties serverProperties) {
    final Integer maxAge = serverProperties.getSession().getCookie().getMaxAge();
    final int defaultMaxAge = (int) TimeUnit.DAYS.toSeconds(1L);
    return maxAge != null ? maxAge : defaultMaxAge;
  }

  @Override
  public void configure(final HttpSecurity http) {
    if (this.appProperties.getSecurityEnabled()) {
      final PreAuthTokenFilter preAuthTokenFilter = this.preAuthTokenFilter();
      http.addFilterBefore(preAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);

      if (this.appProperties.getSecurityUseTestUser()) {
        http.addFilterAfter(this.preAuthTestUserFilter(), BasicAuthenticationFilter.class);
        http.addFilterAfter(this.preAuthTokenProcessingFilter(), PreAuthTestUserFilter.class);
      } else {
        http.addFilterAfter(this.preAuthTokenProcessingFilter(), BasicAuthenticationFilter.class);
      }
    }
  }

  @Bean(name = GENERIC_USER_COOKIE)
  public TypeSafeCookie<GenericUser> genericUserCookie() {
    // server.session.cookie.comment= # Comment for the session cookie.
    // server.session.cookie.domain= # Domain for the session cookie.
    // server.session.cookie.http-only= # "HttpOnly" flag for the session cookie.
    // server.session.cookie.max-age= # Maximum age of the session cookie in seconds.
    // server.session.cookie.name= # Session cookie name.
    // server.session.cookie.path= # Path of the session cookie.
    // server.session.cookie.secure= # "Secure" flag for the session cookie.

    final TypeSafeToken<GenericUser> token = this.genericUserToken();
    return new TokenBasedCookie<>( //
      this.domainResolver, //
      true, //
      getMaxAge(this.serverProperties), //
      "generic_user", //
      false, //
      token //
    );
  }

  @Bean(name = GENERIC_USER_TOKEN)
  public TypeSafeToken<GenericUser> genericUserToken() {
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();

    TypeSafeToken<GenericUser> token = new JsonToken<>(GenericUser.class, this.objectMapper);

    final Jwt jwtCipher = Cryptos.cipher(appSecurityProperties.getJwtKey());
    if (jwtCipher != null) {
      token = new JsonWebToken<>(token, jwtCipher, getMaxAge(this.serverProperties));
    } else {
      log.warn("INSECURE ! JwtKey not set. Using plain text token.");
    }

    final EncodeCipher cookieCipher = Cryptos.cipher(appSecurityProperties.getCookieKey());
    if (cookieCipher != null) {
      token = new SecureToken<>(token, cookieCipher);
    }

    token = new UrlEncodedToken<>(token);
    return token;
  }

  public PreAuthTokenFilter preAuthTokenFilter() {
    final PreAuthTokenFilter filter = new PreAuthTokenFilter();
    filter.setCookie(this.genericUserCookie());
    filter.setEnvironment(this.environment);
    filter.setPermitedRequestMatcher(this.permitedRequestMatcher);
    filter.setToken(this.genericUserToken());
    return filter;
  }

  public PreAuthTokenProcessingFilter preAuthTokenProcessingFilter() {
    final PreAuthTokenProcessingFilter filter = new PreAuthTokenProcessingFilter();
    filter.setEnvironment(this.environment);
    return filter;
  }

  @SuppressWarnings("unchecked")
  public PreAuthTestUserFilter preAuthTestUserFilter() {
    final PreAuthTestUserFilter filter;
    if (this.appProperties.getSecurityUseTestUser()) {
      final String defaultTestUser = this.appProperties.getSecurityDefaultTestUser();
      final List<User> testUsers = this.userDetailsAuthenticationProvider.initTestUsers();
      filter = new PreAuthTestUserFilter(defaultTestUser, testUsers);
      filter.setEnvironment(this.environment);
      filter.setToken(this.genericUserToken());
    } else {
      this.userDetailsAuthenticationProvider.deleteTestUsers();
      filter = null;
    }
    return filter;
  }

  @Bean(name = PRE_AUTH_AUTHENTICATION_PROVIDER)
  @ConditionalOnAppSecurity(ENABLED)
  public AuthenticationProvider preAuthAuthenticationProvider() {
    // this.appProperties.getSecurityUseTestUser() ? new NoOpPreAuthenticatedAuthenticationProvider()
    return new PreAuthTokenAuthenticationProvider(FALSE);
  }
}
