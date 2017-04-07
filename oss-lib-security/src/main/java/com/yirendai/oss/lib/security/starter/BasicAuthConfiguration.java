package com.yirendai.oss.lib.security.starter;

import static com.yirendai.oss.boot.autoconfigure.AppType.MIXED;
import static com.yirendai.oss.boot.autoconfigure.AppType.RESOURCE;
import static com.yirendai.oss.boot.autoconfigure.AppType.RESTFUL;
import static com.yirendai.oss.boot.autoconfigure.AppType.TEMPLATE;
import static com.yirendai.oss.lib.security.internal.rest.RestfulBasicAuthenticationEntryPoint.DEFAULT_REALM_NAME;
import static org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER;

import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.boot.autoconfigure.AppSecurity;
import com.yirendai.oss.boot.autoconfigure.ConditionalOnAppSecurity;
import com.yirendai.oss.boot.autoconfigure.ConditionalOnAppType;
import com.yirendai.oss.lib.errorhandle.internal.RestfulExceptionHandler;
import com.yirendai.oss.lib.security.internal.rest.RestfulBasicAuthenticationEntryPoint;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Created by zhanghaolun on 16/8/19.
 */
@Configuration
@Order(BasicAuthConfiguration.ORDER_BASIC_AUTH)
public class BasicAuthConfiguration extends SecurityConfigurerAdapter<BasicAuthConfiguration> {

  public static final int ORDER_BASIC_AUTH = BASIC_AUTH_ORDER + 1;

  public static final String BASIC_AUTHENTICATION_ENTRYPOINT = "basicAuthenticationEntryPoint";

  @Autowired
  private AppProperties appProperties;

  @Autowired
  private Environment environment;

  @Autowired
  private RestfulExceptionHandler exceptionHandler;

  @SneakyThrows
  @Override
  public void configure(final HttpSecurity http) {
    if (this.appProperties.getSecurity().getEnabled() && this.appProperties.getType() != RESOURCE) {
      http.httpBasic() //
        .authenticationEntryPoint(this.basicAuthenticationEntryPoint(this.environment));
    } else {
      http.httpBasic() //
        .disable();
    }
  }

  @Bean(name = BASIC_AUTHENTICATION_ENTRYPOINT)
  @ConditionalOnAppSecurity(AppSecurity.ENABLED)
  @ConditionalOnAppType({MIXED, RESTFUL, TEMPLATE})
  public AuthenticationEntryPoint basicAuthenticationEntryPoint(final Environment environment) {
    final String realmName = environment.getProperty("security.basic.realm", DEFAULT_REALM_NAME);
    final RestfulBasicAuthenticationEntryPoint entryPoint = new RestfulBasicAuthenticationEntryPoint();
    entryPoint.setExceptionHandler(this.exceptionHandler);
    entryPoint.setRealmName(realmName);
    return entryPoint;
  }
}
