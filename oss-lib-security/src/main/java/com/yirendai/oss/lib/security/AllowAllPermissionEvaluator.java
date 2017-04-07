package com.yirendai.oss.lib.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

/**
 * Created by zhanghaolun on 16/7/14.
 */
public class AllowAllPermissionEvaluator implements PermissionEvaluator {

  @Override
  public boolean hasPermission(final Authentication authentication, final Object targetDomainObject,
      final Object permission) {
    return true;
  }

  @Override
  public boolean hasPermission(final Authentication authentication, final Serializable targetId,
      final String targetType, final Object permission) {
    return true;
  }
}
