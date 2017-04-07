package com.yirendai.oss.lib.security.internal.template;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.yirendai.oss.lib.common.CodecUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.UrlUtils;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhanghaolun on 16/11/11.
 */
@Slf4j
public class SmartRedirectStrategy implements RedirectStrategy {

  public static final String PARAM_REDIRECT = "redirect";

  private boolean contextRelative;

  /**
   * Redirects the response to the supplied URL.
   * <p>
   * If <tt>contextRelative</tt> is set, the redirect value will be the value after the
   * request context path. Note that this will result in the loss of protocol
   * information (HTTP or HTTPS), so will cause problems if a redirect is being
   * performed to change to HTTPS, for example.
   * </p>
   */
  @Override
  public void sendRedirect( //
    final HttpServletRequest request,
    final HttpServletResponse response,
    final String url //
  ) throws IOException {
    String redirectUrl = buildRedirectUrl(request, url);
    redirectUrl = response.encodeRedirectURL(redirectUrl);

    if (log.isDebugEnabled()) {
      log.debug("Redirecting to '" + redirectUrl + "'");
    }

    response.sendRedirect(redirectUrl);
  }

  private String buildRedirectUrl( //
    final HttpServletRequest request, //
    final String url //
  ) {
    final String redirectUrl = calculateRedirectUrl(request, url);

    final String requestUrl = request.getRequestURL().toString();
    final String paramValue = request.getParameter(PARAM_REDIRECT);
    final Boolean containsRedirectParam = containsParam(requestUrl, PARAM_REDIRECT) || isNotBlank(paramValue);
    final String result;
    if (containsRedirectParam) {
      result = appendParam(redirectUrl, PARAM_REDIRECT, paramValue);
    } else {
      result = redirectUrl;
    }
    return result;
  }

  private String calculateRedirectUrl(final HttpServletRequest request, final String url) {
    final String contextPath = request.getContextPath();
    final String redirectUrl;
    if (!UrlUtils.isAbsoluteUrl(url)) {
      if (this.contextRelative) {
        redirectUrl = url;
      } else {
        redirectUrl = contextPath + url;
      }
    } else {
      // Full URL, including http(s)://
      if (!this.contextRelative) {
        redirectUrl = url;
      } else {
        // Calculate the relative URL from the fully qualified URL, minus the last
        // occurrence of the scheme and base context.
        final String withoutScheme = url.substring(url.lastIndexOf("://") + 3); // strip off scheme
        final String withoutContextPath = withoutScheme.substring( //
          withoutScheme.indexOf(contextPath) + contextPath.length());

        if (withoutContextPath.length() > 1 && withoutContextPath.charAt(0) == '/') {
          redirectUrl = url.substring(1);
        } else {
          redirectUrl = withoutContextPath;
        }
      }
    }
    return redirectUrl;
  }

  /**
   * If <tt>true</tt>, causes any redirection URLs to be calculated minus the protocol
   * and context path (defaults to <tt>false</tt>).
   *
   * @param useRelativeContext useRelativeContext
   */
  public void setContextRelative(final boolean useRelativeContext) {
    this.contextRelative = useRelativeContext;
  }

  public static Boolean containsParam(final String url, final String paramName) {
    Boolean containsParam = FALSE;
    if (isNotBlank(url) && url.contains("?")) { // find redirect param
      final String queryString = url.substring(url.indexOf('?') + 1);
      final String[] params = queryString.split("&");
      for (final String param : params) {
        final String name = param.split("=")[0];
        if (paramName.equals(name)) {
          containsParam = TRUE;
          break;
        }
      }
    }
    return containsParam;
  }

  @SneakyThrows
  public static String appendParam(final String url, final String paramName, final String paramValue) {
    final String param = paramName + "=" + CodecUtils.urlEncode(paramValue);
    final String result;
    if (url != null) {
      if (url.contains("?")) {
        result = url + "&" + param;
      } else {
        result = url + "?" + param;
      }
    } else {
      result = null;
    }
    return result;
  }
}
