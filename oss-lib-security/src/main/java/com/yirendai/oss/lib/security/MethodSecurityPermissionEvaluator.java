package com.yirendai.oss.lib.security;

import com.yirendai.oss.boot.autoconfigure.AppUtils;
import com.yirendai.oss.lib.common.DiscoverableEnums;
import com.yirendai.oss.lib.security.api.GenericUser;
import com.yirendai.oss.lib.security.api.StaticPrivilege;
import com.yirendai.oss.lib.security.internal.BaseGrantedAuthority;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * Created by zhanghaolun on 16/7/13.
 */
@Slf4j
public class MethodSecurityPermissionEvaluator implements PermissionEvaluator {

  public static final String HAS_PERMISSION_3 =
    "permissionEvaluator hasPermission(authentication, targetDomainObject, permission)";
  public static final String HAS_PERMISSION_4 =
    "permissionEvaluator hasPermission(authentication, targetId, targetType, permission)";

  private Boolean securityEnabled;

  private PermissionEvaluator projectPermissionEvaluator;
  @SuppressWarnings("rawtypes")
  private DiscoverableEnums<? extends StaticPrivilege> staticPrivileges;

  @SuppressWarnings("unchecked")
  public void setProjectPermissionEvaluator(final PermissionEvaluator projectPermissionEvaluator) {
    final String basePackage = AppUtils.appBasePackage(projectPermissionEvaluator.getClass().getName().split("\\.")[0]);
    this.staticPrivileges = DiscoverableEnums.discoverableEnums(basePackage, StaticPrivilege.class);
    this.projectPermissionEvaluator = projectPermissionEvaluator;
  }

  boolean hasStaticPermission( //
    final GenericUser genericUser, //
    final String targetDomainObject, //
    final String permission //
  ) {
    final String privilege = StaticPrivilege.toAuthority(targetDomainObject, permission);
    @SuppressWarnings("rawtypes")
    final StaticPrivilege found = this.staticPrivileges.convert(privilege);

    return genericUser != null && //
      genericUser.getAuthorities() != null && //
      genericUser.getAuthorities().contains(new BaseGrantedAuthority(found.getAuthority()));
  }

  @Override
  public boolean hasPermission(final Authentication authentication, final Object targetDomainObject,
    final Object permission) {
    final boolean hasPermission;
    if (!this.securityEnabled) {
      log.info("{} securityEnabled not enabled, returning true", HAS_PERMISSION_3);
      hasPermission = true;
    } else if (authentication == null) {
      log.warn("{} authentication is null, returning false", HAS_PERMISSION_3);
      hasPermission = false;
    } else if (targetDomainObject == null) {
      log.warn("{} targetDomainObject is null, returning false", HAS_PERMISSION_3);
      hasPermission = false;
    } else if (permission == null) {
      log.warn("{} permission is null, returning false", HAS_PERMISSION_3);
      hasPermission = false;
    } else {
      final GenericUser genericUser = GenericUser.fromPrincipal(authentication);
      if (String.class.isAssignableFrom(targetDomainObject.getClass())
        && String.class.isAssignableFrom(permission.getClass())) {
        hasPermission = this.hasStaticPermission(genericUser, targetDomainObject.toString(), permission.toString());
      } else {
        hasPermission = this.projectPermissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
      }
    }
    return hasPermission;
  }

  @Override
  public boolean hasPermission(final Authentication authentication, final Serializable targetId,
    final String targetType, final Object permission) {
    final boolean hasPermission;
    if (!this.securityEnabled) {
      log.info("{} securityEnabled not enabled, returning true", HAS_PERMISSION_4);
      hasPermission = true;
    } else {
      // Not required here.
      log.warn("{} not implemented, returning false", HAS_PERMISSION_4);
      hasPermission = false;
    }
    return hasPermission;
  }

  public void setSecurityEnabled(final Boolean securityEnabled) {
    this.securityEnabled = securityEnabled;
  }
}
