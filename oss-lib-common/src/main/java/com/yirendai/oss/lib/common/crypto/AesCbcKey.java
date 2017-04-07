package com.yirendai.oss.lib.common.crypto;

import static com.google.common.base.Preconditions.checkArgument;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.ALGO_AES;
import static com.yirendai.oss.lib.common.crypto.CryptoConstants.UNDERSCORE;
import static java.lang.Integer.parseInt;

import com.yirendai.oss.lib.common.CodecUtils;
import com.yirendai.oss.lib.common.crypto.AesException.AesError;

import lombok.Getter;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by zhanghaolun on 16/11/17.
 */
public class AesCbcKey {

  public static final String AES_MODE_CBC = "CBC";

  @Getter
  private final KeyExpression keyExpression;
  @Getter
  private final SecretKeySpec keySpec;
  @Getter
  private final IvParameterSpec cbcIv;
  @Getter
  private final int cbcIvLength;

  public AesCbcKey(final KeyExpression keyExpression) {
    this.keyExpression = keyExpression;

    final String spec = keyExpression.getSpec();
    final int keySize = keySize(spec);
    final int cbcIvLength = cbcIvLength(spec);

    final String key = keyExpression.getValue();
    checkArgument(key.endsWith("="), "must ends with '='");

    final byte[] keyBytes = CodecUtils.decodeBase64(key);
    checkArgument(keyBytes.length * 8 == keySize, AesError.ILLEGAL_AES_KEY);

    this.keySpec = new SecretKeySpec(keyBytes, ALGO_AES);
    // first cbcIvLength bytes
    this.cbcIv = new IvParameterSpec(keyBytes, 0, cbcIvLength);
    //this.cbcIv = new IvParameterSpec(java.util.Arrays.copyOfRange(aesKey, 0, randomStringLength));
    this.cbcIvLength = cbcIvLength;
  }

  public AesCbcKey(final String keyExpression) {
    this(new KeyExpression(keyExpression));
  }

  @Override
  public String toString() {
    return this.keyExpression.toString();
  }

  public static int keySize(final String spec) {
    return parseInt(spec.split(UNDERSCORE)[0].substring(3));
  }

  public static String keySpec(final int keySize) {
    return CryptoConstants.ALGO_AES + keySize + UNDERSCORE + AES_MODE_CBC + 16;
  }

  public static int cbcIvLength(final String spec) {
    return parseInt(spec.substring(spec.indexOf(AES_MODE_CBC) + 3));
  }
}
