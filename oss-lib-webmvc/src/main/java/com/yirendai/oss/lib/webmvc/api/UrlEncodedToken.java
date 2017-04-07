package com.yirendai.oss.lib.webmvc.api;

import static com.google.common.base.Preconditions.checkNotNull;

import com.yirendai.oss.lib.common.CodecUtils;

/**
 * Created by zhanghaolun on 16/11/14.
 */
public class UrlEncodedToken<T> implements TypeSafeToken<T> {

  private final TypeSafeToken<T> delegate;

  public UrlEncodedToken(final TypeSafeToken<T> delegate) {
    this.delegate = checkNotNull(delegate, "delegate must not null");
  }

  @Override
  public String toToken(final T obj) {
    final String token = this.delegate.toToken(obj);
    return CodecUtils.urlEncode(token);
  }

  @Override
  public T fromToken(final String token) {
    final String decoded = CodecUtils.urlDecode(token);
    return this.delegate.fromToken(decoded);
  }
}
