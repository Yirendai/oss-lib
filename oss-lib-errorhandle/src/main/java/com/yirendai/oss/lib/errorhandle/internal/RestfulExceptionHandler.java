package com.yirendai.oss.lib.errorhandle.internal;

import static com.yirendai.oss.lib.errorhandle.api.ExceptionResolver.COMPOSITE_EXCEPTION_RESOLVER;

import com.yirendai.oss.lib.errorhandle.api.ExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.HttpHeader;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.webmvc.internal.DefaultHttpEntityMethodProcessor;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhanghaolun on 16/8/22.
 */
@Slf4j
public class RestfulExceptionHandler {

  @Qualifier(COMPOSITE_EXCEPTION_RESOLVER)
  @Autowired
  @Getter
  @Setter
  private ExceptionResolver<Throwable> exceptionResolver;
  @Autowired
  @Setter
  private DefaultHttpEntityMethodProcessor httpEntityMethodProcessor;

  static ResponseEntity<ResolvedError> toResponseEntity(final ResolvedError error) {
    ResponseEntity<ResolvedError> entity = null;
    try {
      entity = new ResponseEntity<>( //
        error, //
        HttpHeader.toHttpHeaders(error.getHeaders()), //
        HttpStatus.valueOf(error.getStatus()) //
      );
    } catch (final Exception ex) {
      if (log.isWarnEnabled()) {
        log.warn("Failed to process resolvedError: {}", error, ex);
      }
    }
    return entity;
  }

  public ModelAndView resolveAndHandle(
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final Throwable throwable //
  ) {
    final ResolvedError resolvedError = this.exceptionResolver.resolve(request, throwable);
    this.handleResolved(request, response, resolvedError);
    return new ModelAndView();
  }

  public void handleResolved( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final ResolvedError resolvedError //
  ) {
    final ResponseEntity<ResolvedError> returnValue = toResponseEntity(resolvedError);

    if (returnValue != null) {
      try {
        final Method method = ClassUtils.getMethod( //
          ExceptionResolver.class, "resolve", HttpServletRequest.class, Throwable.class //
        );
        final MethodParameter returnTypeMethodParam = new MethodParameter(method, -1);
        // This method caches the resolved value, so it's convenient to initialize it only once here.
        returnTypeMethodParam.getGenericParameterType();

        // XXX: Create MethodParameter from the actually used subclass of ExceptionResolver?
        final MethodParameter returnType = new MethodParameter(returnTypeMethodParam);
        final ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        final NativeWebRequest webRequest = new ServletWebRequest(request, response);

        this.httpEntityMethodProcessor.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
      } catch (final Exception ex) {
        if (log.isWarnEnabled()) {
          log.warn("Failed to process error response: {}", returnValue, ex);
        }
      }
    }
  }
}
