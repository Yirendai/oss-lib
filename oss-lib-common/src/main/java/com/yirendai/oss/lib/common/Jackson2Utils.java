package com.yirendai.oss.lib.common;

import static java.util.stream.Collectors.toSet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.PropertyResolver;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

/**
 * Created by zhanghaolun on 16/6/9.
 */
@Slf4j
public final class Jackson2Utils {

  private Jackson2Utils() {
  }

  private static final Boolean JACKSON2_PRESENT = ClassUtils.isPresent( //
    "com.fasterxml.jackson.databind.ObjectMapper", //
    Thread.currentThread().getContextClassLoader() //
  ) && ClassUtils.isPresent( //
    "com.fasterxml.jackson.core.JsonGenerator", //
    Thread.currentThread().getContextClassLoader() //
  );

  public static class RuntimeJsonProcessingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RuntimeJsonProcessingException(final String message, final Throwable cause) {
      super(message, cause);
    }
  }

  public static Boolean getJackson2Present() {
    return Jackson2Utils.JACKSON2_PRESENT;
  }

  @SuppressWarnings("rawtypes")
  static Set<Jackson2Configurator> scanJackson2Configurators() {
    final String basePackage = Jackson2Utils.class.getName().split("\\.")[0];
    final Set<Class<Jackson2Configurator>> configurators = FileAndClasspathUtils.scan(basePackage,
      new FileAndClasspathUtils.AssignableFilter(Jackson2Configurator.class, false, true));

    if (configurators.isEmpty()) {
      log.warn("no {} found.", Jackson2Configurator.class.getSimpleName());
    }

    return configurators.stream() //
      .flatMap(configurator -> Arrays.stream(configurator.getEnumConstants())) //
      .collect(toSet());
  }

  /**
   * Setup a Mapper.
   *
   * @param propertyResolver propertyResolver
   * @param objectMapper     to setup
   * @param <T>              ObjectMapper or XmlMapper
   * @return same instance of objectMapper param
   */
  @SuppressWarnings("rawtypes")
  public static <T extends ObjectMapper> T setupObjectMapper( //
    final PropertyResolver propertyResolver, final T objectMapper) {
    for (final Jackson2Configurator instance : scanJackson2Configurators()) {
      log.info("config objectMapper: '{}' using: '{}'.", objectMapper, instance);
      instance.config(propertyResolver, objectMapper);
    }
    return objectMapper;
  }

  public static <T> Function<String, T> fromJson( //
    final ObjectMapper objectMapper, //
    final TypeReference<T> typeReference //
  ) {
    return string -> {
      try {
        return objectMapper.readValue(string, typeReference);
      } catch (final IOException wrapped) {
        throw new RuntimeJsonProcessingException("error read from JSON.", wrapped);
      }
    };
  }

  /**
   * parse JSON.
   *
   * @param objectMapper objectMapper
   * @param type         type
   * @param <T>          type
   * @return object
   */
  public static <T> Function<String, T> fromJson( //
    final ObjectMapper objectMapper, //
    final Class<T> type //
  ) {
    return string -> {
      try {
        return objectMapper.readValue(string, type);
      } catch (final IOException wrapped) {
        throw new RuntimeJsonProcessingException("error read from JSON.", wrapped);
      }
    };
  }

  /**
   * to JSON.
   *
   * @param objectMapper objectMapper
   * @param <T>          type
   * @return JSON
   */
  public static <T> Function<T, String> toJson(final ObjectMapper objectMapper) {
    return object -> {
      try {
        if (object != null) {
          return objectMapper.writeValueAsString(object);
        } else {
          return "";
        }
      } catch (final JsonProcessingException wrapped) {
        throw new RuntimeJsonProcessingException("error serialize to JSON.", wrapped);
      }
    };
  }

  public static <T> String toJson(final ObjectMapper objectMapper, final T item) {
    return toJson(objectMapper).apply(item);
  }
}
