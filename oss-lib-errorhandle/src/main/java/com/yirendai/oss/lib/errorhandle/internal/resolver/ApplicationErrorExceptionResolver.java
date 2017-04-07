package com.yirendai.oss.lib.errorhandle.internal.resolver;

import com.yirendai.oss.lib.errorhandle.api.AbstractConcreteExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ApplicationException;
import com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator;

import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestAttributes;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

/**
 * Created by zhanghaolun on 16/8/11.
 */
public class ApplicationErrorExceptionResolver
  extends AbstractConcreteExceptionResolver<ApplicationException> {

  @Override
  public Optional<Integer> status( //
    final RequestAttributes requestAttributes, //
    final ExceptionTranslator.Location location, //
    final ApplicationException throwable //
  ) {
    final HttpStatus httpStatus = throwable.getStatus();
    return Optional.ofNullable(httpStatus != null ? httpStatus.value() : null);
  }

  @Override
  public Optional<String> localizedMessage( //
    final RequestAttributes requestAttributes, //
    final ExceptionTranslator.Location location, //
    final ApplicationException throwable //
  ) {
    final Map<String, Serializable> contextVariables = throwable.getContextVariables();
    final String template = throwable.getTemplate();
    return this.exceptionTranslator.localizedMessage( //
      template, //
      requestAttributes, //
      throwable, //
      contextVariables //
    );
  }
}
