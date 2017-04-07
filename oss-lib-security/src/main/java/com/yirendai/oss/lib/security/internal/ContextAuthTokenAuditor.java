package com.yirendai.oss.lib.security.internal;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * obtain current user (probably from session or Spring Security context – depends on application).
 *
 * @author zhanghaolun
 */
@Deprecated
public class ContextAuthTokenAuditor implements AuditorAware<String> {

  @Override
  public String getCurrentAuditor() {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication authentication = securityContext.getAuthentication();
    // ((org.springframework.security.core.userdetails.User)
    // authentication.getPrincipal()).getUsername()
    return authentication == null || !authentication.isAuthenticated() ? //
      "unknown" : authentication.getName();
  }
}
