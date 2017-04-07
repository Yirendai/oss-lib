package com.yirendai.oss.lib.common.crypto;

/**
 * A encryptor encrypt and encode the plain text.
 * Created by zhanghaolun on 16/11/17.
 */
public interface EncodeEncryptor {

  /**
   * encrypt and encode.
   *
   * @param plainText plain text to encrypt
   * @return encrypted and encoded
   */
  String encrypt(String plainText);

  /**
   * encrypt and encode.
   *
   * @param plainText plainText plain text to encrypt
   * @param maxAge maxAge in seconds
   * @return encrypted and encoded
   */
  String encrypt(String plainText, Integer maxAge);
}
