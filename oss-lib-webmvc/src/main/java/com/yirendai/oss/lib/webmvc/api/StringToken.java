package com.yirendai.oss.lib.webmvc.api;

/**
 * Created by zhanghaolun on 16/11/1.
 */
public class StringToken implements TypeSafeToken<String> {

  @Override
  public String toToken(final String obj) {
    return obj;
  }

  @Override
  public String fromToken(final String token) {
    return token;
  }
}
