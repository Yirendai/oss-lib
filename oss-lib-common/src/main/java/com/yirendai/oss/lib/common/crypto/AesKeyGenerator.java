package com.yirendai.oss.lib.common.crypto;

import static com.google.common.base.Preconditions.checkArgument;
import static com.yirendai.oss.lib.common.crypto.RandomString.RandomStrings.RANDOM_BASE62;

import lombok.Getter;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public class AesKeyGenerator implements KeyGenerator {

  @Getter
  private final String spec;
  private KeyExpression key;

  public AesKeyGenerator(final String spec) {
    this.spec = spec;
  }

  @Override
  public KeyExpression generateKey() {
    this.key = AesKeyGenerator.generateAesKey(this.spec);
    return this.key;
  }

  @Override
  public KeyExpression getKey(final String spec) {
    checkArgument(this.spec.equals(spec), "spec " + spec + " not supported.");
    if (this.key == null) {
      this.generateKey();
    }
    return this.key;
  }

  public static KeyExpression generateAesKey(final String spec) {
    final int keySize = AesCbcKey.keySize(spec);
    final String value;
    if (keySize == 256) {
      value = RANDOM_BASE62.generate(43) + "="; // 32bytes after decoded
    } else {
      throw new IllegalArgumentException("unsupported spec " + spec);
    }
    return new KeyExpression(spec, value);
  }
}
