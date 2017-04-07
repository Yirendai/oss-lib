package com.yirendai.oss.lib.webmvc.api;

import static org.junit.Assert.assertEquals;

import com.yirendai.oss.lib.common.Defaults;
import com.yirendai.oss.lib.common.crypto.Jwt;
import com.yirendai.oss.lib.common.crypto.KeyExpression;

import io.jsonwebtoken.ExpiredJwtException;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhanghaolun on 16/11/1.
 */
public class JsonWebTokenTest {

  private JsonWebToken<String> token;
  private int maxAge;

  @Before
  public void setUp() {
    this.maxAge = (int) TimeUnit.DAYS.toSeconds(1L);
    final String keyExpression = "HS512:Ve+/vU5u77+977+977+977+977+9Acu/77+977+977+9OXrvv71XH++" +
      "/vRLvv73vv73vv73vv71577+9fQLvv73vv71eB++/vW7vv71g77+977+977+9L++/vWDvv73vv73vv71577+9VO+" +
      "/ve+/vR3vv73vv73Coemfv++/ve+/vQ==";
    final Jwt jwt = new Jwt(new KeyExpression(keyExpression));
    this.token = new JsonWebToken<>(new StringToken(), jwt, maxAge);
  }

  @Test
  public void testJwtTokenNotExpired() {
    final DateTime now = Defaults.now();
    final String subject = "jwtToken";
    final String compactJws = this.token.getJwt().getEncryptor().buildCompactJws(subject, now, this.maxAge);
    assertEquals(subject, //
      this.token.getJwt().getDecryptor().parseCompactJws(compactJws, now.plusSeconds(this.maxAge / 2)).getBody().getSubject());
  }

  @Test(expected = ExpiredJwtException.class)
  public void testJwtTokenExpired() {
    final DateTime now = Defaults.now();
    final String subject = "jwtToken";
    final String compactJws = this.token.getJwt().getEncryptor().buildCompactJws(subject, now, this.maxAge);
    this.token.getJwt().getDecryptor().parseCompactJws(compactJws, now.plusSeconds(this.maxAge + 1));
  }
}
