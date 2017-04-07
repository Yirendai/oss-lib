package com.yirendai.oss.lib.common.crypto;

import static com.google.common.base.Preconditions.checkArgument;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.ALGO_AES;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.ALGO_RSA;

import io.jsonwebtoken.SignatureAlgorithm;

import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;

@Slf4j
public abstract class Cryptos {

  private static final Provider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();

  private Cryptos() {
  }

  public static Provider provider() {
    return BOUNCY_CASTLE_PROVIDER;
  }

  @SuppressWarnings("unchecked")
  public static <T extends EncodeCipher> T cipher(final KeyExpression keyExpression) {
    final T result;
    if (keyExpression == null || !keyExpression.isPresent()) {
      result = null;
    } else {
      final String spec = keyExpression.getSpec();
      if (spec.startsWith(ALGO_AES)) {
        result = (T) new Aes(Cryptos.provider(), keyExpression);
      } else if (spec.startsWith(ALGO_RSA)) {
        result = (T) new Rsa(Cryptos.provider(), keyExpression);
      } else {
        SignatureAlgorithm.forName(spec);
        result = (T) new Jwt(keyExpression);
      }
    }

    if (result != null) {
      final String test = "test";
      try {
        checkArgument(test.equals(result.decrypt(result.encrypt(test))), "bad cipher, test failed");
      } catch (final UnsupportedOperationException ignored) {
        // ignored
        log.trace("unsupported cipher operation", ignored);
      }
      try {
        checkArgument(test.equals(result.decrypt(result.encrypt(test, 3600))), "bad cipher, test failed");
      } catch (final UnsupportedOperationException ignored) {
        // ignored
        log.trace("unsupported cipher operation", ignored);
      }
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T extends EncodeEncryptor> T encryptor(final KeyExpression keyExpression) {
    final T result;
    if (keyExpression == null || !keyExpression.isPresent()) {
      result = null;
    } else {
      final String spec = keyExpression.getSpec();
      if (spec.startsWith(ALGO_AES)) {
        result = (T) new AesEncryptor(BOUNCY_CASTLE_PROVIDER, keyExpression);
      } else if (spec.startsWith(ALGO_RSA)) {
        result = (T) new RsaEncryptor(BOUNCY_CASTLE_PROVIDER, keyExpression);
      } else {
        SignatureAlgorithm.forName(spec);
        result = (T) new JwtEncryptor(keyExpression);
      }
    }

    if (result != null) {
      try {
        result.encrypt("test");
      } catch (final UnsupportedOperationException ignored) {
        // ignored
        log.trace("unsupported encryptor operation", ignored);
      }
      try {
        result.encrypt("test", 3600);
      } catch (final UnsupportedOperationException ignored) {
        // ignored
        log.trace("unsupported encryptor operation", ignored);
      }
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T extends EncodeDecryptor> T decryptor(final KeyExpression keyExpression) {
    final T result;
    if (keyExpression == null || !keyExpression.isPresent()) {
      result = null;
    } else {
      final String spec = keyExpression.getSpec();
      if (spec.startsWith(ALGO_AES)) {
        result = (T) new AesDecryptor(BOUNCY_CASTLE_PROVIDER, keyExpression);
      } else if (spec.startsWith(ALGO_RSA)) {
        result = (T) new RsaDecryptor(BOUNCY_CASTLE_PROVIDER, keyExpression);
      } else {
        SignatureAlgorithm.forName(spec);
        result = (T) new JwtDecryptor(keyExpression);
      }
    }
    return result;
  }
}
