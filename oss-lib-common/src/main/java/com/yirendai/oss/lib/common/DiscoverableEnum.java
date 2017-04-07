package com.yirendai.oss.lib.common;

import com.fasterxml.jackson.annotation.JsonValue;

public interface DiscoverableEnum<T extends Enum<T> & DiscoverableEnum<T>> {

  @JsonValue
  String getText();
}
