package com.yirendai.oss.lib.webmvc.api;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirendai.oss.lib.common.Jackson2Utils;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * Created by zhanghaolun on 16/10/29.
 */
@Slf4j
public class JsonToken<T> implements TypeSafeToken<T> {

  private final Function<String, T> fromToken;
  private final Function<T, String> toToken;

  public JsonToken( //
    final Class<T> type, //
    final ObjectMapper objectMapper //
  ) {
    this.toToken = Jackson2Utils.toJson(objectMapper);
    this.fromToken = Jackson2Utils.fromJson(objectMapper, type);
  }

  @Override
  public String toToken(final T obj) {
    return this.toToken != null && obj != null ? //
      this.toToken.apply(obj) : //
      (obj != null) ? obj.toString() : null;
  }

  @Override
  public T fromToken(final String token) {
    T result;
    try {
      result = isNotBlank(token) ? this.fromToken.apply(token) : null;
    } catch (final Exception ex) {
      log.warn("error reading token: {}", token, ex);
      result = null;
    }
    return result;
  }
}
