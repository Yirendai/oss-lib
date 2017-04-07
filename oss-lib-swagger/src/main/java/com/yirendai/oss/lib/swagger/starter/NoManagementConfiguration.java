package com.yirendai.oss.lib.swagger.starter;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by zhanghaolun on 16/11/18.
 */
@ConditionalOnMissingClass({"org.springframework.boot.actuate.autoconfigure.ManagementServerProperties"})
@Configuration
public class NoManagementConfiguration {

  public static final String MANAGEMENT_PATHS = "MANAGEMENT_PATHS";

  @Bean(name = MANAGEMENT_PATHS)
  public Predicate<String> managementPaths() {
    return Predicates.alwaysFalse();
  }
}
