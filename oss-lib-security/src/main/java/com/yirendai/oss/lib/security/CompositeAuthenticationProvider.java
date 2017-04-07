package com.yirendai.oss.lib.security;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;
import java.util.Optional;

/**
 * Created by zhanghaolun on 16/7/6.
 */
public class CompositeAuthenticationProvider implements AuthenticationProvider {

  private List<AuthenticationProvider> delegates = ImmutableList.of();

  public void setDelegates(final List<AuthenticationProvider> delegates) {
    Preconditions.checkArgument(delegates != null, "delegates must not null");
    this.delegates = delegates;
  }

  AuthenticationProvider findAuthenticationProviderFor(final Authentication authentication) {
    return authentication != null ? this.findAuthenticationProviderFor(authentication.getClass()) : null;
  }

  AuthenticationProvider findAuthenticationProviderFor(final Class<?> authentication) {
    final AuthenticationProvider found;
    if (authentication != null) {
      final Optional<AuthenticationProvider> optional =
        this.delegates.stream().filter(provider -> provider.supports(authentication)).findFirst();
      found = optional.isPresent() ? optional.get() : null;
    } else {
      found = null;
    }
    return found;
  }

  @Override
  public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
    final AuthenticationProvider authenticationProvider = this.findAuthenticationProviderFor(authentication);
    return authenticationProvider != null ? authenticationProvider.authenticate(authentication) : null;
  }

  @Override
  public boolean supports(final Class<?> authentication) {
    return this.findAuthenticationProviderFor(authentication) != null;
  }
}
