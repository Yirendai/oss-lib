package com.yirendai.oss.lib.security.api;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.Set;

public interface User extends UserDetails, CredentialsContainer {

  Set<GrantedAuthority> getAuthorities();

  String getId();

  String getName();

  @JsonIgnore
  String getType();

  @JsonIgnore
  Map<String, String> getProperties();

  void enable();
}
