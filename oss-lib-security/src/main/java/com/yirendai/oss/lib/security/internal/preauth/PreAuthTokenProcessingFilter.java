package com.yirendai.oss.lib.security.internal.preauth;

import static com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenFilter.ATTR_PRINCIPAL;
import static com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenFilter.ATTR_PRINCIPAL_TOKEN;
import static lombok.AccessLevel.PROTECTED;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.Security;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.NullRememberMeServices;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Not a bean, avoid auto pick-up.
 * after BasicAuthenticationFilter and PreAuthTestUserFilter.
 * see: BasicAuthenticationFilter or AbstractPreAuthenticatedProcessingFilter
 *
 * <p>authenticate against the supplied {@code AuthenticationManager}
 * and use the supplied {@code AuthenticationEntryPoint} to handle authentication failures.</p>
 *
 * @author zhanghaolun
 */
@Setter
@Getter(value = PROTECTED)
@Slf4j
public class PreAuthTokenProcessingFilter extends GenericFilterBean {

  /**
   * will be invoked when authentication fails. Typically an instance of {@link
   * BasicAuthenticationEntryPoint}.
   */
  private AuthenticationEntryPoint authenticationEntryPoint;
  /**
   * the bean to submit authentication requests to.
   */
  private AuthenticationManager authenticationManager;
  private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource;
  private RememberMeServices rememberMeServices;

  public PreAuthTokenProcessingFilter() {
    this.authenticationEntryPoint = null;
    this.authenticationManager = new NoOpAuthenticationManager();

    this.authenticationDetailsSource = new WebAuthenticationDetailsSource();
    this.rememberMeServices = new NullRememberMeServices();
  }

  @Override
  public void doFilter( //
    final ServletRequest req, //
    final ServletResponse res, //
    final FilterChain chain //
  ) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    try {
      if (this.authenticationIsRequired()) {
        final GenericUser principal = (GenericUser) request.getAttribute(ATTR_PRINCIPAL);
        final String token = (String) request.getAttribute(ATTR_PRINCIPAL_TOKEN);
        request.removeAttribute(ATTR_PRINCIPAL);
        request.removeAttribute(ATTR_PRINCIPAL_TOKEN);

        final AbstractAuthenticationToken authRequest = this.attempAuthentication(request, principal, token);
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));

        final Authentication authResult = this.authenticationManager.authenticate(authRequest);

        if (log.isTraceEnabled()) {
          log.trace("AUTH authentication success: {}, principal: {}", authResult, principal);
        }

        final SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authResult);
        this.rememberMeServices.loginSuccess(request, response, authResult);
        this.onSuccessfulAuthentication(request, response, authResult);
      }
    } catch (final AuthenticationException failed) {
      SecurityContextHolder.clearContext();

      if (log.isTraceEnabled()) {
        log.trace("AUTH authentication failed. not login.", failed);
      }

      this.rememberMeServices.loginFail(request, response);
      this.onUnsuccessfulAuthentication(request, response, failed);
      if (this.getIgnoreFailure()) {
        chain.doFilter(request, response);
      } else {
        this.authenticationEntryPoint.commence(request, response, failed);
      }
      return;
    }

    chain.doFilter(request, response);
  }

  private boolean authenticationIsRequired() {
    return Security.authenticationIsRequired();
  }

  @SuppressWarnings({"squid:S1172"})
  private AbstractAuthenticationToken attempAuthentication( //
    final HttpServletRequest request, //
    final GenericUser principal, //
    final String token
  ) {
    return new PreAuthenticatedAuthenticationToken(principal, token);
  }

  @SuppressWarnings({"squid:S1172"})
  protected void onSuccessfulAuthentication( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final Authentication authResult //
  ) throws IOException {
    // Do nothing
  }

  @SuppressWarnings({"squid:S1172"})
  protected void onUnsuccessfulAuthentication( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final AuthenticationException failed //
  ) throws IOException {
    // Do nothing
  }

  public Boolean getIgnoreFailure() {
    return this.authenticationEntryPoint == null;
  }

  @Autowired
  @Override
  public void setEnvironment(final Environment environment) {
    super.setEnvironment(environment);
  }

  public static class NoOpAuthenticationManager implements AuthenticationManager {

    @Override
    public Authentication authenticate(final Authentication authentication) {
      return authentication;
    }
  }
}
