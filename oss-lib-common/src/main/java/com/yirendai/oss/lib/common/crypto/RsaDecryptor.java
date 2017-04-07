package com.yirendai.oss.lib.common.crypto;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;

import com.yirendai.oss.lib.common.CodecUtils;

import lombok.Getter;
import lombok.SneakyThrows;

import java.security.Key;
import java.security.Provider;

import javax.crypto.Cipher;

/**
 * Created by zhanghaolun on 16/10/25.
 */
public class RsaDecryptor implements EncodeDecryptor {

  private final Provider provider;
  @Getter
  private final RsaKey key;

  public RsaDecryptor(final Provider provider, final KeyExpression keyExpression) {
    this.provider = provider;
    this.key = new RsaKey(keyExpression);
  }

  @Override
  public String decrypt(final String ciphertext) {
    return decodeAndDecrypt(this.provider, this.key.getRsaPrivateKey(), ciphertext);
  }

  @SneakyThrows
  public static byte[] decryptBytes(final Provider provider, final Key key, final byte[] binary) {
    final Cipher cipher = Cipher.getInstance(CryptoConstants.RSA_ECB_PKCS1_PADDING, provider);
    cipher.init(Cipher.DECRYPT_MODE, key);
    return cipher.doFinal(binary);
  }

  @SneakyThrows
  public static String decodeAndDecrypt(final Provider provider, final Key key, final String ciphertext) {
    checkArgument(key != null, "key must not null");

    final String result;
    if (ciphertext == null) {
      result = null;
    } else if (ciphertext.length() == 0) {
      result = "";
    } else {
      final byte[] binary = CodecUtils.decodeBase64(ciphertext);
      final byte[] decrypted = decryptBytes(provider, key, binary);
      result = new String(decrypted, UTF_8);
    }
    return result;
  }
}
