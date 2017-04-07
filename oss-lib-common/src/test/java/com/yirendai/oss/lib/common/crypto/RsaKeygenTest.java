package com.yirendai.oss.lib.common.crypto;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Created by zhanghaolun on 16/11/4.
 */
public class RsaKeygenTest {

  @Test
  public void testRsaKeygen() {
    final String spec = RsaKey.keySpec(RsaKey.KEY_FORMAT_PKCS1_X509, 1024, RsaKey.KEY_TYPE_PAIR);
    final KeyExpression keyExpression = new RsaKeyGenerator(spec).generateKey();
    final Rsa rsa = new Rsa(Cryptos.provider(), keyExpression);

    final String plainText = "plainText";
    assertEquals(plainText, rsa.decrypt(rsa.encrypt(plainText)));
  }
}
