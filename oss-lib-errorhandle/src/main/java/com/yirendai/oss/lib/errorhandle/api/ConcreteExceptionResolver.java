package com.yirendai.oss.lib.errorhandle.api;

import static org.springframework.util.StringUtils.isEmpty;

import com.google.common.collect.ImmutableMap;

import com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator.Location;

import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestAttributes;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.ConstraintViolationException;

/**
 * Created by zhanghaolun on 16/8/11.
 */
public interface ConcreteExceptionResolver<T extends Throwable> extends ExceptionResolver<T> {

  Class<T> getExceptionClass();

  /**
   * set conversionService.
   *
   * @param conversionService convert value in validation error into string
   */
  void setConversionService(ConversionService conversionService);

  ExceptionTranslator getExceptionTranslator();

  void setExceptionTranslator(ExceptionTranslator exceptionTranslator);

  void setStackTraceIndicator(StackTraceIndicator stackTraceIndicator);

  default Optional<HttpHeaders> createHeaders(final RequestAttributes request, final T throwable) {
    return Optional.of(ResolvedError.newHttpHeaders());
  }

  /**
   * find a request attribute.
   *
   * @param requestAttributes requestAttributes
   * @param name              name
   * @param <A>               attributeType
   * @return request attribute
   */
  @SuppressWarnings("unchecked")
  default <A> A getAttribute(final RequestAttributes requestAttributes, final String name) {
    return (A) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
  }

  default <E extends Throwable> String message( //
    final RequestAttributes requestAttributes, //
    final E throwable, //
    final Optional<List<ValidationError>> errorsOptional //
  ) {
    final Object fromAttribute = getAttribute(requestAttributes, "javax.servlet.error.message");
    final String fromError;
    if (throwable != null) {
      if (!errorsOptional.isPresent()) {
        fromError = throwable.getMessage();
      } else {
        final int errorCount = errorsOptional.get().size();
        fromError = errorCount > 0 ? "Validation failed. Error count: " + errorCount : "No errors";
      }
    } else {
      fromError = null;
    }
    final String result;
    if ((!isEmpty(fromAttribute) || fromError == null) && !(throwable instanceof BindingResult)) {
      result = isEmpty(fromAttribute) ? "No message available" : fromAttribute.toString();
    } else {
      result = fromError;
    }
    return result;
  }

  default Optional<Location> find(final T throwable) {
    final ExceptionTranslator exceptionTranslator = this.getExceptionTranslator();
    return exceptionTranslator != null ? exceptionTranslator.find(throwable) : Optional.empty();
  }

  default Optional<Integer> status( //
    final RequestAttributes requestAttributes, //
    final Location location, //
    final T throwable //
  ) {
    final ExceptionTranslator exceptionTranslator = this.getExceptionTranslator();
    final Optional<Integer> translated = exceptionTranslator != null ? //
      exceptionTranslator.status(location) : //
      Optional.empty();
    final Integer fromAttribute = this.getAttribute(requestAttributes, "javax.servlet.error.status_code");
    return translated.isPresent() ? translated : Optional.ofNullable(fromAttribute);
  }

  /**
   * Translate exception and generates localizedMessage.
   *
   * @param request   The current request.
   * @param location  The location get data from.
   * @param throwable throwable
   * @return A TranslateResult.
   */
  default Optional<String> localizedMessage( //
    final RequestAttributes request, //
    final Location location, //
    final T throwable //
  ) {
    final ExceptionTranslator exceptionTranslator = this.getExceptionTranslator();
    final Optional<String> result;
    if (exceptionTranslator != null) {
      final Map<String, Serializable> contextVariables = ImmutableMap.of();
      final String template = exceptionTranslator.template(location).orElse(null);
      result = exceptionTranslator.localizedMessage(template, request, throwable, contextVariables);
    } else {
      result = Optional.empty();
    }
    return result;
  }

  /**
   * validation errors.
   *
   * @param throwable {@link ConstraintViolationException}, {@link MethodArgumentNotValidException}
   * @return validation errors
   */
  default Optional<List<ValidationError>> validationErrors(final T throwable) {
    return Optional.empty();
  }
}
