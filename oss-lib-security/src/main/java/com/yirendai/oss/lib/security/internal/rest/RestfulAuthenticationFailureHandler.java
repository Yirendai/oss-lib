package com.yirendai.oss.lib.security.internal.rest;

import com.yirendai.oss.lib.errorhandle.internal.RestfulExceptionHandler;

import lombok.NonNull;
import lombok.Setter;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证失败不清cookie.
 *
 * <p>Created by zhanghaolun on 16/6/28.</p>
 */
public class RestfulAuthenticationFailureHandler implements AuthenticationFailureHandler {

  @NonNull
  @Setter
  private RestfulExceptionHandler exceptionHandler;

  public static RestfulAuthenticationFailureHandler restfulFailureHandler( //
      final RestfulExceptionHandler exceptionHandler //
  ) {
    final RestfulAuthenticationFailureHandler failureHandler = new RestfulAuthenticationFailureHandler();
    failureHandler.setExceptionHandler(exceptionHandler);
    return failureHandler;
  }

  @Override
  public void onAuthenticationFailure( //
      final HttpServletRequest request, //
      final HttpServletResponse response, //
      final AuthenticationException authException //
  ) throws IOException, ServletException {

    // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    this.exceptionHandler.resolveAndHandle(request, response, authException);
  }
}
