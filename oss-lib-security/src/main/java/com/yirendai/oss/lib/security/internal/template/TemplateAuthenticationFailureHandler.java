package com.yirendai.oss.lib.security.internal.template;

import static com.yirendai.oss.lib.common.CodecUtils.urlEncode;

import com.yirendai.oss.lib.errorhandle.api.ExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by zhanghaolun on 16/8/23.
 */
@Slf4j
public class TemplateAuthenticationFailureHandler implements AuthenticationFailureHandler {
  // extends SimpleUrlAuthenticationFailureHandler

  private String defaultFailureUrl;
  private boolean forwardToDestination = false;
  private boolean allowSessionCreation = true;
  private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

  private ExceptionResolver<Throwable> exceptionResolver;
  private TypeSafeCookie<ResolvedError> resolvedErrorCookie;

  public TemplateAuthenticationFailureHandler( //
    final String defaultFailureUrl, //
    final ExceptionResolver<Throwable> exceptionResolver, //
    final TypeSafeCookie<ResolvedError> resolvedErrorCookie //
  ) {
    setDefaultFailureUrl(defaultFailureUrl);
    this.exceptionResolver = exceptionResolver;
    this.resolvedErrorCookie = resolvedErrorCookie;
  }

  @Override
  public void onAuthenticationFailure( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final AuthenticationException exception //
  ) throws IOException, ServletException {
    final ResolvedError resolvedError = this.exceptionResolver.resolve(request, exception);
    if (this.resolvedErrorCookie != null) {
      this.resolvedErrorCookie.setCookie(request, response, resolvedError.eraseTraces());
    }

    if (defaultFailureUrl == null) {
      log.debug("No failure URL set, sending 401 Unauthorized error");

      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
        "Authentication Failed: " + exception.getMessage());
    } else {
      saveException(request, exception);

      if (forwardToDestination) {
        log.debug("Forwarding to " + defaultFailureUrl);

        request.getRequestDispatcher(defaultFailureUrl)
          .forward(request, response);
      } else {
        final String url = this.defaultFailureUrl + "?error=" + urlEncode(resolvedError.getLocalizedMessage());
        log.debug("Redirecting to " + url);
        redirectStrategy.sendRedirect(request, response, url);
      }
    }
  }

  /**
   * <p>
   * Caches the {@code AuthenticationException} for use in view rendering.
   * </p>
   * If {@code forwardToDestination} is set to true, request scope will be used,
   * otherwise it will attempt to store the exception in the session. If there is no
   * session and {@code allowSessionCreation} is {@code true} a session will be created.
   * Otherwise the exception will not be stored.
   *
   * @param request   request
   * @param exception exception
   */
  protected final void saveException(final HttpServletRequest request, final AuthenticationException exception) {
    if (forwardToDestination) {
      request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
    } else {
      HttpSession session = request.getSession(false);

      if (session != null || allowSessionCreation) {
        request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
      }
    }
  }

  /**
   * The URL which will be used as the failure destination.
   *
   * @param defaultFailureUrl the failure URL, for example "/loginFailed.jsp".
   */
  public void setDefaultFailureUrl(String defaultFailureUrl) {
    Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl), "'"
      + defaultFailureUrl + "' is not a valid redirect URL");
    this.defaultFailureUrl = defaultFailureUrl;
  }

  protected boolean isUseForward() {
    return forwardToDestination;
  }

  /**
   * If set to <tt>true</tt>, performs a forward to the failure destination URL instead
   * of a redirect. Defaults to <tt>false</tt>.
   *
   * @param forwardToDestination forwardToDestination
   */
  public void setUseForward(final boolean forwardToDestination) {
    this.forwardToDestination = forwardToDestination;
  }

  /**
   * Allows overriding of the behaviour when redirecting to a target URL.
   *
   * @param redirectStrategy redirectStrategy
   */
  public void setRedirectStrategy(final RedirectStrategy redirectStrategy) {
    this.redirectStrategy = redirectStrategy;
  }

  protected RedirectStrategy getRedirectStrategy() {
    return this.redirectStrategy;
  }

  protected boolean isAllowSessionCreation() {
    return this.allowSessionCreation;
  }

  public void setAllowSessionCreation(boolean allowSessionCreation) {
    this.allowSessionCreation = allowSessionCreation;
  }

  public static TemplateAuthenticationFailureHandler templateFailureHandler( //
    final String loginFormUrl, //
    final ExceptionResolver<Throwable> exceptionResolver, //
    final TypeSafeCookie<ResolvedError> resolvedErrorCookie //
  ) {
    final TemplateAuthenticationFailureHandler failureHandler = new TemplateAuthenticationFailureHandler( //
      loginFormUrl, exceptionResolver, resolvedErrorCookie);
    failureHandler.setUseForward(false);
    failureHandler.setRedirectStrategy(new SmartRedirectStrategy());
    return failureHandler;
  }
}
