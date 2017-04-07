package com.yirendai.oss.lib.common;

import static com.google.common.base.Charsets.UTF_8;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.COLON;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.Base64Variants;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public class BasicAuthUtilsTest {

  @Test
  public void testBasicAuthHeader() {
    // echo "Basic $(echo -ne 'admin:admin_pass' | base64)"
    final String shellEncoded = "Basic YWRtaW46YWRtaW5fcGFzcw==";
    final String[] decoded = BasicAuthUtils.extractAndDecodeAuthHeader(shellEncoded, UTF_8.name());
    assertEquals("admin", decoded[0]);
    assertEquals("admin_pass", decoded[1]);

    assertEquals(shellEncoded, verify("admin", "admin_pass"));
  }

  private String verify(final String username, final String password) {
    final String authStr = username + COLON + password;
    final byte[] commonsEncodedBytes = org.apache.commons.codec.binary.Base64.encodeBase64(authStr.getBytes(UTF_8));
    final String commonsEncoded = "Basic " + new String(commonsEncodedBytes, UTF_8);
    final String jacksonEncoded = "Basic " + Base64Variants.MIME_NO_LINEFEEDS.encode(authStr.getBytes(UTF_8));

    final String result = BasicAuthUtils.basicAuthHeader("admin", "admin_pass");
    assertEquals(commonsEncoded, result);
    assertEquals(jacksonEncoded, result);

    final String[] decoded = BasicAuthUtils.extractAndDecodeAuthHeader(result, UTF_8.name());
    assertEquals(username, decoded[0]);
    assertEquals(password, decoded[1]);
    return result;
  }
}
