package com.yirendai.oss.lib.security.api;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public abstract class Security {

  public static final String AUTHORITY_PREFIX = "AUTHORITY_";
  public static final String ROLE_PREFIX = "ROLE_";

  public static final String HEADER_AUTH_TOKEN = "X-Auth-Token"; // X-AuthenticationToken

  private Security() {
  }

  public static boolean authenticationIsRequired() {
    // Only reauthenticate if username doesn't match SecurityContextHolder and user isn't
    // authenticated
    // (see SEC-53)
    final SecurityContext securityContext = SecurityContextHolder.getContext();
    final Authentication existingAuth = securityContext.getAuthentication();

    if (existingAuth == null || !existingAuth.isAuthenticated()) {
      return true;
    }

    // Limit username comparison to providers which use usernames (ie
    // UsernamePasswordAuthenticationToken, PreAuthenticatedAuthenticationToken)
    // (see SEC-348)
    if (existingAuth instanceof PreAuthenticatedAuthenticationToken) {
      final GenericUser unknown_user = GenericUser.unknownUser();
      if (!existingAuth.getName().equals(unknown_user.getUsername())) {
        return true;
      }
    }

    // Handle unusual condition where an AnonymousAuthenticationToken is already present
    // This shouldn't happen very often, as BasicProcessingFitler is meant to be earlier in the
    // filter
    // chain than AnonymousAuthenticationFilter. Nevertheless, presence of both an
    // AnonymousAuthenticationToken
    // together with a BASIC authentication request header should indicate reauthentication using
    // the
    // BASIC protocol is desirable. This behaviour is also consistent with that provided by form and
    // digest,
    // both of which force re-authentication if the respective header is detected (and in doing so
    // replace
    // any existing AnonymousAuthenticationToken). See SEC-610.
    return existingAuth instanceof AnonymousAuthenticationToken;
  }
}
