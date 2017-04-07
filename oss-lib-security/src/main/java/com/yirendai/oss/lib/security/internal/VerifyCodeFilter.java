package com.yirendai.oss.lib.security.internal;

import static com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenFilter.ATTR_PRINCIPAL;
import static com.yirendai.oss.lib.security.starter.FormAuthConfiguration.FORM_AUTHENTICATION_ENTRYPOINT;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.Security;
import com.yirendai.oss.lib.security.api.VerifyCodeProvider;
import com.yirendai.oss.lib.webmvc.api.RequestResolver;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO Note: Not a bean, avoid auto pick-up.
 * before UsernamePasswordAuthenticationFilter.
 *
 * <p>
 * Created by zhanghaolun on 16/7/14.
 * </p>
 */
@Setter
@Slf4j
public class VerifyCodeFilter extends GenericFilterBean {

  /**
   * will be invoked when authentication fails. Typically an instance of {@link
   * org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint}.
   */
  @Qualifier(FORM_AUTHENTICATION_ENTRYPOINT)
  @Autowired
  @NonNull
  private AuthenticationEntryPoint formAuthenticationEntryPoint;

  @Autowired(required = false)
  @NonNull
  private VerifyCodeProvider codeVerifyProvider;

  @Autowired
  @NonNull
  private RequestResolver requestResolver;

  @Override
  public void doFilter( //
    final ServletRequest req, final ServletResponse res, final FilterChain chain //
  ) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    try {
      if (this.authenticationIsRequired(request)) {
        final GenericUser principal = (GenericUser) request.getAttribute(ATTR_PRINCIPAL);
        this.authenticate(request, principal);
      }
    } catch (final AuthenticationException failed) {
      SecurityContextHolder.clearContext();

      if (log.isTraceEnabled()) {
        log.trace("AUTH authentication failed. not login.", failed);
      }

      this.formAuthenticationEntryPoint.commence(request, response, failed);
      return;
    }

    chain.doFilter(request, response);
  }

  private boolean authenticationIsRequired(final HttpServletRequest request) {
    // TODO fix this, 有context-path的情况下可能不对
    return Security.authenticationIsRequired() && this.requestResolver.isLoginRequest(request);
  }

  private void authenticate( //
    final HttpServletRequest request, //
    final GenericUser principal //
  ) throws AuthenticationException {
    final String code = request.getParameter("verifycode");
    if (isBlank(code)) {
      throw new BadCredentialsException("verifycode is required");
    }
    final String uuid = principal.getUuid();
    final Boolean match = this.codeVerifyProvider.match(uuid, code);
    if (!match) {
      throw new BadCredentialsException("verifycode not match");
    }
  }

  public String getCodeUrl() {
    return this.codeVerifyProvider.getCodeUrl();
  }

  @Autowired
  @Override
  public void setEnvironment(final Environment environment) {
    super.setEnvironment(environment);
  }
}
