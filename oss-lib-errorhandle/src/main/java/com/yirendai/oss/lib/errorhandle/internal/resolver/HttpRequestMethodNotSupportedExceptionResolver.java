package com.yirendai.oss.lib.errorhandle.internal.resolver;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.yirendai.oss.lib.errorhandle.api.AbstractConcreteExceptionResolver;

import org.springframework.http.HttpHeaders;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Optional;

public class HttpRequestMethodNotSupportedExceptionResolver
    extends AbstractConcreteExceptionResolver<HttpRequestMethodNotSupportedException> {

  @Override
  public Optional<HttpHeaders> createHeaders( //
      final RequestAttributes requestAttributes, //
      final HttpRequestMethodNotSupportedException exception //
  ) {
    final HttpHeaders headers = super.createHeaders(requestAttributes, exception).orElse(new HttpHeaders());

    if (!isEmpty(exception.getSupportedMethods())) {
      headers.setAllow(exception.getSupportedHttpMethods());
    }

    return !headers.isEmpty() ? Optional.of(headers) : Optional.empty();
  }
}
