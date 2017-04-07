package com.yirendai.oss.lib.security.internal.rest;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.yirendai.oss.lib.errorhandle.internal.RestfulExceptionHandler;

import lombok.Getter;
import lombok.NonNull;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhanghaolun on 16/6/28.
 */
public class RestfulBasicAuthenticationEntryPoint implements AuthenticationEntryPoint, InitializingBean {

  public static final String DEFAULT_REALM_NAME = "DefaultRealmName";

  @Getter
  private String realmName;

  @Autowired
  @NonNull
  private RestfulExceptionHandler exceptionHandler;

  @Override
  public void commence( //
      final HttpServletRequest request, //
      final HttpServletResponse response, //
      final AuthenticationException authException //
  ) throws IOException, ServletException {
    response.addHeader("WWW-Authenticate", "Basic realm=\"" + this.getRealmName() + "\"");

    // response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    // response.getWriter().println("HTTP Status 401 - " + authException.getMessage());
    this.exceptionHandler.resolveAndHandle(request, response, authException);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (isBlank(this.getRealmName())) {
      this.setRealmName(DEFAULT_REALM_NAME);
    }
    // Assert.hasText(this.realmName, "realmName must be specified");
  }

  public void setRealmName(final String realmName) {
    this.realmName = realmName;
  }

  public void setExceptionHandler(final RestfulExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }
}
