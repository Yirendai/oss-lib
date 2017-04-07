package com.yirendai.oss.lib.security.internal.preauth;

import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_COOKIE;
import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_TOKEN;
import static com.yirendai.oss.lib.security.api.Security.HEADER_AUTH_TOKEN;
import static lombok.AccessLevel.PROTECTED;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.Security;
import com.yirendai.oss.lib.security.starter.PermitedRequestConfiguration;
import com.yirendai.oss.lib.webmvc.api.TypeSafeCookie;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Not a bean, avoid auto pick-up.
 * Created by zhanghaolun on 16/7/15.
 */
@Setter
@Getter(value = PROTECTED)
public class PreAuthTokenFilter extends GenericFilterBean {

  public static final String ATTR_PRINCIPAL = "principal";
  static final String ATTR_PRINCIPAL_TOKEN = "principal_token";
  public static final String PERMITED = "PERMITED";

  @Qualifier(GENERIC_USER_COOKIE)
  @Autowired
  private TypeSafeCookie<GenericUser> cookie;

  @Qualifier(PermitedRequestConfiguration.PERMITED_REQUEST_MATCHER)
  @Autowired(required = false)
  private RequestMatcher permitedRequestMatcher;

  @Qualifier(GENERIC_USER_TOKEN)
  @Autowired
  private TypeSafeToken<GenericUser> token;

  @SuppressWarnings({"squid:S1871"})
  @Override
  public void doFilter( //
    final ServletRequest req, //
    final ServletResponse res, //
    final FilterChain chain //
  ) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    if (this.authenticationIsRequired()) {
      Object[] principalAndToken = this.findPrincipalAndToken(request);
      final GenericUser principalFound = (GenericUser) principalAndToken[0];
      final String tokenFound = (String) principalAndToken[1];

      final GenericUser principal;
      final String tokenContent;
      if (principalFound != null && tokenFound != null) {
        if (GenericUser.isGenericUserLogin(principalFound)) {
          principal = principalFound;
          tokenContent = tokenFound;
        } else if (this.permitedRequestMatcher.matches(request)) { // unknown user
          principal = null;
          tokenContent = PERMITED;
        } else {
          // suppress sonar warnings about 'if structure with the same implementation is at best duplicate code'
          principal = principalFound;
          tokenContent = tokenFound;
        }
      } else if (this.permitedRequestMatcher.matches(request)) {
        principal = null;
        tokenContent = PERMITED;
      } else {
        principal = GenericUser.unknownUser();
        tokenContent = this.token.toToken(principal);

        // write unknownUser to client
        response.setHeader(HEADER_AUTH_TOKEN, tokenContent);
        this.cookie.setCookie(request, response, principal);
      }

      if (principal != null) {
        request.setAttribute(ATTR_PRINCIPAL, principal);
      }
      if (tokenContent != null) {
        request.setAttribute(ATTR_PRINCIPAL_TOKEN, tokenContent);
      }
    }

    chain.doFilter(request, response);
  }

  Object[] findPrincipalAndToken(final HttpServletRequest request) {
    // 自适应使用header或cookie.
    final GenericUser principal;
    final String tokenContent;

    final String tokenFromHeader = request.getHeader(HEADER_AUTH_TOKEN);
    final GenericUser principalFromHeader = this.token.fromToken(tokenFromHeader);
    if (principalFromHeader != null) {
      principal = principalFromHeader;
      tokenContent = tokenFromHeader;
    } else {
      final String tokenFromCookie = this.cookie.getValue(request);
      final GenericUser principalFromCookie = this.cookie.getCookie(request);
      if (principalFromCookie != null) {
        principal = principalFromCookie;
        tokenContent = tokenFromCookie;
      } else {
        principal = null;
        tokenContent = null;
      }
    }
    return new Object[]{principal, tokenContent};
  }

  private boolean authenticationIsRequired() {
    return Security.authenticationIsRequired();
  }

  @Autowired
  @Override
  public void setEnvironment(final Environment environment) {
    super.setEnvironment(environment);
  }
}
