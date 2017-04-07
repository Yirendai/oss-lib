package com.yirendai.oss.lib.common.crypto;

import lombok.Getter;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public class Jwt implements EncodeCipher {

  @Getter
  private final JwtEncryptor encryptor;
  @Getter
  private final JwtDecryptor decryptor;

  public Jwt(final JwtEncryptor encryptor, final JwtDecryptor decryptor) {
    this.encryptor = encryptor;
    this.decryptor = decryptor;
  }

  public Jwt(final KeyExpression keyExpression) {
    this( //
      new JwtEncryptor(keyExpression), //
      new JwtDecryptor(keyExpression) //
    );
  }

  @Override
  public String encrypt(final String plainText) {
    return this.encryptor.encrypt(plainText);
  }

  @Override
  public String encrypt(final String plainText, final Integer maxAge) {
    return this.encryptor.encrypt(plainText, maxAge);
  }

  @Override
  public String decrypt(final String encryptedAndEncoded) {
    return this.decryptor.decrypt(encryptedAndEncoded);
  }
}
