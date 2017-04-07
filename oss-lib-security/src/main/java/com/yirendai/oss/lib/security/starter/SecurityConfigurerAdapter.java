package com.yirendai.oss.lib.security.starter;

import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

/**
 * Created by zhanghaolun on 16/8/30.
 */
public abstract class SecurityConfigurerAdapter<T extends SecurityConfigurerAdapter> implements SecurityConfigurer<T> {

  @Override
  public void init(final WebSecurity builder) {
  }

  @Override
  public void configure(final AuthenticationManagerBuilder auth) {
  }

  @Override
  public void configure(final HttpSecurity http) {
  }
}
