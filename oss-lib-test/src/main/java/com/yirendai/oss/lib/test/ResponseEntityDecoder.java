package com.yirendai.oss.lib.test;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Decoder adds compatibility for Spring MVC's ResponseEntity to any other decoder via composition.
 *
 * @author chadjaros
 */
public class ResponseEntityDecoder implements Decoder {

  private final Decoder decoder;

  public ResponseEntityDecoder(final Decoder decoder) {
    this.decoder = decoder;
  }

  @Override
  public Object decode(final Response response, final Type type) throws IOException, FeignException {
    final Object ret;
    if (isParameterizeHttpEntity(type)) {
      final Type type_ = ((ParameterizedType) type).getActualTypeArguments()[0];
      final Object decodedObject = decoder.decode(response, type_);
      ret = createResponse(decodedObject, response);
    } else if (isHttpEntity(type)) {
      ret = createResponse(null, response);
    } else {
      ret = decoder.decode(response, type);
    }
    return ret;
  }

  private boolean isParameterizeHttpEntity(final Type type) {
    boolean ret = false;
    if (type instanceof ParameterizedType) {
      ret = isHttpEntity(((ParameterizedType) type).getRawType());
    }
    return ret;
  }

  @SuppressWarnings("rawtypes")
  private boolean isHttpEntity(final Type type) {
    boolean ret = false;
    if (type instanceof Class) {
      final Class c = (Class) type;
      ret = HttpEntity.class.isAssignableFrom(c);
    }
    return ret;
  }

  @SuppressWarnings({"unchecked"})
  private <T> ResponseEntity<T> createResponse(final Object instance, final Response response) {

    final MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
    for (final String key : response.headers().keySet()) {
      headers.put(key, createLintedList(response.headers().get(key)));
    }

    return new ResponseEntity<>((T) instance, headers, HttpStatus.valueOf(response.status()));
  }

  <T> List<T> createLintedList(final Collection<T> type) {
    return new LinkedList<>(type);
  }
}
