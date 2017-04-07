package com.yirendai.oss.lib.histrix.config;

import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;

import lombok.extern.slf4j.Slf4j;

import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 监听配置变化，判断是否hystrix属性，是则刷新hystrix属性.
 * 2016/12/8 yanzhang153
 */
@Slf4j
@Component
public class HystrixApplicationListener implements ApplicationListener<EnvironmentChangeEvent> {
  @Override
  public void onApplicationEvent(EnvironmentChangeEvent event) {
    for (String key : event.getKeys()) {
      if (key.startsWith("hystrix")) {
        HystrixPropertiesFactory.reset(); // 清理当前默认配置,下次请求会自动加载最新配置
        log.info("hystrix properties has changed, event:{}", event.toString());
        break;
      }
    }
  }
}
