package com.yirendai.oss.lib.errorhandle.api;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static com.google.common.collect.Lists.newArrayList;
import static com.yirendai.oss.lib.common.CurlUtils.curl;
import static com.yirendai.oss.lib.errorhandle.api.ResolvedErrorException.isResolvedError;
import static com.yirendai.oss.lib.errorhandle.api.ResolvedErrorException.isResolvedErrorWrapByOther;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.servlet.HandlerMapping.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE;

import com.google.common.collect.ImmutableList;

import com.yirendai.oss.lib.common.Defaults;
import com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator.Location;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * AbstractConcreteExceptionResolver.
 *
 * @param <T> exception type
 * @author zhanghaolun
 */
@Slf4j
public abstract class AbstractConcreteExceptionResolver<T extends Throwable> implements ConcreteExceptionResolver<T> {

  private static final String TRACE_OFF = "trace off";

  protected final Class<T> exceptionClass;

  protected ConversionService conversionService;
  protected ExceptionTranslator exceptionTranslator;
  protected StackTraceIndicator stackTraceIndicator;

  /**
   * This constructor determines the exception class from the generic class parameter {@code T}.
   */
  protected AbstractConcreteExceptionResolver() {
    this.exceptionClass = determineTargetType();
  }

  protected AbstractConcreteExceptionResolver(final Class<T> exceptionClass) {
    this.exceptionClass = exceptionClass;
  }

  @Deprecated
  static HttpStatus parseHttpStatus(final Object value) {
    Assert.notNull(value, "Values of the resolverMap map must not be null");

    final HttpStatus result;
    if (value instanceof HttpStatus) {
      result = (HttpStatus) value;
    } else if (value instanceof Integer) {
      result = HttpStatus.valueOf((int) value);
    } else if (value instanceof String) {
      result = HttpStatus.valueOf(Integer.parseInt((String) value));
    } else {
      throw new IllegalArgumentException(String.format( //
        "Values of the resolverMap maps must be instance of " //
          + "ErrorResponseFactory, HttpStatus, String, or int, " //
          + "but %s given",
        value.getClass()));
    }
    return result;
  }

  @Deprecated
  @SuppressWarnings("unused")
  private static <T extends Throwable> Throwable cause(final T exception) {
    Throwable cause = null;
    if (exception != null) {
      cause = exception;
      while (cause instanceof ServletException && cause.getCause() != null) {
        final ServletException servletException = ((ServletException) cause);
        cause = servletException.getCause();
      }
    }
    return cause;
  }

  private static String error(final Optional<Integer> statusOptional) {
    // TODO Optional<Integer>' used as type for parameter
    String result;
    if (statusOptional.isPresent()) {
      final Integer status = statusOptional.get();
      try {
        final HttpStatus httpStatus = HttpStatus.valueOf(status);
        result = httpStatus.getReasonPhrase();
      } catch (final IllegalArgumentException ignored) { // Unable to obtain a reason
        if (log.isDebugEnabled()) {
          log.debug("Unable to obtain a reason, status {}", status, ignored);
        }
        result = "Http Status " + status;
      }
    } else {
      result = "None";
    }
    return result;
  }

  @Override
  public final ResolvedError resolve( //
    final HttpServletRequest request, //
    final T throwable //
  ) {
    final DateTime now = Defaults.now();

    // See http://stackoverflow.com/a/12979543/2217862
    // This attribute is never set in MockMvc, so it's not covered in integration it.
    request.removeAttribute(PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE);

    final RequestAttributes requestAttributes = new ServletRequestAttributes(request);

    final Boolean stackTrace = this.stackTraceIndicator.stackTrace(request, null);
    final String path = request.getQueryString() != null ? //
      request.getRequestURI() + request.getQueryString() : //
      request.getRequestURI();
    final String track = stackTrace ? curl(request) : TRACE_OFF;

    return this.resolve(requestAttributes, throwable, now, stackTrace, path, track);
  }

  /*
   * (non-Javadoc)
   * 
   * @see ExceptionResolver#resolve(RequestAttributes, Throwable)
   */
  @Override
  public final ResolvedError resolve( //
    final RequestAttributes requestAttributes, //
    final T throwable //
  ) {
    final DateTime now = Defaults.now();

    final Object requestUri = getAttribute( //
      requestAttributes, //
      "javax.servlet.error.request_uri" //
    );
    final Boolean stackTrace = this.stackTraceIndicator.stackTrace(null, null);
    final String path = requestUri != null ? requestUri.toString() : null;
    // TODO final String track = stackTrace ? curl(request) : TRACE_OFF; ? 忘记为什么这里简单使用requestUri
    final String track = stackTrace ? path : TRACE_OFF;

    return this.resolve(requestAttributes, throwable, now, stackTrace, path, track);
  }

  protected ResolvedError resolve( //
    final RequestAttributes requestAttributes, //
    final T throwable, //
    final DateTime now, //
    final Boolean stackTrace, //
    final String path, //
    final String track //
  ) {
    final ResolvedError resolvedError;
    if (isResolvedError(throwable)) {
      resolvedError = ((ResolvedErrorException) throwable).getError();
      resolvedError.trackPrepend(track);
    } else if (isResolvedErrorWrapByOther(throwable)) {
      // 注意顺序
      // 1.判断是不是ResolvedErrorException
      // 2.判断是不是被封装了的ResolvedErrorException(如HystrixException)
      log.warn("这里将被封装过ResolvedErrorException提取出来，原异常信息为：", throwable);
      resolvedError = ((ResolvedErrorException) throwable.getCause()).getError();
      resolvedError.trackPrepend(track);
    } else {
      final Location location = find(throwable).orElse(null);
      // basic
      final Optional<List<ValidationError>> errorsOptional = this.validationErrors(throwable);
      final Optional<Integer> statusOptional = this.status(requestAttributes, location, throwable);
      final String error = error(statusOptional);
      final List<ValidationError> errors = errorsOptional.orElse(null);
      final String exception = throwable != null ? throwable.getClass().getName() : null;
      final String message = message(requestAttributes, throwable, errorsOptional);
      final Integer status = statusOptional.orElse(500);
      final Long timestamp = now.getMillis();
      final String trace = stackTrace && throwable != null ? getStackTraceAsString(throwable) : null;
      // extended
      final String datetime = now.toString(Defaults.ISO8601);
      final String localizedMessage = this.localizedMessage(requestAttributes, location, throwable).orElse("null");
      final HttpHeaders headers = this.createHeaders(requestAttributes, throwable).orElse(null);
      final List<String> tracks = ImmutableList.of(track);

      resolvedError = ResolvedError.resolvedErrorBuilder() //
        // basic
        .error(error) //
        .errors(errors != null ? errors.toArray(new ValidationError[errors.size()]) : null) //
        .exception(exception) //
        .message(message) //
        .path(path) //
        .status(status) //
        .timestamp(timestamp) //
        .trace(trace) //
        // extended
        .datetime(datetime) //
        .localizedMessage(localizedMessage) //
        .headers(HttpHeader.fromHttpHeaders(headers)) //
        .tracks(tracks.toArray(new String[tracks.size()])) //
        .build();
    }

    logError(requestAttributes, throwable, resolvedError);

    return resolvedError;
  }

  /**
   * Logs the exception; on ERROR level when status is 5xx, otherwise on INFO level without stack
   * trace, or DEBUG level with stack trace. The logger name is {@code ExceptionResolver}.
   *
   * @param requestAttributes requestAttributes
   * @param throwable         throwable
   * @param resolvedError     The exception to log.
   */
  protected void logError( //
    final RequestAttributes requestAttributes, //
    final T throwable, //
    final ResolvedError resolvedError //
  ) {
    if (resolvedError.getStatus() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
      final Marker marker = MarkerFactory.getMarker("error");
      final String msg = String.format( //
        "%s ~> %d", //
        resolvedError.getPath(), //
        resolvedError.getStatus() //
      );

      if (log.isTraceEnabled()) {
        log.trace("attributes in request scope: {}", //
          newArrayList(requestAttributes.getAttributeNames(SCOPE_REQUEST)));
      }
      log.warn(marker, msg, new Object[]{throwable});
    }
  }

  @SuppressWarnings("unchecked")
  private Class<T> determineTargetType() {
    return (Class<T>) GenericTypeResolver.resolveTypeArguments( //
      this.getClass(), //
      AbstractConcreteExceptionResolver.class //
    )[0];
  }

  @Override
  public Class<T> getExceptionClass() {
    return this.exceptionClass;
  }

  @Override
  public void setConversionService(final ConversionService conversionService) {
    this.conversionService = conversionService;
  }

  @Override
  public ExceptionTranslator getExceptionTranslator() {
    return this.exceptionTranslator;
  }

  @Override
  public void setExceptionTranslator(final ExceptionTranslator exceptionTranslator) {
    this.exceptionTranslator = exceptionTranslator;
  }

  @Override
  public void setStackTraceIndicator(final StackTraceIndicator stackTraceIndicator) {
    this.stackTraceIndicator = stackTraceIndicator;
  }
}
