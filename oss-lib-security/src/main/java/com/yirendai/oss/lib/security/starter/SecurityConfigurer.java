package com.yirendai.oss.lib.security.starter;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.Optional;

/**
 * Created by zhanghaolun on 16/8/30.
 */
public interface SecurityConfigurer<T extends SecurityConfigurer> extends Ordered, Comparable<T> {

  Comparator<SecurityConfigurer> COMPARATOR = (lhs, rhs) -> {
    final int result;
    if (lhs != null && rhs != null) {
      final int lhsOrder = lhs.getOrder();
      final int rhsOrder = rhs.getOrder();
      result = Ints.compare(lhsOrder, rhsOrder);
    } else if (lhs != null) {
      result = -1;
    } else {
      result = 1;
    }
    return result;
  };

  @Override
  default int compareTo(final T rhs) {
    return COMPARATOR.compare(this, rhs);
  }

  /**
   * second invoked.
   *
   * @param auth authentication manager builder
   */
  void configure(AuthenticationManagerBuilder auth);

  /**
   * last invoked.
   *
   * @param http http security builder
   */
  void configure(HttpSecurity http);

  default int getOrder() {
    final Type superClass = this.getClass().getGenericSuperclass();
    final Class<?> clazz;
    if (superClass instanceof Class<?>) { // sanity check, should never happen
      clazz = (Class<?>) superClass;
    } else {
      final Type type = ((ParameterizedType) superClass).getActualTypeArguments()[0];

      final JavaType javaType = TypeFactory.defaultInstance().constructType(type);
      clazz = javaType.getRawClass();
    }


    final Annotation[] annotations = clazz.getAnnotations();
    final Optional<Annotation> orderAnnotation = Lists.newArrayList(annotations)
      .stream()
      .filter(annotation -> annotation.annotationType() == Order.class)
      .findFirst();

    final int result;
    if (orderAnnotation.isPresent()) {
      final Order order = (Order) orderAnnotation.get();
      result = order.value();
    } else {
      result = LOWEST_PRECEDENCE;
    }
    return result;
  }

  /**
   * first invoked.
   *
   * @param builder web security builder
   */
  void init(WebSecurity builder);
}
