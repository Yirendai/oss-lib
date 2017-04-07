package com.yirendai.oss.lib.test;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
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
public class RestTemplateForTest14x implements RestTemplateForTest {

  private final org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

  public RestTemplateForTest14x(final RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = new TestRestTemplate(restTemplateBuilder);
  }

  public RestTemplateForTest14x(final TestRestTemplate.HttpClientOption... httpClientOptions) {
    this.restTemplate = new TestRestTemplate(httpClientOptions);
  }

  public RestTemplateForTest14x( //
    final String username,  //
    final String password, //
    final TestRestTemplate.HttpClientOption... httpClientOptions //
  ) {
    this.restTemplate = new TestRestTemplate(username, password, httpClientOptions);
  }

  public RestTemplateForTest14x(final RestTemplate restTemplate) {
    this.restTemplate = new TestRestTemplate(restTemplate);
  }

  public RestTemplateForTest14x( //
    final RestTemplate restTemplate, //
    final String username, //
    final String password, //
    final TestRestTemplate.HttpClientOption... httpClientOptions //
  ) {
    this.restTemplate = new TestRestTemplate(restTemplate, username, password, httpClientOptions);
  }

  public RestTemplateForTest14x(final TestRestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public RestTemplate getRestTemplate() {
    return this.restTemplate.getRestTemplate();
  }

  public void setUriTemplateHandler(final UriTemplateHandler handler) {
    this.restTemplate.setUriTemplateHandler(handler);
  }

  public RestTemplateForTest withBasicAuth(final String username, final String password) {
    return new RestTemplateForTest14x(this.restTemplate.withBasicAuth(username, password));
  }

  @Override
  public <T> T getForObject( //
    final String url, final Class<T> responseType, final Object... urlVariables //
  ) throws RestClientException {
    return this.restTemplate.getForObject(url, responseType, urlVariables);
  }

  @Override
  public <T> T getForObject( //
    final String url, final Class<T> responseType, final Map<String, ?> urlVariables //
  ) throws RestClientException {
    return this.restTemplate.getForObject(url, responseType, urlVariables);
  }

  @Override
  public <T> T getForObject( //
    final URI url, final Class<T> responseType //
  ) throws RestClientException {
    return this.restTemplate.getForObject(url, responseType);
  }

  @Override
  public <T> ResponseEntity<T> getForEntity( //
    final String url, final Class<T> responseType, final Object... urlVariables //
  ) throws RestClientException {
    return this.restTemplate.getForEntity(url, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> getForEntity( //
    final String url, final Class<T> responseType, final Map<String, ?> urlVariables //
  ) throws RestClientException {
    return this.restTemplate.getForEntity(url, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> getForEntity(final URI url, final Class<T> responseType) throws RestClientException {
    return this.restTemplate.getForEntity(url, responseType);
  }

  @Override
  public HttpHeaders headForHeaders(final String url, final Object... urlVariables) throws RestClientException {
    return this.restTemplate.headForHeaders(url, urlVariables);
  }

  @Override
  public HttpHeaders headForHeaders( //
    final String url, final Map<String, ?> urlVariables //
  ) throws RestClientException {
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
  public <T> T postForObject( //
    final String url, final Object request, final Class<T> responseType, final Object... urlVariables //
  ) throws RestClientException {
    return this.restTemplate.postForObject(url, request, responseType, urlVariables);
  }

  @Override
  public <T> T postForObject( //
    final String url, final Object request, final Class<T> responseType, final Map<String, ?> urlVariables //
  ) throws RestClientException {
    return this.restTemplate.postForObject(url, request, responseType, urlVariables);
  }

  @Override
  public <T> T postForObject(URI url, Object request, Class<T> responseType) throws RestClientException {
    return this.restTemplate.postForObject(url, request, responseType);
  }

  @Override
  public <T> ResponseEntity<T> postForEntity( //
    final String url, final Object request, final Class<T> responseType, final Object... urlVariables //
  ) throws RestClientException {
    return this.restTemplate.postForEntity(url, request, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> postForEntity( //
    final String url, final Object request, final Class<T> responseType, final Map<String, ?> urlVariables //
  ) throws RestClientException {
    return this.restTemplate.postForEntity(url, request, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> postForEntity( //
    final URI url, final Object request, final Class<T> responseType //
  ) throws RestClientException {
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
  public <T> ResponseEntity<T> exchange( //
    final String url, final HttpMethod method, final HttpEntity<?> requestEntity, //
    final Class<T> responseType, final Object... urlVariables //
  ) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange( //
    final String url, final HttpMethod method, final HttpEntity<?> requestEntity, //
    final Class<T> responseType, final Map<String, ?> urlVariables //
  ) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange( //
    final URI url, final HttpMethod method, final HttpEntity<?> requestEntity, //
    final Class<T> responseType //
  ) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType);
  }

  @Override
  public <T> ResponseEntity<T> exchange( //
    final String url, final HttpMethod method, final HttpEntity<?> requestEntity, //
    final ParameterizedTypeReference<T> responseType, final Object... urlVariables //
  ) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange( //
    final String url, final HttpMethod method, final HttpEntity<?> requestEntity, //
    final ParameterizedTypeReference<T> responseType, final Map<String, ?> urlVariables //
  ) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType, urlVariables);
  }

  @Override
  public <T> ResponseEntity<T> exchange( //
    final URI url, final HttpMethod method, final HttpEntity<?> requestEntity, //
    final ParameterizedTypeReference<T> responseType //
  ) throws RestClientException {
    return this.restTemplate.exchange(url, method, requestEntity, responseType);
  }

  @Override
  public <T> ResponseEntity<T> exchange( //
    final RequestEntity<?> requestEntity, final Class<T> responseType //
  ) throws RestClientException {
    return this.restTemplate.exchange(requestEntity, responseType);
  }

  @Override
  public <T> ResponseEntity<T> exchange( //
    final RequestEntity<?> requestEntity, final ParameterizedTypeReference<T> responseType //
  ) throws RestClientException {
    return this.restTemplate.exchange(requestEntity, responseType);
  }

  @Override
  public <T> T execute( //
    final String url, final HttpMethod method, final RequestCallback requestCallback, //
    final ResponseExtractor<T> responseExtractor, final Object... urlVariables //
  ) throws RestClientException {
    return this.restTemplate.execute(url, method, requestCallback, responseExtractor, urlVariables);
  }

  @Override
  public <T> T execute( //
    final String url, final HttpMethod method, final RequestCallback requestCallback, //
    final ResponseExtractor<T> responseExtractor, final Map<String, ?> urlVariables //
  ) throws RestClientException {
    return this.restTemplate.execute(url, method, requestCallback, responseExtractor, urlVariables);
  }

  @Override
  public <T> T execute( //
    final URI url, final HttpMethod method, final RequestCallback requestCallback, //
    final ResponseExtractor<T> responseExtractor //
  ) throws RestClientException {
    return this.restTemplate.execute(url, method, requestCallback, responseExtractor);
  }
}
