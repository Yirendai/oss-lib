package com.yirendai.oss.lib.security.internal.preauth;

import static com.yirendai.oss.lib.security.internal.preauth.PreAuthTokenFilter.PERMITED;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.internal.AuthenticationTokens;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

@Slf4j
public class PreAuthTokenAuthenticationProvider implements AuthenticationProvider {

  private final Boolean bypass;

  public PreAuthTokenAuthenticationProvider(final Boolean bypass) {
    this.bypass = bypass;
  }

  @Override
  public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
    final Authentication result;
    if (authentication != null && this.supports(authentication.getClass())) {
      final PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) authentication;
      final Object principal = token.getPrincipal();
      final Object credentials = token.getCredentials();

      if (this.bypass || PERMITED.equals(credentials)) {
        result = AuthenticationTokens.authenticated(token);
      } else {
        if (!GenericUser.isGenericUser(principal)) {
          if (log.isDebugEnabled()) {
            log.debug("not containsUserInfo");
          }
          throw new BadCredentialsException( //
            "The presented AuthenticationToken does not contain the expected principal or credentials");
        }

        if (!GenericUser.isGenericUserLogin((GenericUser) principal)) {
          if (log.isDebugEnabled()) {
            log.debug("not login");
          }
          throw new BadCredentialsException("login needed");
        }

        result = AuthenticationTokens.authenticated(token);
      }
    } else {
      result = null;
    }

    return result;
  }

  @Override
  public boolean supports(final Class<?> authentication) {
    return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
