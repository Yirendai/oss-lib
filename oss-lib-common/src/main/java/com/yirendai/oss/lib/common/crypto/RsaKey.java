package com.yirendai.oss.lib.common.crypto;

import static com.google.common.base.Preconditions.checkArgument;
import static com.yirendai.oss.lib.common.StringUtils.dropComment;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.ALGO_RSA;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.COLON;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.UNDERSCORE;
import static java.lang.Integer.parseInt;

import com.google.common.collect.ImmutableSet;

import com.yirendai.oss.lib.common.CodecUtils;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collection;

/**
 * Created by zhanghaolun on 16/11/17.
 */
@Slf4j
public class RsaKey {

  public static final char COMMENT_MARK = '-';
  public static final String KEY_FORMAT_PKCS1 = "PKCS1";
  public static final String KEY_FORMAT_PKCS8 = "PKCS8";
  public static final String KEY_FORMAT_X509 = "X509";
  public static final String KEY_FORMAT_PKCS1_PKCS1 = KEY_FORMAT_PKCS1 + "_" + KEY_FORMAT_PKCS1;
  public static final String KEY_FORMAT_PKCS1_X509 = KEY_FORMAT_PKCS1 + "_" + KEY_FORMAT_X509;
  public static final String KEY_FORMAT_PKCS8_X509 = KEY_FORMAT_PKCS8 + "_" + KEY_FORMAT_X509;

  public static final String KEY_TYPE_PAIR = "PAIR";
  public static final String KEY_TYPE_PRIVATE = "PRIV";
  public static final String KEY_TYPE_PUBLIC = "PUB";

  static final Collection<String> SUPPORTED_PAIR_FORMATS = ImmutableSet.of( //
    KEY_FORMAT_PKCS1_PKCS1, //
    KEY_FORMAT_PKCS1_X509, //
    KEY_FORMAT_PKCS8_X509 //
  );

  @Getter
  private final KeyExpression keyExpression;
  @Getter
  private final RSAPublicKey rsaPublicKey;
  @Getter
  private final RSAPrivateKey rsaPrivateKey;
  @Getter
  private final String keyFormat;
  @Getter
  private final int keySize;
  @Getter
  private final String keyType;
  @Getter
  private final String privateKeyFormat;
  @Getter
  private final String publicKeyFormat;

  public RsaKey(final KeyExpression keyExpression) {
    this.keyExpression = keyExpression;

    final String spec = keyExpression.getSpec();
    final String value = keyExpression.getValue();

    this.keyFormat = RsaKey.keyFormat(spec);
    this.keySize = RsaKey.keySize(spec);
    this.keyType = RsaKey.keyType(spec);

    if (KEY_TYPE_PAIR.equals(this.keyType)) {
      checkArgument(SUPPORTED_PAIR_FORMATS.contains(this.keyFormat), "unsupported keyFormat " + this.keyFormat);
      this.privateKeyFormat = this.keyFormat.split(UNDERSCORE)[0];
      this.publicKeyFormat = this.keyFormat.split(UNDERSCORE)[1];
      this.rsaPrivateKey = RsaKey.privateKey(RsaKey.extractPrivateKey(this.keyExpression), this.privateKeyFormat);
      this.rsaPublicKey = RsaKey.publicKey(RsaKey.extractPublicKey(this.keyExpression), this.publicKeyFormat);
    } else if (KEY_TYPE_PRIVATE.equals(this.keyType)) {
      this.privateKeyFormat = this.keyFormat;
      this.publicKeyFormat = null;
      this.rsaPrivateKey = RsaKey.privateKey(value, this.keyFormat);
      this.rsaPublicKey = null;
    } else if (KEY_TYPE_PUBLIC.equals(this.keyType)) {
      this.privateKeyFormat = null;
      this.publicKeyFormat = this.keyFormat;
      this.rsaPrivateKey = null;
      this.rsaPublicKey = RsaKey.publicKey(value, this.keyFormat);
    } else {
      throw new IllegalArgumentException("unsupported keyType " + this.keyType);
    }
  }

  public RsaKey(final String keyExpression) {
    this(new KeyExpression(keyExpression));
  }

  public String getEncodedRsaPrivateKey() {
    return CodecUtils.encodeBase64(this.getRsaPrivateKey().getEncoded());
  }

  public String getEncodedRsaPublicKey() {
    return CodecUtils.encodeBase64(this.getRsaPublicKey().getEncoded());
  }

  public KeyExpression getPrivateKey() {
    final KeyExpression result;
    if (KEY_TYPE_PAIR.equals(this.keyType)) {
      final String spec = RsaKey.keySpec(this.privateKeyFormat, this.keySize, KEY_TYPE_PRIVATE);
      final String value = RsaKey.extractPrivateKey(this.keyExpression);
      result = new KeyExpression(spec, value);
    } else if (KEY_TYPE_PRIVATE.equals(this.keyType)) {
      result = this.keyExpression;
    } else {
      throw new UnsupportedOperationException("no privateKey present");
    }
    return result;
  }

  public KeyExpression getPublicKey() {
    final KeyExpression result;
    if (KEY_TYPE_PAIR.equals(this.keyType)) {
      final String spec = RsaKey.keySpec(this.publicKeyFormat, this.keySize, KEY_TYPE_PUBLIC);
      final String value = RsaKey.extractPublicKey(this.keyExpression);
      result = new KeyExpression(spec, value);
    } else if (KEY_TYPE_PUBLIC.equals(this.keyType)) {
      result = this.keyExpression;
    } else {
      throw new UnsupportedOperationException("no publicKey present");
    }
    return result;
  }

  public KeyExpression getKey(final String spec) {
    final String keyFormat = RsaKey.keyFormat(spec);
    final int keySize = RsaKey.keySize(spec);
    final String keyType = RsaKey.keyType(spec);
    checkArgument(this.keySize == keySize, "keySize not match " + this.keySize);

    final KeyExpression result;
    if (KEY_TYPE_PAIR.equals(keyType)) {
      checkArgument(KEY_TYPE_PAIR.equals(this.keyType), "no keyPair present");
      checkArgument(keyFormat.equals(this.keyFormat), "keyFormat not match " + this.keyFormat);
      result = new KeyExpression(spec, this.keyExpression.getValue());
    } else if (KEY_TYPE_PRIVATE.equals(keyType)) {
      checkArgument(keyFormat.equals(this.privateKeyFormat), "keyFormat not match " + this.privateKeyFormat);
      result = this.getPrivateKey();
    } else if (KEY_TYPE_PUBLIC.equals(keyType)) {
      checkArgument(keyFormat.equals(this.publicKeyFormat), "keyFormat not match " + this.publicKeyFormat);
      result = this.getPublicKey();
    } else {
      throw new IllegalArgumentException("unsupported keyType " + keyType);
    }
    return result;
  }

  @Override
  public String toString() {
    return this.keyExpression.toString();
  }

  public static String extractPrivateKey(final KeyExpression keyExpression) {
    return keyExpression.getValue().substring(0, keyExpression.getValue().indexOf(COLON));
  }

  public static String extractPublicKey(final KeyExpression keyExpression) {
    return keyExpression.getValue().substring(keyExpression.getValue().indexOf(COLON) + 1);
  }

  public static String keyFormat(final String spec) {
    final int index = spec.indexOf(UNDERSCORE, spec.indexOf(UNDERSCORE) + 1) + 1;
    return spec.substring(index);
  }

  public static int keySize(final String spec) {
    final String text = spec.split(UNDERSCORE)[0].substring(3);
    return parseInt(text);
  }

  public static String keySpec(final String keyFormat, final int keySize, final String keyType) {
    return ALGO_RSA + keySize + UNDERSCORE + keyType.toUpperCase() + UNDERSCORE + keyFormat.toUpperCase();
  }

  public static String keyType(final String spec) {
    return spec.split(UNDERSCORE)[1];
  }

  public static RSAPrivateKey privateKey(final String key, final String keyFormat) {
    final RSAPrivateKey result;
    if (KEY_FORMAT_PKCS1.equals(keyFormat)) {
      result = RsaKey.privateKeyPkcs1(key);
    } else if (KEY_FORMAT_PKCS8.equals(keyFormat)) {
      result = RsaKey.privateKeyPkcs8(key);
    } else {
      throw new IllegalArgumentException("unsupported keyFormat " + keyFormat);
    }
    return result;
  }

  public static RSAPublicKey publicKey(final String key, final String keyFormat) {
    checkArgument(KEY_FORMAT_X509.equals(keyFormat), "unsupported keyFormat " + keyFormat);
    return RsaKey.publicKeyX509(key);
  }

  @SneakyThrows
  public static RSAPrivateKey privateKeyPkcs1(final String pkcs1) {
    final String withoutComment = dropComment(pkcs1, COMMENT_MARK);
    final byte[] raw = CodecUtils.decodeBase64(withoutComment);

    if (log.isDebugEnabled()) {
      log.debug("withoutComment: {}", withoutComment);
      log.debug("raw: {}", Hex.encodeHexString(raw));
    }

    final ASN1Primitive asn1Primitive = ASN1Sequence.fromByteArray(raw);
    // final RSAPrivateKeyStructure asn1 = new RSAPrivateKeyStructure((ASN1Sequence) asn1Primitive);
    final org.bouncycastle.asn1.pkcs.RSAPrivateKey asn1 =
      org.bouncycastle.asn1.pkcs.RSAPrivateKey.getInstance(asn1Primitive);
    final RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(asn1.getModulus(), asn1.getPrivateExponent());
    return (RSAPrivateKey) KeyFactory.getInstance(ALGO_RSA).generatePrivate(rsaPrivateKeySpec);
  }

  @SneakyThrows
  public static RSAPrivateKey privateKeyPkcs8(final String pkcs8) {
    final byte[] raw = CodecUtils.decodeBase64(dropComment(pkcs8, COMMENT_MARK));
    return (RSAPrivateKey) KeyFactory.getInstance(ALGO_RSA).generatePrivate(new PKCS8EncodedKeySpec(raw));
  }

  @SneakyThrows
  public static RSAPublicKey publicKeyX509(final String x509) {
    final byte[] raw = CodecUtils.decodeBase64(dropComment(x509, COMMENT_MARK));
    return (RSAPublicKey) KeyFactory.getInstance(ALGO_RSA).generatePublic(new X509EncodedKeySpec(raw));
  }
}
