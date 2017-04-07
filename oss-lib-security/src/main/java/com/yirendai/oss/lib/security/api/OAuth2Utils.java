package com.yirendai.oss.lib.security.api;

import static java.util.stream.Collectors.toSet;

import com.google.common.collect.ImmutableMap;

import com.yirendai.oss.lib.common.Defaults;
import com.yirendai.oss.lib.security.internal.BaseGrantedAuthority;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhanghaolun on 16/11/22.
 */
abstract class OAuth2Utils {

  private OAuth2Utils() {
  }

  static Boolean isOAuth2Authentication(final Principal principal) {
    return principal instanceof OAuth2Authentication;
  }

  static GenericUser fromOAuth2Authentication(final Principal principal) {
    final OAuth2Authentication oauth2Authentication = (OAuth2Authentication) principal;

    final Authentication auth = oauth2Authentication.getUserAuthentication();
    if (oauth2Authentication.isClientOnly() || auth == null || auth.getDetails() == null) {
      return null;
    }

    final boolean accountNonExpired = true;
    final boolean accountNonLocked = true;
    @SuppressWarnings("unchecked")
    final Map<String, Object> details = (Map<String, Object>) auth.getDetails();
    @SuppressWarnings("unchecked")
    final Set<GrantedAuthority> authorities =
      ((Collection<Map<String, String>>) details.get("authorities")).stream()
        .map(authority -> new BaseGrantedAuthority(authority.get("authority")))
        .collect(toSet());
    final boolean credentialsNonExpired = true;
    final boolean enabled = true;
    final String password = System.getProperty("blankPassword", "");
    final String username = auth.getPrincipal().toString();
    final String uuid = ((OAuth2AuthenticationDetails) oauth2Authentication.getDetails()).getTokenValue();

    return new GenericUser(accountNonExpired, accountNonLocked, authorities, credentialsNonExpired,
      enabled, password, username, ImmutableMap.of(), // TODO pass properties ?
      Defaults.now(), uuid);
  }
}
