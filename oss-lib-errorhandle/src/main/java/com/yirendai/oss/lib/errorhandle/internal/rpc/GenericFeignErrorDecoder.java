package com.yirendai.oss.lib.errorhandle.internal.rpc;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.collect.Lists.newArrayList;
import static com.yirendai.oss.lib.errorhandle.api.ResolvedErrorException.isResolvedError;
import static org.apache.commons.lang3.StringUtils.toEncodedString;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.yirendai.oss.lib.common.JaxbUtils;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.errorhandle.api.ResolvedErrorException;

import feign.codec.DecodeException;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Setter
@Slf4j
public class GenericFeignErrorDecoder implements feign.codec.ErrorDecoder {

  private final feign.codec.ErrorDecoder delegate = new Default();

  private final Jaxb2Marshaller jaxb2Marshaller = JaxbUtils.jaxb2Marshaller();
  @Autowired
  private ObjectMapper objectMapper;

  private static HttpHeaders cloneHeaders(final feign.Response response) {
    final HttpHeaders responseHeaders = new HttpHeaders();
    response.headers().entrySet().stream()
        .forEach(entry -> responseHeaders.put(entry.getKey(), newArrayList(entry.getValue())));
    return responseHeaders;
  }

  private static byte[] responseBody(final feign.Response response) {
    try {
      return StreamUtils.copyToByteArray(response.body().asInputStream());
    } catch (final Exception wrapped) {
      log.info("Failed to process response body.", wrapped);
      throw new DecodeException("Failed to process response body.", wrapped);
    }
  }

  @Override
  public Exception decode(final String methodKey, final feign.Response response) {
    if (log.isDebugEnabled()) {
      log.debug("decode : {}", methodKey);
    }

    final HttpHeaders responseHeaders = cloneHeaders(response);
    final byte[] responseBody = responseBody(response);
    final Exception result;
    if (ResolvedErrorException.isResolvedError(responseHeaders)) {
      final String info = toEncodedString(responseBody, UTF_8);
      try {
        // 如果被调用的服务配置了XmlHttpMessageConverter(使用XStreamMarshaller) 可能导致数据被包裹在<string></string>里面
        final String json =
            info.startsWith("<string>") ? JaxbUtils.unmarshal(this.jaxb2Marshaller, info, String.class) : info;
        result = new ResolvedErrorException(this.objectMapper.readValue(json, ResolvedError.class));
      } catch (final Exception wrapped) {
        log.info("Failed to decode resolvedError. info: " + info, wrapped);
        throw new DecodeException("Failed to decode resolvedError. info: " + info, wrapped);
      }
    } else {
      final HttpStatus statusCode = HttpStatus.valueOf(response.status());
      final String statusText = response.reason();
      if (response.status() >= 400 && response.status() <= 499) {
        result = new HttpClientErrorException( //
            statusCode, //
            statusText, //
            responseHeaders, //
            responseBody, //
            null);
      } else if (response.status() >= 500 && response.status() <= 599) {
        result = new HttpServerErrorException( //
            statusCode, //
            statusText, //
            responseHeaders, //
            responseBody, //
            null);
      } else {
        result = delegate.decode(methodKey, response);
      }
    }
    return result;
  }
}
