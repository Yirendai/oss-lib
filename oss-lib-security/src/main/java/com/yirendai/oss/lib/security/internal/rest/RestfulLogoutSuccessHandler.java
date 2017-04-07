package com.yirendai.oss.lib.security.internal.rest;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * see: {@link org.springframework.security.web.authentication.logout.LogoutFilter}.
 *
 * <p>Created by zhanghaolun on 16/7/12.</p>
 */
public class RestfulLogoutSuccessHandler implements LogoutSuccessHandler {

  public void onLogoutSuccess(final HttpServletRequest request, final HttpServletResponse response,
      final Authentication authentication) throws IOException, ServletException {
    response.setStatus(OK.value());
  }
}
