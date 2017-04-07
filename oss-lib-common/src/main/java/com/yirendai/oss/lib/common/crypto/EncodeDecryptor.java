package com.yirendai.oss.lib.common.crypto;

/**
 * A decryptor decode and decrypt then output the plain text.
 * Created by zhanghaolun on 16/11/17.
 */
public interface EncodeDecryptor {

  /**
   * decode and decrypt.
   *
   * @param encryptedAndEncoded encrypted and encoded
   * @return plain text
   */
  String decrypt(String encryptedAndEncoded);
}
