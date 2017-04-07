package com.yirendai.oss.lib.common.crypto;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;

import lombok.Getter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public class JwtKey {

  @Getter
  private final KeyExpression keyExpression;
  @Getter
  private final SignatureAlgorithm signatureAlgorithm;
  @Getter
  private final SecretKey signatureKey;

  public JwtKey(final KeyExpression keyExpression) {
    this.keyExpression = keyExpression;
    this.signatureAlgorithm = SignatureAlgorithm.forName(this.keyExpression.getSpec());
    this.signatureKey = new SecretKeySpec( //
      TextCodec.BASE64.decode(this.keyExpression.getValue()), //
      this.signatureAlgorithm.getJcaName() //
    );
  }

  public JwtKey(final String keyExpression) {
    this(new KeyExpression(keyExpression));
  }
}
