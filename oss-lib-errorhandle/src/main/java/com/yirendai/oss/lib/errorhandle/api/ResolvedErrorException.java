package com.yirendai.oss.lib.errorhandle.api;

import static com.yirendai.oss.lib.errorhandle.api.ResolvedError.HEADER_RESOLVED_ERROR;
import static java.lang.Boolean.FALSE;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.http.HttpHeaders;

/**
 * Created by zhanghaolun on 16/8/13.
 */
@JsonIgnoreProperties({"cause", "stackTrace", "suppressed"})
public class ResolvedErrorException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final ResolvedError error;

  public ResolvedErrorException(final ResolvedError error) {
    super();
    this.error = error;
  }

  public ResolvedError getError() {
    return this.error;
  }

  public static Boolean isResolvedError(final Throwable error) {
    final Boolean result;
    if (error != null) {
      result = ResolvedErrorException.class.isAssignableFrom(error.getClass());
    } else {
      result = FALSE;
    }
    return result;
  }

  public static Boolean isResolvedError(final HttpHeaders headers) {
    return headers.containsKey(HEADER_RESOLVED_ERROR);
  }

  /**
   * 判断是否是经过封装过的ResolvedErrorException.
   *
   * @param error error
   * @return boolean boolean
   */
  public static Boolean isResolvedErrorWrapByOther(final Throwable error) {
    final Boolean result;
    if (error != null && error.getCause() != null) {
      result = ResolvedErrorException.class.isAssignableFrom(error.getCause().getClass());
    } else {
      result = FALSE;
    }
    return result;
  }
}
