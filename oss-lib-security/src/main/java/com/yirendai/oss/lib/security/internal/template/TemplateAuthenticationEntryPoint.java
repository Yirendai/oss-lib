package com.yirendai.oss.lib.security.internal.template;

import static com.yirendai.oss.lib.security.internal.template.SmartRedirectStrategy.PARAM_REDIRECT;
import static com.yirendai.oss.lib.security.internal.template.SmartRedirectStrategy.appendParam;

import com.yirendai.oss.lib.common.CodecUtils;
import com.yirendai.oss.lib.errorhandle.api.ExceptionResolver;
import com.yirendai.oss.lib.errorhandle.api.ResolvedError;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;

import lombok.SneakyThrows;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhanghaolun on 16/8/23.
 */
public class TemplateAuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

  private ExceptionResolver<Throwable> exceptionResolver;
  private TypeSafeCookie<ResolvedError> resolvedErrorCookie;

  /**
   * @param loginFormUrl        URL where the login page can be found. Should either be relative to the web-app context
   *                            path (include a leading {@code /}) or an absolute URL.
   * @param exceptionResolver   exceptionResolver
   * @param resolvedErrorCookie resolvedErrorCookie
   */
  public TemplateAuthenticationEntryPoint(
    final String loginFormUrl, //
    final ExceptionResolver<Throwable> exceptionResolver, //
    final TypeSafeCookie<ResolvedError> resolvedErrorCookie //
  ) {
    super(loginFormUrl);

    this.exceptionResolver = exceptionResolver;
    this.resolvedErrorCookie = resolvedErrorCookie;
  }

  @SneakyThrows
  protected String determineUrlToUseForThisRequest( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final AuthenticationException exception //
  ) {
    final ResolvedError resolvedError = this.exceptionResolver.resolve(request, exception);
    if (this.resolvedErrorCookie != null) {
      this.resolvedErrorCookie.setCookie(request, response, resolvedError.eraseTraces());
    }
    final String url = super.determineUrlToUseForThisRequest(request, response, exception);
    return url + "?error=" + CodecUtils.urlEncode(resolvedError.getLocalizedMessage());
  }

  @SneakyThrows
  protected String buildRedirectUrlToLoginPage( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final AuthenticationException authException //
  ) {
    final String redirectUrl = super.buildRedirectUrlToLoginPage(request, response, authException);

    final Boolean containsRedirectParam = SmartRedirectStrategy.containsParam(redirectUrl, PARAM_REDIRECT);

    return !containsRedirectParam ? //
      appendParam(redirectUrl, PARAM_REDIRECT, request.getRequestURL().toString()) : redirectUrl;
  }

  public static TemplateAuthenticationEntryPoint templateEntryPoint( //
    final String loginFormUrl, //
    final ExceptionResolver<Throwable> exceptionResolver, //
    final TypeSafeCookie<ResolvedError> resolvedErrorCookie //
  ) {
    final TemplateAuthenticationEntryPoint entryPoint = new TemplateAuthenticationEntryPoint( //
      loginFormUrl, exceptionResolver, resolvedErrorCookie);
    entryPoint.setForceHttps(false);
    entryPoint.setUseForward(false);
    return entryPoint;
  }
}
