package com.yirendai.oss.lib.common.crypto;

import static com.yirendai.oss.lib.common.crypto.CryptoConstants.AES_CBC_NOPADDING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.crypto.Cipher.ENCRYPT_MODE;

import com.yirendai.oss.lib.common.CodecUtils;
import com.yirendai.oss.lib.common.crypto.RandomString.RandomStrings;

import lombok.Getter;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.Provider;

import javax.crypto.Cipher;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public class AesEncryptor implements EncodeEncryptor {

  @Getter
  private final Charset charset;
  private final Provider provider;
  private final AesCbcKey key;

  public AesEncryptor(final Provider provider, final KeyExpression keyExpression) {
    this.charset = UTF_8;
    this.provider = provider;
    this.key = new AesCbcKey(keyExpression);
  }

  @Override
  public String encrypt(final String plainText) {
    return this.encryptBytes( //
      RandomStrings.RANDOM_BASE62.generate(this.key.getCbcIvLength()), //
      plainText.getBytes(this.charset) //
    );
  }

  @Override
  public String encrypt(final String plainText, final Integer maxAge) {
    throw new UnsupportedOperationException();
  }

  public String encryptBytes(final String random, final byte[] plainBytes) {
    final byte[] randomBytes = random.getBytes(this.charset);
    final int nopadLength = randomBytes.length + plainBytes.length;
    // ... + pad: 使用自定义的填充方式对明文进行补位填充
    final byte[] padBytes = Pkcs7Encoder.PKCS7_UTF8_BLOCK32.encode(nopadLength);

    // 获得最终的字节流, 未加密
    // random + plain + pad
    final ByteBuffer unencrypted = ByteBuffer.allocate(nopadLength + padBytes.length);
    unencrypted.put(randomBytes);
    unencrypted.put(plainBytes);
    unencrypted.put(padBytes);
    try {
      // 加密
      final byte[] encrypted = this.encryptCipher().doFinal(unencrypted.array());
      // 使用BASE64对加密后的字符串进行编码
      return CodecUtils.encodeBase64(encrypted);
    } catch (final Exception cause) {
      throw new AesException(AesException.AesError.ENCRYPT_AES_ERROR, cause);
    } finally {
      unencrypted.clear();
    }
  }

  @SneakyThrows
  Cipher encryptCipher() { // arg1: "BC" 指定使用 bouncycastle provider, 不指定则使用jre
    final Cipher cipher = Cipher.getInstance(AES_CBC_NOPADDING, this.provider);// 设置加密模式为AES的CBC模式
    cipher.init(ENCRYPT_MODE, this.key.getKeySpec(), this.key.getCbcIv());
    return cipher;
  }
}
