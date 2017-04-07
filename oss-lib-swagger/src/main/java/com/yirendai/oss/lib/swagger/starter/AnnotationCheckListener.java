package com.yirendai.oss.lib.swagger.starter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationConfigurationException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;

/**
 * Created by leo on 16/12/14.
 */
@Component
@ConditionalOnClass(FeignClient.class)
public class AnnotationCheckListener implements ApplicationListener<ContextRefreshedEvent> {
  @Autowired
  public ApplicationContext applicationContext;

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {

    String[] beanNamesForAnnotation = applicationContext.getBeanNamesForAnnotation(RequestMapping.class);

    Arrays.stream(beanNamesForAnnotation)
        .filter(beanName -> applicationContext.findAnnotationOnBean(beanName, FeignClient.class) != null)
        .forEach(beanName -> throwAnnotationConfigurationException(beanName));
  }

  static void throwAnnotationConfigurationException(String beanName) {
    throw new AnnotationConfigurationException("Cannot have both @RequestMapping and @FeignClient on " + beanName);
  }
}
