package com.yirendai.oss.lib.common.crypto;

import com.yirendai.oss.lib.common.Defaults;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.impl.FixedClock;

import lombok.Getter;

import org.joda.time.DateTime;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public class JwtDecryptor implements EncodeDecryptor {

  @Getter
  private final JwtKey key;

  public JwtDecryptor(final KeyExpression keyExpression) {
    this.key = new JwtKey(keyExpression);
  }

  /**
   * decrypt.
   *
   * @param encryptedAndEncoded compactJws
   */
  @Override
  public String decrypt(final String encryptedAndEncoded) {
    final Jws<Claims> jws = encryptedAndEncoded != null ? //
      this.parseCompactJws(encryptedAndEncoded, Defaults.now()) : null;
    return jws != null ? jws.getBody().getSubject() : null;
  }

  public Jws<Claims> parseCompactJws(final String compactJws, final DateTime dateTime) {
    final Clock clock = new FixedClock(dateTime.toDate());
    return compactJws != null ? //
      Jwts.parser().setClock(clock).setSigningKey(this.key.getSignatureKey()).parseClaimsJws(compactJws) : null;
  }
}
