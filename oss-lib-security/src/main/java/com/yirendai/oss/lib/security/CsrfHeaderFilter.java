package com.yirendai.oss.lib.security;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CsrfHeaderFilter for AngularJs.
 * <p>see: https://github.com/spring-guides/tut-spring-security-and-angular-js/blob/1ab67bd14e4a7fbfb0d5ed9f5a6060beca13d699/modular/src/main/java/demo/UiApplication.java</p>
 * <p>see: https://spring.io/guides/tutorials/spring-security-and-angular-js/</p>
 * <p>see: {@link org.springframework.security.web.csrf.CsrfFilter}</p>
 * <p>For Angular built in support for CSRF (which it calls "XSRF") based on cookies.</p>
 * <p>Created by zhanghaolun on 16/6/28.</p>
 */
public class CsrfHeaderFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain
  ) throws ServletException, IOException {
    // Spring Security provides it as a request attribute by default
    final CsrfToken csrf = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

    if (csrf != null) {
      // Angular wants the cookie name to be "XSRF-TOKEN"
      Cookie cookie = WebUtils.getCookie(request, "XSRF-TOKEN");
      final String token = csrf.getToken();
      if (cookie == null || token != null && !token.equals(cookie.getValue())) {
        cookie = new Cookie("XSRF-TOKEN", token);
        cookie.setPath("/");
        response.addCookie(cookie);
      }
    }
    filterChain.doFilter(request, response);
  }
}
