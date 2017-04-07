package com.yirendai.oss.lib.adminclient.starter;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.config.client.ConfigServerHealthIndicator;
import org.springframework.cloud.config.client.ConfigServicePropertySourceLocator;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Created by Jin Yuliang on 16/11/17.
 * This component is a fix to issue: https://github.com/spring-cloud/spring-cloud-config/issues/182
 */
@Component
@ConditionalOnClass({HealthIndicator.class, ConfigServicePropertySourceLocator.class})
@ConditionalOnProperty(value = "health.config.enabled", matchIfMissing = true)
@Slf4j
public class ConfigServerPostProcessor implements BeanPostProcessor {

  @Autowired
  private Environment environment;

  @Override
  public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
    if (ConfigServerHealthIndicator.class.isAssignableFrom(bean.getClass())) {
      log.info("Config Server Bean: {} was processed.", beanName);

      final Boolean env = setField(bean, ConfigServerHealthIndicator.class, "env", this.environment);
      final Boolean environment = setField(bean, ConfigServerHealthIndicator.class, "environment", this.environment);
      if (!env && !environment) {
        log.warn("env and environment not found in ConfigServerHealthIndicator.class");
      }
    }
    return bean;
  }

  static Boolean setField(final Object object, final Class<?> objectType, final String field, final Object value) {
    final Field fieldFound = ReflectionUtils.findField(objectType, field);
    final Boolean result;
    if (fieldFound != null) {
      fieldFound.setAccessible(true);
      ReflectionUtils.setField(fieldFound, object, value);
      result = Boolean.TRUE;
    } else {
      result = Boolean.FALSE;
    }
    return result;
  }
}
