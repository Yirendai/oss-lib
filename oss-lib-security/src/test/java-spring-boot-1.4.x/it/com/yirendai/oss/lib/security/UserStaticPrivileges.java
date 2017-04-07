package it.com.yirendai.oss.lib.security;

import com.yirendai.oss.lib.security.api.StaticPrivilege;

/**
 * Created by zhanghaolun on 16/10/29.
 */
public enum UserStaticPrivileges implements StaticPrivilege<UserStaticPrivileges> {

  PRIVILEGE_DATA_VIEW(UserStaticPrivileges.RESOURCE_DATA, UserStaticPrivileges.ACTION_VIEW),
  PRIVILEGE_DATA_EDIT(UserStaticPrivileges.RESOURCE_DATA, UserStaticPrivileges.ACTION_EDIT);

  public static final String RESOURCE_DATA = "DATA";

  public static final String ACTION_VIEW = "VIEW";
  public static final String ACTION_EDIT = "EDIT";

  private final String resource;
  private final String action;

  UserStaticPrivileges(final String resource, final String action) {
    this.resource = resource;
    this.action = action;
  }

  @Override
  public String getAction() {
    return this.action;
  }

  @Override
  public String getResource() {
    return this.resource;
  }
}
