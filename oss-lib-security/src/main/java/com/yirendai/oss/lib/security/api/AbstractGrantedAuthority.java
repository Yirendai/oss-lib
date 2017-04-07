package com.yirendai.oss.lib.security.api;

import org.springframework.security.core.GrantedAuthority;

import java.util.Comparator;

/**
 * Created by haolun on 17/1/4.
 */
public abstract class AbstractGrantedAuthority implements GrantedAuthority, Comparable<GrantedAuthority> {

  public static final Comparator<GrantedAuthority> COMPARATOR = (lhs, rhs) -> {
    final int result;
    if (lhs != null && rhs != null) {
      result = lhs.getAuthority().compareTo(rhs.getAuthority());
    } else if (lhs != null) { // rhs == null
      result = 1;
    } else { // lhs == null && rhs != null
      result = -1;
    }
    return result;
  };

  @Override
  public int compareTo(final GrantedAuthority rhs) {
    return AbstractGrantedAuthority.COMPARATOR.compare(this, rhs);
  }

  @Override
  public boolean equals(final Object obj) {
    final boolean result;
    if (obj != null) {
      if (this.getClass() == obj.getClass() || GrantedAuthority.class.isAssignableFrom(obj.getClass())) {
        final GrantedAuthority rhs = (GrantedAuthority) obj;
        result = this.getAuthority().equals(rhs.getAuthority());
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
    return this.getAuthority().hashCode();
  }

  @Override
  public String toString() {
    return this.getAuthority();
  }
}
