package com.yirendai.oss.lib.webmvc.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by Meifans on 17/1/9.
 */
@ConditionalOnClass(name = "com.google.gson.Gson")
@Configuration
public class GsonConfiguration {

  @Autowired(required = false)
  @Bean
  @ConditionalOnBean(type = {"com.google.gson.GsonBuilder"})
  public com.google.gson.Gson gson(final com.google.gson.GsonBuilder builder) {
    return com.yirendai.oss.lib.common.GsonUtils.setup(builder);
  }
}
