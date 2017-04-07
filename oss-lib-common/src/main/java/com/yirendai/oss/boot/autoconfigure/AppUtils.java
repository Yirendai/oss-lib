package com.yirendai.oss.boot.autoconfigure;

import static java.util.stream.Collectors.toList;

import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by zhanghaolun on 16/10/29.
 */
public abstract class AppUtils {

  private AppUtils() {
  }

  public static void printApplicationInfo(final ApplicationContext ctx) {
    System.out.println("Current environment is: " + ctx.getBean(AppProperties.class).getEnv());
    System.out.println("Let's inspect the beans provided by Spring Boot:");

    final String[] beanNames = ctx.getBeanDefinitionNames();
    for (final String beanName : Arrays.stream(beanNames).sorted().collect(toList())) {
      final Object bean = ctx.getBean(beanName);
      final Class<?> clazz = bean != null ? bean.getClass() : null;
      final String clazzName = clazz != null ? clazz.getName() : "null";
      System.out.println(beanName + " - " + clazzName);
    }
  }

  public static String appBasePackage(final String defaultValue) {
    final Map.Entry<String, String> javaMainClass = System.getenv().entrySet() //
      .stream() //
      .filter(entry -> entry.getKey().startsWith("JAVA_MAIN_CLASS")) //
      .findFirst() //
      .orElse(null);

    final String sunJavaCommand = System.getProperty("sun.java.command");

    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    final String result;
    if (javaMainClass != null && ClassUtils.isPresent(javaMainClass.getValue(), classLoader)) {
      result = ClassUtils.getPackageName(javaMainClass.getValue());
    } else if (ClassUtils.isPresent(sunJavaCommand, classLoader)) {
      result = ClassUtils.getPackageName(sunJavaCommand);
    } else {
      result = defaultValue;
    }
    return result;
  }
}
