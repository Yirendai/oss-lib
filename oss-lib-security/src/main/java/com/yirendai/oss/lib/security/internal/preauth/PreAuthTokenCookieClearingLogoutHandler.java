package com.yirendai.oss.lib.security.internal.preauth;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;

import lombok.NonNull;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * see: {@link org.springframework.security.web.authentication.logout.LogoutFilter}.
 *
 * <p>Created by zhanghaolun on 16/7/6.</p>
 */
public class PreAuthTokenCookieClearingLogoutHandler implements LogoutHandler {

  @Qualifier(GenericUser.GENERIC_USER_COOKIE)
  @Autowired(required = false)
  @Setter
  @NonNull
  private TypeSafeCookie<GenericUser> cookie;

  @Override
  public void logout( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final Authentication authentication //
  ) {
    if (this.cookie != null) {
      this.cookie.clearCookie(request, response);
    }
  }
}
