package com.yirendai.oss.lib.webmvc.api;

import static java.util.concurrent.TimeUnit.DAYS;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * cookie.
 */
public interface TypeSafeCookie<T> {

  /**
   * build a cookie.setMaxAge(0) if no value provided.
   *
   * @param domain   domain
   * @param httpOnly httpOnly
   * @param maxAge   maxAge
   * @param name     name
   * @param secure   secure
   * @param value    value
   * @return cookie
   */
  static Cookie buildCookie( //
    final String domain, //
    final Boolean httpOnly, //
    final Integer maxAge, //
    final String name, //
    final Boolean secure, //
    final String value //
  ) {
    final Cookie cookie = new Cookie(name, value);
    cookie.setComment(name);
    if (isNotBlank(domain)) {
      cookie.setDomain(domain);
    }
    if (httpOnly != null && httpOnly) {
      cookie.setHttpOnly(true);
    }
    final int defaultMaxAge = value != null ? (int) DAYS.toSeconds(1L) : 0;
    cookie.setMaxAge(maxAge != null ? maxAge : defaultMaxAge);
    cookie.setPath("/");
    if (secure != null && secure) {
      cookie.setSecure(true);
    }
    cookie.setVersion(1);
    return cookie;
  }

  static void configSessionCookie( //
    final ServletContext servletContext, //
    final String domain, //
    final String name, //
    final Integer maxAge //
  ) {
    servletContext.setInitParameter("org.eclipse.jetty.servlet.SessionCookie", name);
    final SessionCookieConfig sessionCookieConfig = servletContext.getSessionCookieConfig();
    if (isNotBlank(domain)) {
      sessionCookieConfig.setDomain(domain);
    }
    sessionCookieConfig.setMaxAge(maxAge);
    sessionCookieConfig.setName(name);
    sessionCookieConfig.setPath("/");
  }

  static Cookie findCookie(final HttpServletRequest request, final String name) {
    final Cookie[] cookies = request.getCookies();
    final Optional<Cookie> firstMatch = Arrays.stream(cookies != null ? cookies : new Cookie[0])
      .filter(cookie -> cookie.getName().equals(name)).findFirst();
    return firstMatch.isPresent() ? firstMatch.get() : null;
  }

  void clearCookie(HttpServletRequest request, HttpServletResponse response);

  T getCookie(HttpServletRequest request);

  String getValue(HttpServletRequest request);

  T setCookie(HttpServletRequest request, HttpServletResponse response, T obj);
}
