package com.yirendai.oss.lib.common.crypto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.security.Provider;

/**
 * Created by zhanghaolun on 16/7/8.
 */
@Slf4j
public class Rsa implements EncodeCipher {

  @Getter
  private final RsaEncryptor encryptor;
  @Getter
  private final RsaDecryptor decryptor;

  public Rsa(final RsaEncryptor encryptor, final RsaDecryptor decryptor) {
    this.encryptor = encryptor;
    this.decryptor = decryptor;
  }

  public Rsa(final Provider provider, final KeyExpression keyExpression) {
    this( //
      new RsaEncryptor(provider, keyExpression), //
      new RsaDecryptor(provider, keyExpression) //
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
