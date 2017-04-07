package it.com.yirendai.oss.lib.security;

import static it.com.yirendai.oss.lib.security.UserRole.ROLE_ADMIN;
import static it.com.yirendai.oss.lib.security.UserRole.ROLE_ADMIN_AUTHORITY;
import static it.com.yirendai.oss.lib.security.UserRole.ROLE_USER;
import static it.com.yirendai.oss.lib.security.UserRole.ROLE_USER_AUTHORITY;
import static it.com.yirendai.oss.lib.security.UserStaticPrivileges.PRIVILEGE_DATA_EDIT;
import static it.com.yirendai.oss.lib.security.UserStaticPrivileges.PRIVILEGE_DATA_VIEW;
import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import lombok.NoArgsConstructor;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhanghaolun on 16/10/28.
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class UserRolePrivileges {

  static final Map<String, Set<GrantedAuthority>> ROLE_PRIVILEGE_MAP;

  static {
    ROLE_PRIVILEGE_MAP = ImmutableMap.of( //
      ROLE_ADMIN, ImmutableSet.of( //
        ROLE_ADMIN_AUTHORITY, //
        PRIVILEGE_DATA_EDIT, //
        PRIVILEGE_DATA_VIEW //
      ), //
      ROLE_USER, ImmutableSet.of( //
        ROLE_USER_AUTHORITY, //
        PRIVILEGE_DATA_VIEW //
      )
    );
  }

  public static Set<GrantedAuthority> getAuthorities(final Set<String> roles) {
    return roles.stream().map(UserRolePrivileges::getAuthorities).flatMap(Collection::stream).collect(toSet());
  }

  public static Set<GrantedAuthority> getAuthorities(final String role) { // Privileges
    return ROLE_PRIVILEGE_MAP.get(role);
  }
}
