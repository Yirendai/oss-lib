package com.yirendai.oss.lib.security.internal.rest;

import com.yirendai.oss.lib.errorhandle.internal.RestfulExceptionHandler;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Note: Not a bean, avoid auto pick-up.
 * see: {@link org.springframework.security.web.authentication.logout.LogoutFilter}.
 * Created by zhanghaolun on 16/8/21.
 */
public class RestfulLogoutDisabledFilter extends GenericFilterBean {

  private RequestMatcher logoutRequestMatcher;

  @Autowired
  @NonNull
  private RestfulExceptionHandler exceptionHandler;

  @Override
  public void doFilter( //
      final ServletRequest req, //
      final ServletResponse res, //
      final FilterChain chain //
  ) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    if (requiresLogout(request, response)) {
      this.exceptionHandler.resolveAndHandle(request, response, new IllegalStateException("security disabled"));
    } else {
      chain.doFilter(request, response);
    }
  }

  /**
   * Allow subclasses to modify when a logout should take place.
   *
   * @param request  the request
   * @param response the response
   * @return <code>true</code> if logout should occur, <code>false</code> otherwise
   */
  protected boolean requiresLogout( //
      final HttpServletRequest request, //
      final HttpServletResponse response //
  ) {
    return this.logoutRequestMatcher.matches(request);
  }

  public void setLogoutRequestMatcher(final RequestMatcher logoutRequestMatcher) {
    Assert.notNull(logoutRequestMatcher, "logoutRequestMatcher cannot be null");
    this.logoutRequestMatcher = logoutRequestMatcher;
  }

  public void setFilterProcessesUrl(final String filterProcessesUrl) {
    this.logoutRequestMatcher = new AntPathRequestMatcher(filterProcessesUrl);
  }

  public void setExceptionHandler(final RestfulExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }
}
