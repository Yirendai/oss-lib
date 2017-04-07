package com.yirendai.oss.lib.test;

import org.springframework.boot.test.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplateHandler;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhanghaolun on 16/10/6.
 */
public class RestTemplateForTest13x implements RestTemplateForTest {

  private final org.springframework.boot.test.TestRestTemplate restTemplate;

  public RestTemplateForTest13x(final TestRestTemplate.HttpClientOption... httpClientOptions) {
    this.restTemplate = new TestRestTemplate(httpClientOptions);
  }

  public RestTemplateForTest13x( //
    final String username, //
    final String password, //
    final TestRestTemplate.HttpClientOption... httpClientOptions //
  ) {
    this.restTemplate = new TestRestTemplate(username, password, httpClientOptions);
  }

  public RestTemplateForTest13x(final TestRestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public RestTemplate getRestTemplate() {
    return this.restTemplate;
  }

  public void setUriTemplateHandler(final UriTemplateHandler handler) {
    this.restTemplate.setUriTemplateHandler(handler);
  }


  @Override
  public <T> T getForObject(String url, Class<T> responseType, Object... urlVariables) throws RestClientException {
    return this.restTemplate.getForObject(url, responseType, urlVariables);
  }

  @Override
  public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> urlVariables) throws RestClientException {
    return this.restTemplate.getForObject(url, responseType, urlVariables);
  }

  @Override
  public <T> T getForObject(URI url, Class<T> responseType) throws RestClientException {
    return this.restTemplate.getForObject(url, responseType);
  }

  @Override
  public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... urlVariables) throws RestClientException {
    return this.restTemplate.getForEntity(url, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> urlVariables) throws RestClientException {
    return this.restTemplate.getForEntity(url, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> getForEntity(URI url, Class<T> responseType) throws RestClientException {
    return this.restTemplate.getForEntity(url, responseType);
  }

  @Override
  public HttpHeaders headForHeaders(String url, Object... urlVariables) throws RestClientException {
    return this.restTemplate.headForHeaders(url, urlVariables);
  }

  @Override
  public HttpHeaders headForHeaders(String url, Map<String, ?> urlVariables)
    throws RestClientException {
    return this.restTemplate.headForHeaders(url, urlVariables);
  }

  @Override
  public HttpHeaders headForHeaders(URI url) throws RestClientException {
    return this.restTemplate.headForHeaders(url);
  }

  @Override
  public URI postForLocation(String url, Object request, Object... urlVariables) throws RestClientException {
    return this.restTemplate.postForLocation(url, request, urlVariables);
  }

  @Override
  public URI postForLocation(String url, Object request, Map<String, ?> urlVariables) throws RestClientException {
    return this.restTemplate.postForLocation(url, request, urlVariables);
  }

  @Override
  public URI postForLocation(URI url, Object request) throws RestClientException {
    return this.restTemplate.postForLocation(url, request);
  }

  @Override
  public <T> T postForObject(String url, Object request, Class<T> responseType, Object... urlVariables) throws RestClientException {
    return this.restTemplate.postForObject(url, request, responseType, urlVariables);
  }

  @Override
  public <T> T postForObject(String url, Object request, Class<T> responseType, Map<String, ?> urlVariables) throws RestClientException {
    return this.restTemplate.postForObject(url, request, responseType, urlVariables);
  }

  @Override
  public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
    return this.restTemplate.postForObject(url, request, responseType);
  }

  @Override
  public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... urlVariables) throws RestClientException {
    return this.restTemplate.postForEntity(url, request, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Map<String, ?> urlVariables) throws RestClientException {
    return this.restTemplate.postForEntity(url, request, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> postForEntity(URI url, Object request, Class<T> responseType) throws RestClientException {
    return this.restTemplate.postForEntity(url, request, responseType);
  }

  @Override
  public void put(String url, Object request, Object... urlVariables) throws RestClientException {
    this.restTemplate.put(url, request, urlVariables);
  }

  @Override
  public void put(String url, Object request, Map<String, ?> urlVariables) throws RestClientException {
    this.restTemplate.put(url, request, urlVariables);
  }

  @Override
  public void put(URI url, Object request) throws RestClientException {
    this.restTemplate.put(url, request);
  }

  @Override
  public void delete(String url, Object... urlVariables) throws RestClientException {
    this.restTemplate.delete(url, urlVariables);
  }

  @Override
  public void delete(String url, Map<String, ?> urlVariables) throws RestClientException {
    this.restTemplate.delete(url, urlVariables);
  }

  @Override
  public void delete(URI url) throws RestClientException {
    this.restTemplate.delete(url);
  }

  @Override
  public Set<HttpMethod> optionsForAllow(String url, Object... urlVariables) throws RestClientException {
    return this.restTemplate.optionsForAllow(url, urlVariables);
  }

  @Override
  public Set<HttpMethod> optionsForAllow(String url, Map<String, ?> urlVariables) throws RestClientException {
    return this.restTemplate.optionsForAllow(url, urlVariables);
  }

  @Override
  public Set<HttpMethod> optionsForAllow(URI url) throws RestClientException {
    return this.restTemplate.optionsForAllow(url);
  }

  @Override
  public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... urlVariables) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> urlVariables) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType);
  }

  @Override
  public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Object... urlVariables) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType, Map<String, ?> urlVariables) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange(URI url, HttpMethod method, HttpEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType);
  }

  @Override
  public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity,
    Class<T> responseType) throws RestClientException {
    return this.restTemplate.exchange(requestEntity, responseType);
  }

  @Override
  public <T> ResponseEntity<T> exchange(RequestEntity<?> requestEntity, ParameterizedTypeReference<T> responseType) throws RestClientException {
    return this.restTemplate.exchange(requestEntity, responseType);
  }

  @Override
  public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Object... urlVariables) throws RestClientException {
    return this.restTemplate.execute(url, method, requestCallback, responseExtractor, urlVariables);
  }

  @Override
  public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Map<String, ?> urlVariables) throws RestClientException {
    return this.restTemplate.execute(url, method, requestCallback, responseExtractor, urlVariables);
  }

  @Override
  public <T> T execute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor) throws RestClientException {
    return this.restTemplate.execute(url, method, requestCallback, responseExtractor);
  }
}
