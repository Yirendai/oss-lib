package com.yirendai.oss.lib.common.crypto;

import static com.yirendai.oss.lib.common.StringUtils.dropComment;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.COLON;
import static com.yirendai.oss.lib.common.crypto.RsaKey.COMMENT_MARK;
import static com.yirendai.oss.lib.common.crypto.RsaKey.KEY_FORMAT_PKCS1_X509;
import static com.yirendai.oss.lib.common.crypto.RsaKey.KEY_TYPE_PAIR;
import static com.yirendai.oss.lib.common.crypto.RsaKey.extractPrivateKey;
import static com.yirendai.oss.lib.common.crypto.RsaKey.extractPublicKey;
import static org.junit.Assert.assertEquals;

import com.yirendai.oss.lib.common.ResourceUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by zhanghaolun on 16/7/9.
 */
@Slf4j
public class RsaTest {

  private String pkcs1;
  private String pkcs8;
  private String x509;
  private String loginRsaKey;

  @Before
  public void setUp() throws IOException {
    this.pkcs1 = dropComment(ResourceUtils.classPathString("rsa/RSA1024_PRIV_PKCS1.pem"), COMMENT_MARK);
    this.pkcs8 = dropComment(ResourceUtils.classPathString("rsa/RSA1024_PRIV_PKCS8.pem"), COMMENT_MARK);
    this.x509 = dropComment(ResourceUtils.classPathString("rsa/RSA1024_PUB_X509.pem"), COMMENT_MARK);
    // log.info("pkcs1: \n{}", this.pkcs1);
    // log.info("pkcs8: \n{}", this.pkcs8);
    // log.info("x509: \n{}", this.x509);

    this.loginRsaKey = this.pkcs1 + COLON + this.x509;
    log.info("loginRsaKey: {}", this.loginRsaKey);
  }

  @Test
  public void testParseKey() {
    log.info("keypair: {}", dropComment(this.pkcs1, COMMENT_MARK) + ":" + dropComment(this.x509, COMMENT_MARK));

    final String spec = RsaKey.keySpec(KEY_FORMAT_PKCS1_X509, 1024, KEY_TYPE_PAIR);
    final RsaKey rsaKey = new RsaKey(new KeyExpression(spec, this.loginRsaKey));
    log.info("pkcs1RsaPrivateKey: {}", rsaKey.getEncodedRsaPrivateKey());
    assertEquals(rsaKey.getPrivateKey().getValue(), extractPrivateKey(rsaKey.getKeyExpression()));
    assertEquals(rsaKey.getPrivateKey().getValue(), this.pkcs1);
    assertEquals(rsaKey.getEncodedRsaPublicKey(), rsaKey.getPublicKey().getValue());
    assertEquals(rsaKey.getEncodedRsaPublicKey(), extractPublicKey(rsaKey.getKeyExpression()));
    assertEquals(rsaKey.getEncodedRsaPublicKey(), this.x509);

    final String[] keyPair = StringUtils.split(this.loginRsaKey, ':');
    final String privateKey = keyPair[0];
    assertEquals(dropComment(this.pkcs1, COMMENT_MARK), privateKey);
    final String publicKey = keyPair[1];
    assertEquals(dropComment(this.x509, COMMENT_MARK), publicKey);
  }

  @Test
  @SneakyThrows
  public void testDecodeAndDecrypt() {
    final String spec = RsaKey.keySpec(KEY_FORMAT_PKCS1_X509, 1024, KEY_TYPE_PAIR);
    this.testDecodeAndDecrypt(new KeyExpression(spec, this.loginRsaKey));
    this.testDecodeAndDecrypt(new KeyExpression(spec, "MIICXAIBAAKBgQC41p0ZrQAu9tWp4imAEYgeaiwjorlGdFQRdl1Gr/i/lLNrBGT6HqmatmYyVACf0b4ZO/ig6NZsiPV6bCzGzG//jlruZcYs1T2bc2QocdWy/mlKp22qIpsdmhQIUleE7FLQcZXrMfmK+h97DSeJWeRKO6Yebw/1hygvDLi4aiURNwIDAQABAoGAHm0Vhzh3I0P2f/R+qFo6cedARM1weiyD+ivlxtEIvWCY+DDM/m81tAX6ZKZ3Q9ee10CL+NDbGxqgbfqIlGDqvnrn5z1EfyEeB0yd+L+xo3LWv7BlNVOVvHO852fGGjau1FmKXddMetyiUyWJ0JleMUAc5mqEBPkF/pPDdEbY9kECQQDfngPAZ8q0XQ4EJIaKDhx58Mux2qAkZjuPZoCriaVEwfancVbNAQO9qIhCNxPFZHsypNJqgqwgtz8yIeucwQJvAkEA05r6qOMSjAPCDXNU4rE1BePKcPMOuF4T1wJQZFL/6KgIxDGA5XATmMs+KaWchEvrjS2QkGgXEVJ6HGpgWUYhuQJBAIALhExtYmYLDtaSb7OnxuiQWKM79A1DuQExCaawB8Gqa2mP/csftJuqYHPCBAE4u07RkGdK8fM92uMDB15YKuMCQBSrtQS+eddXnqM1uvsSGH3kzKfP9oYoGoQMY9IJEZqmn4Sa2V6QXjm00PO8i9eYmwAFjXOt12MHzYntElBmO3ECQDD30Z4AY5m29xzGyUQhXwL4Qn/eNkjHvqXQDiQEfP8EDhaiwL9hHnsJGNqJ7xIPJWNxwX6YMi64Gi61b0fwBkc=:MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC41p0ZrQAu9tWp4imAEYgeaiwjorlGdFQRdl1Gr/i/lLNrBGT6HqmatmYyVACf0b4ZO/ig6NZsiPV6bCzGzG//jlruZcYs1T2bc2QocdWy/mlKp22qIpsdmhQIUleE7FLQcZXrMfmK+h97DSeJWeRKO6Yebw/1hygvDLi4aiURNwIDAQAB"));
  }

  private void testDecodeAndDecrypt(final KeyExpression keyExpression) {
    final Rsa rsa = new Rsa(Cryptos.provider(), keyExpression);
    log.info("javascript privateKey: {}", rsa.getDecryptor().getKey().getEncodedRsaPrivateKey());
    log.info("javascript publicKey: {}", rsa.getEncryptor().getKey().getEncodedRsaPublicKey());
    // 出错 rsa =
    //Rsa.bouncyCastleRsa1024Pkcs1( //
    //  rsa.getDecryptor().getKey().getEncodedRsaPrivateKey(), //
    //  rsa.getEncryptor().getKey().getEncodedRsaPublicKey());

    final String plainText = "encrypt and decrypt";
    final String result = rsa.decrypt(rsa.encrypt(plainText));
    assertEquals(plainText, result);
    log.info("{}", result);
  }
}
