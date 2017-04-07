package com.yirendai.oss.lib.security.internal.rest;

import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_COOKIE;
import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_TOKEN;
import static com.yirendai.oss.lib.security.api.Security.HEADER_AUTH_TOKEN;

import com.yirendai.oss.lib.errorhandle.internal.RestfulExceptionHandler;
import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.UserDetails;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;
import com.yirendai.oss.lib.webmvc.internal.DefaultHttpEntityMethodProcessor;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhanghaolun on 16/6/28.
 */
@Slf4j
public class RestfulAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  @Qualifier(GENERIC_USER_COOKIE)
  @Autowired(required = false)
  @Setter
  private TypeSafeCookie<GenericUser> cookie;
  @Autowired
  @NonNull
  @Setter
  private RestfulExceptionHandler exceptionHandler;
  @Autowired
  @Setter
  private DefaultHttpEntityMethodProcessor httpEntityMethodProcessor;
  @Qualifier(GENERIC_USER_TOKEN)
  @Autowired(required = false)
  @Setter
  private TypeSafeToken<GenericUser> token;

  public static RestfulAuthenticationSuccessHandler restfulSuccessHandler( //
    final TypeSafeCookie<GenericUser> cookie, //
    final RestfulExceptionHandler exceptionHandler, //
    final DefaultHttpEntityMethodProcessor httpEntityMethodProcessor, //
    final TypeSafeToken<GenericUser> token //
  ) {
    final RestfulAuthenticationSuccessHandler handler = new RestfulAuthenticationSuccessHandler();
    handler.setCookie(cookie);
    handler.setExceptionHandler(exceptionHandler);
    handler.setHttpEntityMethodProcessor(httpEntityMethodProcessor);
    handler.setToken(token);
    return handler;
  }

  @Override
  public void onAuthenticationSuccess( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final Authentication authentication //
  ) throws IOException, ServletException {
    clearAuthenticationAttributes(request);

    final GenericUser user = GenericUser.fromPrincipal(authentication);
    user.eraseCredentials();

    if (this.cookie != null) {
      this.cookie.setCookie(request, response, user);
    }

    if (this.token != null) {
      final String token = this.token.toToken(user);
      response.setHeader(HEADER_AUTH_TOKEN, token);
    }

    final HttpEntity<UserDetails> returnValue = new HttpEntity<>(user.toUserInfo());

    final Method method = ClassUtils.getMethod(GenericUser.class, "toUserInfo");
    final MethodParameter returnTypeMethodParam = new MethodParameter(method, -1);
    final MethodParameter returnType = new MethodParameter(returnTypeMethodParam);
    final ModelAndViewContainer mavContainer = new ModelAndViewContainer();
    final NativeWebRequest webRequest = new ServletWebRequest(request, response);
    try {
      this.httpEntityMethodProcessor.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    } catch (final Exception ex) {
      if (log.isWarnEnabled()) {
        log.warn("Failed to process response: {}", returnValue, ex);
      }
      this.exceptionHandler.resolveAndHandle(request, response, ex);
    }
  }
}
