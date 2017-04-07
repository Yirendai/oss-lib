package com.yirendai.oss.lib.security.internal.rest;

import com.yirendai.oss.lib.errorhandle.internal.RestfulExceptionHandler;

import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
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
 * see: {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}.
 * Created by zhanghaolun on 16/7/5.
 */
public class RestfulLoginDisabledFilter extends GenericFilterBean {

  private RequestMatcher requiresAuthenticationRequestMatcher;
  private boolean postOnly = true;

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

    if (!requiresAuthentication(request, response)) {
      chain.doFilter(request, response);
    } else {
      if (this.postOnly && !"POST".equals(request.getMethod())) {
        this.exceptionHandler.resolveAndHandle(request, response, new AuthenticationServiceException(
          "Authentication method not supported: " + request.getMethod()));
      }
      this.exceptionHandler.resolveAndHandle(request, response, new IllegalStateException("security disabled"));
    }
  }

  /**
   * Indicates whether this filter should attempt to process a login request for the
   * current invocation.
   * <p>It strips any parameters from the "path" section of the request URL (such as the
   * jsessionid parameter in <em>http://host/myapp/index.html;jsessionid=blah</em>)
   * before matching against the <code>filterProcessesUrl</code> property.</p>
   * <p>Subclasses may override for special requirements, such as Tapestry integration.</p>
   *
   * @param request  request
   * @param response response
   * @return <code>true</code> if the filter should attempt authentication, <code>false</code> otherwise.
   */
  @SuppressWarnings({"squid:S1172"})
  protected boolean requiresAuthentication( //
    final HttpServletRequest request, //
    final HttpServletResponse response //
  ) {
    return this.requiresAuthenticationRequestMatcher.matches(request);
  }

  /**
   * Sets the URL that determines if authentication is required.
   *
   * @param filterProcessesUrl filterProcessesUrl
   */
  public void setFilterProcessesUrl(final String filterProcessesUrl) {
    setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher(filterProcessesUrl));
  }

  public final void setRequiresAuthenticationRequestMatcher(final RequestMatcher requestMatcher) {
    Assert.notNull(requestMatcher, "requestMatcher cannot be null");
    this.requiresAuthenticationRequestMatcher = requestMatcher;
  }

  /**
   * Defines whether only HTTP POST requests will be allowed by this filter. If set to
   * true, and an authentication request is received which is not a POST request, an
   * exception will be raised immediately and authentication will not be attempted. The
   * <tt>unsuccessfulAuthentication()</tt> method will be called as if handling a failed
   * authentication.
   *
   * <p>Defaults to <tt>true</tt> but may be overridden by subclasses.</p>
   *
   * @param postOnly postOnly
   */
  public void setPostOnly(final boolean postOnly) {
    this.postOnly = postOnly;
  }

  public void setExceptionHandler(final RestfulExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }
}
