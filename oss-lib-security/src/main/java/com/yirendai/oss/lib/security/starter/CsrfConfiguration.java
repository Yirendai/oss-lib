package com.yirendai.oss.lib.security.starter;

import com.yirendai.oss.lib.security.CsrfHeaderFilter;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * Created by zhanghaolun on 16/8/19.
 */
@Order(CsrfConfiguration.ORDER_CSRF)
@Configuration
public class CsrfConfiguration extends SecurityConfigurerAdapter<CsrfConfiguration> {

  public static final int ORDER_CSRF = VerifyCodeConfiguration.ORDER_VERIFY_CODE + 1;

  @Autowired(required = false)
  private SecurityProperties securityProperties;

  @SneakyThrows
  @Override
  public void configure(final HttpSecurity http) {
    // TODO CSRF的 cookie, header, 各种前端全支持
    // RESOURCE 应用不开启CSRF
    if (this.securityProperties != null && this.securityProperties.isEnableCsrf()) {
      http.csrf() //
        .csrfTokenRepository(this.csrfTokenRepository()) //
        .and() //
        .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class); //
    } else {
      http.csrf() //
        .disable();
    }

    if (this.securityProperties != null && this.securityProperties.getHeaders().isXss()) {
      http.headers().xssProtection().xssProtectionEnabled(true);
    } else {
      http.headers().xssProtection().disable();
    }
  }

  private CsrfTokenRepository csrfTokenRepository() {
    // TODO test cookie token repository not session
    // cookie csrf token is less secure than http session csrf token, but it is stateless.
    // http only prevent javascript access the cookie, it is more secure.
    //final HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
    final CookieCsrfTokenRepository repository = new CookieCsrfTokenRepository(); // since 4.1.0
    repository.setCookieHttpOnly(true);
    repository.setHeaderName("X-XSRF-TOKEN");
    repository.setParameterName("_csrf");
    return repository;
  }
}
