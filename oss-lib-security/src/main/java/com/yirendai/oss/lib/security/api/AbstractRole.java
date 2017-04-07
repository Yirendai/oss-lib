package com.yirendai.oss.lib.security.api;

import static com.yirendai.oss.lib.security.api.Security.ROLE_PREFIX;

/**
 * Created by zhanghaolun on 16/10/28.
 */
public abstract class AbstractRole extends AbstractGrantedAuthority {

  @Override
  public String getAuthority() {
    return ROLE_PREFIX + this.getName();
  }

  public String getDescription() {
    return this.getAuthority();
  }

  public abstract String getName();
}
