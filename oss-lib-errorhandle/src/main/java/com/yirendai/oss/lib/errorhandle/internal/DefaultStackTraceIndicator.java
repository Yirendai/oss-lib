package com.yirendai.oss.lib.errorhandle.internal;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace.ALWAYS;
import static org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace.NEVER;
import static org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace.ON_TRACE_PARAM;

import com.yirendai.oss.lib.errorhandle.api.StackTraceIndicator;

import org.springframework.boot.autoconfigure.web.ErrorProperties.IncludeStacktrace;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhanghaolun on 16/7/1.
 */
@ConfigurationProperties(prefix = "server.error", ignoreUnknownFields = true)
public class DefaultStackTraceIndicator implements StackTraceIndicator {

  private IncludeStacktrace includeStacktrace = NEVER;

  static Boolean stackTrace( //
    final HttpServletRequest request, //
    final IncludeStacktrace includeStacktrace //
  ) {
    final Boolean result;
    if (ON_TRACE_PARAM == includeStacktrace) {
      result = request != null && request.getParameterMap().containsKey("trace");
    } else if (ALWAYS == includeStacktrace) {
      result = TRUE;
    } else { // NEVER or null
      result = FALSE;
    }
    return result;
  }

  @Override
  public Boolean stackTrace(final HttpServletRequest request, final MediaType produces) {
    return stackTrace(request, this.includeStacktrace);
  }

  public void setIncludeStacktrace(final IncludeStacktrace includeStacktrace) {
    this.includeStacktrace = includeStacktrace;
  }
}
