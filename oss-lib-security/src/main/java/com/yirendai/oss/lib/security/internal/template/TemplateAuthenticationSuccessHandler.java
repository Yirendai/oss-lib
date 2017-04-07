package com.yirendai.oss.lib.security.internal.template;

import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_COOKIE;
import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_TOKEN;
import static com.yirendai.oss.lib.security.api.Security.HEADER_AUTH_TOKEN;
import static com.yirendai.oss.lib.security.internal.template.SmartRedirectStrategy.PARAM_REDIRECT;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.NullRequestCache;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhanghaolun on 16/8/23.
 */
public class TemplateAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

  @Qualifier(GENERIC_USER_COOKIE)
  @Autowired(required = false)
  @Setter
  private TypeSafeCookie<GenericUser> cookie;

  @Qualifier(GENERIC_USER_TOKEN)
  @Autowired(required = false)
  @Setter
  private TypeSafeToken<GenericUser> token;

  public TemplateAuthenticationSuccessHandler(final String defaultTargetUrl) {
    super();
    this.setDefaultTargetUrl(defaultTargetUrl);
  }

  @Override
  public void onAuthenticationSuccess( //
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final Authentication authentication //
  ) throws ServletException, IOException { //

    final GenericUser user = GenericUser.fromPrincipal(authentication);
    user.eraseCredentials();

    if (this.cookie != null) {
      this.cookie.setCookie(request, response, user);
    }

    if (this.token != null) {
      final String token = this.token.toToken(user);
      response.setHeader(HEADER_AUTH_TOKEN, token);
    }

    super.onAuthenticationSuccess(request, response, authentication);
  }

  public static TemplateAuthenticationSuccessHandler templateSuccessHandler(final String defaultTargetUrl) {
    final TemplateAuthenticationSuccessHandler handler = new TemplateAuthenticationSuccessHandler(defaultTargetUrl);
    handler.setAlwaysUseDefaultTargetUrl(false);
    handler.setRequestCache(new NullRequestCache()); // stateless application does not use request cache
    handler.setTargetUrlParameter(PARAM_REDIRECT);
    handler.setUseReferer(false);
    return handler;
  }
}
