package com.yirendai.oss.lib.security.api;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

/**
 * Created by zhanghaolun on 16/10/28.
 */
public abstract class AbstractUser implements User {

  /**
   * default user type.
   */
  public static final String USER_TYPE_USER = "UT_USER";

  public void disable() {
    this.setEnabled(false);
  }

  @Override
  public void enable() {
    this.setEnabled(true);
  }

  @Override
  public void eraseCredentials() {
    this.setPassword(null);
  }

  @Override
  public Map<String, String> getProperties() {
    return ImmutableMap.of();
  }

  @Override
  public String getType() {
    return USER_TYPE_USER;
  }

  @Override
  public String getUsername() {
    return this.getName();
  }

  @JsonIgnore
  @Override
  public boolean isAccountNonExpired() {
    return this.isEnabled();
  }

  @JsonIgnore
  @Override
  public boolean isAccountNonLocked() {
    return this.isEnabled();
  }

  @JsonIgnore
  @Override
  public boolean isCredentialsNonExpired() {
    return this.isEnabled();
  }

  protected abstract void setEnabled(boolean enabled);

  protected abstract void setPassword(String password);

  @Override
  public boolean equals(final Object obj) {
    final boolean result;
    if (obj != null) {
      if (this.getClass() == obj.getClass() || AbstractUser.class.isAssignableFrom(obj.getClass())) {
        final AbstractUser rhs = (AbstractUser) obj;
        result = this.getName().equals(rhs.getName());
      } else {
        result = false;
      }
    } else {
      result = false;
    }
    return result;
  }

  @Override
  public int hashCode() {
    return this.getName().hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this.getClass()) //
      .add("name", this.getName()) //
      .add("enabled", this.isEnabled()) //
      .toString();
  }
}
