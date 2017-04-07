package com.yirendai.oss.lib.common.crypto;

import com.yirendai.oss.lib.common.Defaults;

import io.jsonwebtoken.Jwts;

import lombok.Getter;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public class JwtEncryptor implements EncodeEncryptor {

  @Getter
  private final JwtKey key;

  public JwtEncryptor(final KeyExpression keyExpression) {
    this.key = new JwtKey(keyExpression);
  }

  @Override
  public String encrypt(final String plainText) {
    throw new UnsupportedOperationException("jwt without maxAge is not supported.");
  }

  @Override
  public String encrypt(final String plainText, final Integer maxAge) {
    return this.buildCompactJws(plainText, Defaults.now(), maxAge);
  }

  public String buildCompactJws(final String token, final DateTime now, final Integer maxAge) {
    final Date expiration = now.plusSeconds(maxAge).toDate();
    return token != null ? Jwts.builder()
      .setSubject(token)
      .signWith(this.key.getSignatureAlgorithm(), this.key.getSignatureKey())
      .setExpiration(expiration)
      .compact() : null;
  }
}
