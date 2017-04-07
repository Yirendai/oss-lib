package com.yirendai.oss.lib.common;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Created by zhanghaolun on 16/11/16.
 */
public abstract class RequestUtlis {

  private RequestUtlis() {
  }

  public static Charset findCharset(final HttpServletRequest request) {
    final String encoding = request.getCharacterEncoding();
    final Charset result;
    if (isBlank(encoding)) {
      result = StandardCharsets.UTF_8;
    } else {
      result = Charset.forName(encoding);
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T extends HttpServletRequestWrapper> T findWrapper( //
    final ServletRequest request, final Class<T> type //
  ) {
    final T result;
    if (request != null) {
      if (type.isAssignableFrom(request.getClass())) {
        result = (T) request;
      } else {
        if (HttpServletRequestWrapper.class.isAssignableFrom(request.getClass())) {
          return findWrapper(((HttpServletRequestWrapper) request).getRequest(), type);
        } else {
          result = null;
        }
      }
    } else {
      result = null;
    }
    return result;
  }
}
