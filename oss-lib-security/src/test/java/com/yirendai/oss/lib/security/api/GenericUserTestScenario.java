package com.yirendai.oss.lib.security.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import com.yirendai.oss.lib.common.Defaults;
import com.yirendai.oss.lib.security.internal.BaseGrantedAuthority;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.joda.time.DateTime;

/**
 * Created by Meifans on 17/1/12.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class GenericUserTestScenario {

  static GenericUser genericUserScenario() {
    return GenericUser.genericUserBuilder()
      .properties(ImmutableMap.of("tag", "one"))
      .authorities(ImmutableSet.of(new BaseGrantedAuthority("role")))
      .timestamp(new DateTime("2017-01-17T15:48:23.746+08:00", Defaults.UTC_P8))
      .username(GenericUser.toUsername("type", "id", "meifans"))
      .uuid("uuid")
      .password("password").build();
  }
}
