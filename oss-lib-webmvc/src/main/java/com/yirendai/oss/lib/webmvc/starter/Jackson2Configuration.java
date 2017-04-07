package com.yirendai.oss.lib.webmvc.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by Meifans on 17/1/9.
 */
@ConditionalOnClass(name = "com.fasterxml.jackson.databind.ObjectMapper")
@Configuration
public class Jackson2Configuration {

  @Autowired(required = false)
  private Environment environment;

  @Autowired(required = false)
  public void setObjectMapper(final com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
    com.yirendai.oss.lib.common.Jackson2Utils.setupObjectMapper(this.environment, objectMapper);
  }
}
