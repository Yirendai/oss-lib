package com.yirendai.oss.lib.common.crypto;

/**
 * Created by zhanghaolun on 16/8/17.
 */
public interface CryptoConstants {

  String ALGO_AES = "AES";
  String ALGO_RSA = "RSA";

  char COLON = ':';
  String UNDERSCORE = "_";

  String RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1Padding";
  String AES_CBC_NOPADDING = "AES/CBC/NoPadding";
  @Deprecated
  String AES_CBC_PKCS7PADDING = "AES/CBC/PKCS7Padding";
}
