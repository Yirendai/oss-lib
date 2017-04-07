package com.yirendai.oss.lib.common.crypto;

import static com.google.common.base.Preconditions.checkArgument;

import com.yirendai.oss.lib.common.CodecUtils;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;

import lombok.Getter;

/**
 * Created by zhanghaolun on 16/10/29.
 */
public class JwtKeyGenerator implements KeyGenerator {

  @Getter
  private final String spec;
  @Getter
  private KeyExpression key;

  public JwtKeyGenerator(final String spec) {
    this.spec = spec;
  }

  @Override
  public KeyExpression generateKey() {
    return JwtKeyGenerator.generateJwtKey(this.spec);
  }

  @Override
  public KeyExpression getKey(final String spec) {
    checkArgument(this.spec.equals(spec), "spec " + spec + " not supported.");
    if (this.key == null) {
      this.key = this.generateKey();
    }
    return this.key;
  }

  /**
   * generate JWT key
   *
   * @param spec eg: SignatureAlgorithm.HS512.getValue()
   * @return JWT base64EncodedKeyBytes
   */
  public static KeyExpression generateJwtKey(final String spec) {
    final String signatureAlgorithmName = "" + spec;
    final SignatureAlgorithm algorithm = SignatureAlgorithm.forName(signatureAlgorithmName);
    return new KeyExpression( //
      spec, //
      CodecUtils.encodeBase64(MacProvider.generateKey(algorithm).getEncoded()) //
    );
  }
}
