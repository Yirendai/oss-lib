package com.yirendai.oss.lib.webmvc.api;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class TokenBasedCookie<T> implements TypeSafeCookie<T> {

  private final DomainResolver domainResolver;
  private final Boolean httpOnly;
  private final int maxAge;
  private final String name;
  private final Boolean secure;
  private final TypeSafeToken<T> token;

  public TokenBasedCookie( //
    final DomainResolver domainResolver, //
    final Boolean httpOnly, //
    final int maxAge, //
    final String name, //
    final Boolean secure, //
    final TypeSafeToken<T> token //
  ) {
    this.domainResolver = domainResolver;
    this.httpOnly = httpOnly;
    this.maxAge = maxAge;
    this.name = name;
    this.secure = secure;
    this.token = token;
  }

  // --------------------

  @Override
  public void clearCookie(final HttpServletRequest request, final HttpServletResponse response) {
    final String domain = this.domainResolver.resolveDomain(request);
    final Cookie cookie = TypeSafeCookie.buildCookie(domain, this.httpOnly, null, this.name, this.secure, null);
    response.addCookie(cookie);
  }

  @Override
  public T getCookie(final HttpServletRequest request) {
    final String token = this.getValue(request);

    return this.token.fromToken(token);
  }

  @Override
  public String getValue(final HttpServletRequest request) {
    final Cookie cookie = TypeSafeCookie.findCookie(request, this.name);
    if (log.isTraceEnabled()) {
      log.trace("getValue cookie name: {}, cookie: {}", this.name, cookie);
    }
    return cookie != null ? cookie.getValue() : null;
  }

  @Override
  public T setCookie( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final T obj //
  ) {
    final String token = this.token.toToken(obj);

    final String domain = this.domainResolver.resolveDomain(request);
    final Cookie cookie = TypeSafeCookie.buildCookie(domain, this.httpOnly, this.maxAge, this.name, this.secure, token);
    response.addCookie(cookie);
    return obj;
  }
}
