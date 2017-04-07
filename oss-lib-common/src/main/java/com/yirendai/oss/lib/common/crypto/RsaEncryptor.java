package com.yirendai.oss.lib.common.crypto;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;

import com.yirendai.oss.lib.common.CodecUtils;

import lombok.Getter;
import lombok.SneakyThrows;

import java.security.Provider;
import java.security.PublicKey;

import javax.crypto.Cipher;

/**
 * Created by zhanghaolun on 16/10/25.
 */
public class RsaEncryptor implements EncodeEncryptor {

  private final Provider provider;
  @Getter
  private final RsaKey key;

  public RsaEncryptor(final Provider provider, final KeyExpression keyExpression) {
    this.provider = provider;
    this.key = new RsaKey(keyExpression);
  }

  @Override
  public String encrypt(final String plaintext) {
    return encryptAndEncode(this.provider, this.key.getRsaPublicKey(), plaintext);
  }

  @Override
  public String encrypt(final String plainText, final Integer maxAge) {
    throw new UnsupportedOperationException();
  }

  @SneakyThrows
  public static byte[] encryptBytes(final Provider provider, final PublicKey publicKey, final byte[] binary) {
    final Cipher cipher = Cipher.getInstance(CryptoConstants.RSA_ECB_PKCS1_PADDING, provider);
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    return cipher.doFinal(binary);
  }

  public static String encryptAndEncode(final Provider provider, final PublicKey publicKey, final String plaintext) {
    checkArgument(publicKey != null, "publicKey must not null");
    final String result;
    if (plaintext == null) {
      result = null;
    } else if (plaintext.length() == 0) {
      result = "";
    } else {
      final byte[] encrypted = encryptBytes(provider, publicKey, plaintext.getBytes(UTF_8));
      result = CodecUtils.encodeBase64(encrypted);
    }
    return result;
  }
}
