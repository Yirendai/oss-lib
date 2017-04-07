package com.yirendai.oss.lib.test;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

/**
 * Created by zhanghaolun on 16/10/6.
 */
public interface RestTemplateForTest extends RestOperations {

  static <T> HttpEntity<T> newHttpEntity( //
    final MediaType contentType, //
    final MediaType accpet, //
    final T param //
  ) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType);
    headers.add("Accept", accpet.toString());
    final HttpEntity<T> ret = new HttpEntity<>(param, headers);
    System.out.println(ret);
    return ret;
  }

  RestTemplate getRestTemplate();

  void setUriTemplateHandler(UriTemplateHandler handler);

  default <T, P> ResponseEntity<T> exchange( //
    final String url, //
    final HttpMethod method, //
    final MediaType contentType, //
    final MediaType accpet, //
    final Class<T> type, //
    final P param //
  ) {
    return getRestTemplate().exchange(url, method, newHttpEntity(contentType, accpet, param), type);
  }
}
