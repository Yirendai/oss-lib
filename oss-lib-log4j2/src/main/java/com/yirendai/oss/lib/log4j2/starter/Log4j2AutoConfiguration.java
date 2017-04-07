package com.yirendai.oss.lib.log4j2.starter;

import com.yirendai.oss.lib.log4j2.jmx.Log4j2Manager;

import org.apache.logging.log4j.core.LoggerContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Log4j2AutoConfiguration {

  @Bean
  @ConditionalOnClass(LoggerContext.class)
  @ConditionalOnMissingBean
  public Log4j2Manager log4j2Manager() {
    return new Log4j2Manager();
  }

}
