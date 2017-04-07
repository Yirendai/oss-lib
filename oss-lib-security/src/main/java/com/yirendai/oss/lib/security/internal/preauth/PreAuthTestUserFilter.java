package com.yirendai.oss.lib.security.internal.preauth;

import static com.google.common.base.Preconditions.checkArgument;
import static com.yirendai.oss.lib.security.api.GenericUser.GENERIC_USER_TOKEN;
import static com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenFilter.ATTR_PRINCIPAL;
import static com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenFilter.ATTR_PRINCIPAL_TOKEN;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.Security;
import com.yirendai.oss.lib.security.api.User;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;

import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Note: Not a bean, avoid auto pick-up.
 * after BasicAuthenticationFilter.
 *
 * <p>Created by zhanghaolun on 16/7/7.</p>
 */
@Setter
public class PreAuthTestUserFilter extends GenericFilterBean {

  private final String defaultTestUser;
  private final Map<String, User> testUsers;
  @Qualifier(GENERIC_USER_TOKEN)
  @Autowired
  private TypeSafeToken<GenericUser> token;

  public PreAuthTestUserFilter(
    final String defaultTestUser, //
    final List<User> users //
  ) {
    super();
    checkArgument(defaultTestUser != null);
    this.defaultTestUser = defaultTestUser;
    this.testUsers = users.stream().collect(toMap(User::getName, identity()));
  }

  @Override
  public void doFilter( //
    final ServletRequest req, //
    final ServletResponse res, //
    final FilterChain chain //
  ) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) res;

    // use testUser when no principal found.
    if (request.getAttribute(ATTR_PRINCIPAL) == null && //
      request.getAttribute(ATTR_PRINCIPAL_TOKEN) == null && //
      this.authenticationIsRequired()) {
      final GenericUser principal = this.findPrincipal(request);
      if (principal != null) {
        final String token = this.token.toToken(principal);
        request.setAttribute(ATTR_PRINCIPAL, principal);
        request.setAttribute(ATTR_PRINCIPAL_TOKEN, token);
      }
    }

    chain.doFilter(request, response);
  }

  private boolean authenticationIsRequired() {
    return Security.authenticationIsRequired();
  }

  @Autowired
  @Override
  public void setEnvironment(final Environment environment) {
    super.setEnvironment(environment);
  }

  private GenericUser findPrincipal(final HttpServletRequest request) {
    final String fromParameter = request.getParameter("testUser");
    final String username = isNotBlank(fromParameter) ? fromParameter : this.defaultTestUser;
    final User user = this.testUsers.get(username);
    return GenericUser.fromUser(user);
  }
}
