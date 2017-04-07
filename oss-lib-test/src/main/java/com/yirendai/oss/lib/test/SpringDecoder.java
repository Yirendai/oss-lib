package com.yirendai.oss.lib.test;

import com.google.common.collect.Lists;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpMessageConverterExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/*
 * @author Spencer Gibb
 */
public class SpringDecoder implements Decoder {

  private final ObjectFactory<HttpMessageConverters> messageConverters;

  public SpringDecoder(final ObjectFactory<HttpMessageConverters> messageConverters) {
    this.messageConverters = messageConverters;
  }

  @Override
  public Object decode(final Response response, final Type type) throws IOException, FeignException {
    if (type instanceof Class || type instanceof ParameterizedType) {
      @SuppressWarnings({"unchecked", "rawtypes"})
      final HttpMessageConverterExtractor<?> extractor =
        new HttpMessageConverterExtractor(type, this.messageConverters.getObject().getConverters());

      return extractor.extractData(new FeignResponseAdapter(response));
    }
    throw new DecodeException("type is not an instance of Class or ParameterizedType: " + type);
  }


  @Slf4j
  private static class FeignResponseAdapter implements ClientHttpResponse {

    private final Response response;

    FeignResponseAdapter(final Response response) {
      this.response = response;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
      return HttpStatus.valueOf(this.response.status());
    }

    @Override
    public int getRawStatusCode() throws IOException {
      return this.response.status();
    }

    @Override
    public String getStatusText() throws IOException {
      return this.response.reason();
    }

    @Override
    public void close() {
      try {
        this.response.body().close();
      } catch (final IOException ex) {
        // Ignore exception on close...
        log.debug("error close feign response body.", ex);
      }
    }

    @Override
    public InputStream getBody() throws IOException {
      return this.response.body().asInputStream();
    }

    @Override
    public HttpHeaders getHeaders() {
      final HttpHeaders responseHeaders = new HttpHeaders();
      response.headers().entrySet().stream() //
        .forEach(entry -> responseHeaders.put(entry.getKey(), Lists.newArrayList(entry.getValue())));
      return responseHeaders;
    }
  }
}

