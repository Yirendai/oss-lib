package com.yirendai.oss.lib.common.crypto;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public interface KeyGenerator {

  String getSpec();

  KeyExpression generateKey();

  KeyExpression getKey(String spec);
}
