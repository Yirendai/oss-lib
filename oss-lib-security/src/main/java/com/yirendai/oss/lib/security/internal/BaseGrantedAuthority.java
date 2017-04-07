package com.yirendai.oss.lib.security.internal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.Assert;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * Created by Meifans on 17/1/13.
 */
@XmlRootElement(name = "authority")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
@Getter
public class BaseGrantedAuthority implements GrantedAuthority {

  private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  @NonNull
  @XmlValue
  private String authority;

  public BaseGrantedAuthority(String role) {
    Assert.hasText(role, "A granted authority textual representation is required");
    this.authority = role;
  }

  @Override
  public String toString() {
    return this.authority;
  }

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj instanceof BaseGrantedAuthority) {
      return this.authority.equals(((BaseGrantedAuthority) obj).getAuthority());
    }
    if (obj instanceof SimpleGrantedAuthority)
      return this.authority.equals(((SimpleGrantedAuthority) obj).getAuthority());
    return false;
  }

  public int hashCode() {
    return this.authority.hashCode();
  }
}
