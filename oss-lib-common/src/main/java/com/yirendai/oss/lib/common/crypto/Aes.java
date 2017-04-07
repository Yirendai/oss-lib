package com.yirendai.oss.lib.common.crypto;

import lombok.Getter;

import java.security.Provider;

public class Aes implements EncodeCipher {

  @Getter
  private AesEncryptor encryptor;
  @Getter
  private AesDecryptor decryptor;

  public Aes(final AesEncryptor encryptor, final AesDecryptor decryptor) {
    this.encryptor = encryptor;
    this.decryptor = decryptor;
  }

  public Aes(final Provider provider, final KeyExpression keyExpression) {
    this( //
      new AesEncryptor(provider, keyExpression), //
      new AesDecryptor(provider, keyExpression) //
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
