package com.yirendai.oss.lib.webmvc.api;

import static com.google.common.base.Preconditions.checkNotNull;

import com.yirendai.oss.lib.common.crypto.Jwt;

import lombok.Getter;

/**
 * Created by zhanghaolun on 16/10/29.
 */
public class JsonWebToken<T> implements TypeSafeToken<T> {

  private final TypeSafeToken<T> token;
  @Getter
  private final Jwt jwt;
  /**
   * Maximum age of the jwt token in seconds.
   */
  @Getter
  private final Integer maxAge;

  public JsonWebToken( //
    final TypeSafeToken<T> token, //
    final Jwt jwt, //
    final int maxAge //
  ) {
    this.token = checkNotNull(token);
    this.jwt = jwt;
    this.maxAge = maxAge > 0 ? maxAge : 0;
  }

  @Override
  public String toToken(final T obj) {
    final String token = this.token.toToken(obj);
    return this.jwt.encrypt(token, this.maxAge);
  }

  @Override
  public T fromToken(final String compactJws) {
    final String token = this.jwt.decrypt(compactJws);
    return this.token.fromToken(token);
  }
}
