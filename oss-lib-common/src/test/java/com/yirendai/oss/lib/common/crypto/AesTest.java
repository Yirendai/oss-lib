package com.yirendai.oss.lib.common.crypto;

import static org.junit.Assert.assertEquals;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;

@Slf4j
public class AesTest {

  private Aes aes;

  @Before
  public void setUp() {
    final KeyExpression keyExpression = new AesKeyGenerator(AesCbcKey.keySpec(256)).generateKey();
    log.info("keyExpression: {}", keyExpression.toString());
    this.aes = new Aes(Cryptos.provider(), keyExpression);
  }

  @Test
  public void testAes() {
    final String plainText = "文本";
    final String encryptAndEncoded = this.aes.getEncryptor().encryptBytes( //
      RandomString.RandomStrings.RANDOM_BASE62.generate(16), //
      plainText.getBytes(this.aes.getEncryptor().getCharset()));
    log.info("encryptAndEncoded: {}", encryptAndEncoded);
    final byte[] plainBytes = this.aes.getDecryptor().decryptBytes(encryptAndEncoded);
    final String decryptedPlainText = new String(plainBytes, this.aes.getDecryptor().getCharset());
    log.info("decryptedPlainText: {}", decryptedPlainText);
    assertEquals(plainText, decryptedPlainText);
  }
}
