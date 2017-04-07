package com.yirendai.oss.lib.histrix.config;

import com.netflix.hystrix.strategy.properties.HystrixDynamicProperty;

import org.springframework.core.env.Environment;

import java.lang.reflect.ParameterizedType;

/**
 * Created by yanzhang153 on 2016/12/7.
 */
public class HystrixProperty<T> implements HystrixDynamicProperty<T> {
  private String name;
  private T fallback;
  private Environment environment;
  private Class<T> realClass;

  public HystrixProperty(Environment environment, String name, T fallback) {
    this.environment = environment;
    this.name = name;
    this.fallback = fallback;
    realClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void addCallback(Runnable callback) {
    // 属性变更后回调,可以不实现
  }

  @Override
  public T get() {
    return environment.getProperty(name, realClass, fallback);
  }


  protected static class BooleanProperty extends HystrixProperty<Boolean> {
    public BooleanProperty(Environment environment, String name, Boolean fallback) {
      super(environment, name, fallback);
    }
  }

  protected static class StringProperty extends HystrixProperty<String> {
    public StringProperty(Environment environment, String name, String fallback) {
      super(environment, name, fallback);
    }
  }

  protected static class IntegerProperty extends HystrixProperty<Integer> {
    public IntegerProperty(Environment environment, String name, Integer fallback) {
      super(environment, name, fallback);
    }
  }


  protected static class LongProperty extends HystrixProperty<Long> {
    public LongProperty(Environment environment, String name, Long fallback) {
      super(environment, name, fallback);
    }
  }

}
