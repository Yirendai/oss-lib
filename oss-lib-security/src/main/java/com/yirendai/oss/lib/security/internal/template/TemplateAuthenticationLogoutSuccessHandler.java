package com.yirendai.oss.lib.security.internal.template;

import static com.yirendai.oss.lib.security.internal.template.SmartRedirectStrategy.PARAM_REDIRECT;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by zhanghaolun on 16/11/11.
 */
public class TemplateAuthenticationLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

  @Override
  public void onLogoutSuccess(
    final HttpServletRequest request, //
    final HttpServletResponse response, //
    final Authentication authentication //
  ) throws IOException, ServletException {
    super.onLogoutSuccess(request, response, authentication);
  }

  public static TemplateAuthenticationLogoutSuccessHandler templateLogoutSuccessHandler() {
    final TemplateAuthenticationLogoutSuccessHandler handler = new TemplateAuthenticationLogoutSuccessHandler();
    handler.setAlwaysUseDefaultTargetUrl(false);
    handler.setTargetUrlParameter(PARAM_REDIRECT);
    handler.setUseReferer(false);
    return handler;
  }
}
