package com.yirendai.oss.lib.webmvc.api;

/**
 * Created by zhanghaolun on 16/10/30.
 */
public interface TypeSafeToken<T> {

  String toToken(T obj);

  T fromToken(String token);
}
