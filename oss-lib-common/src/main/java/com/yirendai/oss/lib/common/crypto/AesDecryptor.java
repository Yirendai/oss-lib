package com.yirendai.oss.lib.common.crypto;

import static com.yirendai.oss.lib.common.crypto.CryptoConstants.AES_CBC_NOPADDING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.copyOfRange;
import static javax.crypto.Cipher.DECRYPT_MODE;

import com.yirendai.oss.lib.common.CodecUtils;

import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.charset.Charset;
import java.security.Provider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public class AesDecryptor implements EncodeDecryptor {

  @Getter
  private final Charset charset;
  private final Provider provider;
  private final AesCbcKey key;

  public AesDecryptor(final Provider provider, final KeyExpression keyExpression) {
    this.charset = UTF_8;
    this.provider = provider;
    this.key = new AesCbcKey(keyExpression);
  }

  @Override
  public String decrypt(final String encryptedAndEncoded) {
    return new String(this.decryptBytes(encryptedAndEncoded), this.charset);
  }

  public byte[] decryptBytes(final String encryptedAndEncoded) {
    final byte[] unencrypted;
    try {
      // 使用BASE64对密文进行解码
      final byte[] decoded = CodecUtils.decodeBase64(encryptedAndEncoded);
      // 解密
      unencrypted = this.decryptCipher().doFinal(decoded);
    } catch (final IllegalBlockSizeException | BadPaddingException | IllegalArgumentException cause) {
      throw new AesException(AesException.AesError.DECRYPT_AES_ERROR, cause);
    }
    try {
      // 去除补位字符
      final byte[] unpad = Pkcs7Encoder.PKCS7_UTF8_BLOCK32.decode(unencrypted);
      return copyOfRange(unpad, 16, unpad.length);
    } catch (final IllegalArgumentException cause) {
      throw new AesException(AesException.AesError.ILLEGAL_BUFFER, cause);
    }
  }

  @SneakyThrows
  Cipher decryptCipher() {
    final Cipher cipher = Cipher.getInstance(AES_CBC_NOPADDING, this.provider);// 设置解密模式为AES的CBC模式
    cipher.init(DECRYPT_MODE, this.key.getKeySpec(), this.key.getCbcIv());
    return cipher;
  }
}
