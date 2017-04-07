package com.yirendai.oss.lib.common;

import static java.nio.charset.StandardCharsets.UTF_8;

import lombok.SneakyThrows;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

/**
 * Created by zhanghaolun on 16/7/9.
 */
public final class ResourceUtils {

  private ResourceUtils() {
  }

  @SneakyThrows
  public static String classPathString(final String file) {
    return IOUtils.toString(classPathBytes(file), UTF_8.name());
  }

  @SneakyThrows
  public static byte[] classPathBytes(final String file) {
    return IOUtils.toByteArray(
      new ClassPathResource(file, Thread.currentThread().getContextClassLoader()).getInputStream());
  }
}
