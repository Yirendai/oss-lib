package com.yirendai.oss.lib.common.crypto;

import static org.junit.Assert.assertEquals;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.junit.Test;

/**
 * Created by zhanghaolun on 16/11/4.
 */
public class JwtKeyGeneratorTest {

  @Test
  public void testJwtKeyGenerator() {
    final String spec = SignatureAlgorithm.HS512.getValue();
    final KeyExpression keyExpression = new JwtKeyGenerator(spec).generateKey();
    final JwtKey jwtKey = new JwtKey(keyExpression);

    final String compactJws = Jwts.builder()
      .setSubject("Joe")
      .signWith(jwtKey.getSignatureAlgorithm(), jwtKey.getSignatureKey())
      .compact();

    assertEquals("Joe", //
      Jwts.parser().setSigningKey(jwtKey.getSignatureKey()) //
        .parseClaimsJws(compactJws).getBody().getSubject());

    System.out.println(jwtKey.getSignatureAlgorithm().getJcaName());
    System.out.println(jwtKey.getKeyExpression().getValue());
    System.out.println(compactJws);
  }
}
