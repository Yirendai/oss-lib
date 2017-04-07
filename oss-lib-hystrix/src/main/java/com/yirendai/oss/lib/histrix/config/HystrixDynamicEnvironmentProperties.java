package com.yirendai.oss.lib.histrix.config;

import com.netflix.hystrix.strategy.properties.HystrixDynamicProperties;
import com.netflix.hystrix.strategy.properties.HystrixDynamicProperty;

import org.springframework.core.env.Environment;


/**
 * 使用spring Environment配置进行全局配置
 * Created by yanzhang153 on 2016/12/5.
 */
public class HystrixDynamicEnvironmentProperties implements HystrixDynamicProperties {
  private Environment environment;

  public HystrixDynamicEnvironmentProperties() {
    environment = SpringUtils.getBean(Environment.class);
  }

  @Override
  public HystrixDynamicProperty<String> getString(String name, String fallback) {
    return new HystrixProperty.StringProperty(environment, name, fallback);
  }

  @Override
  public HystrixDynamicProperty<Integer> getInteger(String name, Integer fallback) {
    return new HystrixProperty.IntegerProperty(environment, name, fallback);
  }

  @Override
  public HystrixDynamicProperty<Long> getLong(String name, Long fallback) {
    return new HystrixProperty.LongProperty(environment, name, fallback);
  }

  @Override
  public HystrixDynamicProperty<Boolean> getBoolean(String name, Boolean fallback) {
    return new HystrixProperty.BooleanProperty(environment, name, fallback);
  }
}
