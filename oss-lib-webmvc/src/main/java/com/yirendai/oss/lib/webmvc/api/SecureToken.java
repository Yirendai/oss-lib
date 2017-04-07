package com.yirendai.oss.lib.webmvc.api;

import static com.google.common.base.Charsets.UTF_8;

import com.yirendai.oss.lib.common.CodecUtils;
import com.yirendai.oss.lib.common.crypto.EncodeCipher;

/**
 * Created by zhanghaolun on 16/10/30.
 */
public class SecureToken<T> implements TypeSafeToken<T> {

  private final TypeSafeToken<T> token;
  private final EncodeCipher cipher;

  public SecureToken(final TypeSafeToken<T> token, final EncodeCipher cipher) {
    this.token = token;
    this.cipher = cipher;
  }

  private static String encrypt(final String value, final EncodeCipher cipher) {
    final String result;
    if (value == null) {
      result = null;
    } else {
      if (cipher == null) {
        result = CodecUtils.encodeBase64(value.getBytes(UTF_8));
      } else {
        result = cipher.encrypt(value);
      }
    }
    return result;
  }

  private static String decrypt(final String value, final EncodeCipher cipher) {
    final String result;
    if (value == null) {
      result = null;
    } else {
      if (cipher == null) {
        result = new String(CodecUtils.decodeBase64(value), UTF_8);
      } else {
        result = cipher.decrypt(value);
      }
    }
    return result;
  }

  @Override
  public String toToken(final T obj) {
    final String token = this.token.toToken(obj);
    return encrypt(token, this.cipher);
  }

  @Override
  public T fromToken(final String encrypted) {
    final String token = decrypt(encrypted, this.cipher);
    return this.token.fromToken(token);
  }
}
