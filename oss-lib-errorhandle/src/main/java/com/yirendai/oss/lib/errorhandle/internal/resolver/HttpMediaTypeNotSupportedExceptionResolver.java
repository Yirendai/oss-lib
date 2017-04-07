package com.yirendai.oss.lib.errorhandle.internal.resolver;

import static org.springframework.util.CollectionUtils.isEmpty;

import com.yirendai.oss.lib.errorhandle.api.AbstractConcreteExceptionResolver;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.context.request.RequestAttributes;

import java.util.List;
import java.util.Optional;

public class HttpMediaTypeNotSupportedExceptionResolver
    extends AbstractConcreteExceptionResolver<HttpMediaTypeNotSupportedException> {

  @Override
  public Optional<HttpHeaders> createHeaders( //
      final RequestAttributes requestAttributes, //
      final HttpMediaTypeNotSupportedException exception //
  ) {
    final HttpHeaders headers = super.createHeaders(requestAttributes, exception).orElse(new HttpHeaders());

    final List<MediaType> mediaTypes = exception.getSupportedMediaTypes();
    if (!isEmpty(mediaTypes)) {
      headers.setAccept(mediaTypes);
    }

    return !headers.isEmpty() ? Optional.of(headers) : Optional.empty();
  }
}
