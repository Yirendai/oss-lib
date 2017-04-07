package com.yirendai.oss.lib.security.internal;

import static com.yirendai.oss.lib.security.api.GenericUser.isGenericUser;

import com.google.common.collect.ImmutableSet;

import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.webmvc.api.TypeSafeToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Collection;

/**
 * Created by zhanghaolun on 16/10/30.
 */
public abstract class AuthenticationTokens {

  private AuthenticationTokens() {
  }

  public static PreAuthenticatedAuthenticationToken authenticated( //
    final PreAuthenticatedAuthenticationToken authentication //
  ) {
    final PreAuthenticatedAuthenticationToken result;

    if (authentication == null) {
      result = null;
    } else {
      authentication.setAuthenticated(true);
      authentication.eraseCredentials();

      final Object principal = authentication.getPrincipal();
      final Object credentials = authentication.getCredentials();

      final GenericUser genericUser = isGenericUser(principal) ? (GenericUser) principal : null;

      if (genericUser == null) {
        result = authentication;
      } else {
        genericUser.eraseCredentials();
        final Collection<GrantedAuthority> authorities = genericUser.getAuthorities() != null ? //
          genericUser.getAuthorities() : ImmutableSet.of();
        result = new PreAuthenticatedAuthenticationToken(principal, credentials, authorities);
      }
    }
    return result;
  }

  public static String rawToken(final Authentication authentication, final TypeSafeToken<GenericUser> token) {
    // TODO find OAuth2Authentication's header Authorization: Bearer?

    final String result;
    if (authentication instanceof PreAuthenticatedAuthenticationToken) {
      final Object principal = authentication.getPrincipal();
      final GenericUser genericUser = isGenericUser(principal) ? (GenericUser) principal : null;
      result = genericUser != null ? (String) authentication.getCredentials() : null;
    } else {
      final GenericUser genericUser = GenericUser.fromPrincipal(authentication);
      result = token.toToken(genericUser);
    }
    return result;
  }

  public static Boolean tokenInSecurityContext() {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication authentication = securityContext.getAuthentication();
    return authentication != null && authentication.isAuthenticated();
  }

  public static String tokenFromSecurityContext(final TypeSafeToken<GenericUser> token) {
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication authentication = securityContext.getAuthentication();

    final String result;
    if (authentication != null && authentication.isAuthenticated()) {
      result = AuthenticationTokens.rawToken(authentication, token);
    } else {
      result = null;
    }

    return result;
  }

  //  public static boolean containsUserInfo(final Authentication authentication) {
  //    final boolean result;
  //    if (authentication == null) {
  //      result = false;
  //    } else if (authentication instanceof OAuth2Authentication) {
  //      final OAuth2Authentication oAuth2Auth = (OAuth2Authentication) authentication;
  //      result = !oAuth2Auth.isClientOnly();
  //    } else if (authentication instanceof AbstractAuthenticationToken) {
  //      // PreAuthenticatedAuthenticationToken
  //      result = isGenericUser(authentication.getPrincipal());
  //    } else {
  //      result = false;
  //    }
  //    return result;
  //  }
}
