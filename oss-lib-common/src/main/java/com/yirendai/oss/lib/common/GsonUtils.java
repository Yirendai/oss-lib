package com.yirendai.oss.lib.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * TODO remove all SimpleGrantedAuthority.
 * Created by Meifans on 17/1/9.
 */
public final class GsonUtils {

  public static Gson setup(final GsonBuilder builder) {
    return builder
      .registerTypeAdapter(GrantedAuthority.class, getAuthoritySerializer())
      .registerTypeAdapter(GrantedAuthority.class, getAuthorityDeSerializer()).create();
  }

  private static JsonDeserializer<GrantedAuthority> getAuthorityDeSerializer() {
    return (json, typeOfT, context) -> //
      new SimpleGrantedAuthority(json.getAsString());
  }

  private static JsonSerializer<GrantedAuthority> getAuthoritySerializer() {
    return (authority, typeOfSrc, context) -> new JsonPrimitive(authority.getAuthority());
  }
}
