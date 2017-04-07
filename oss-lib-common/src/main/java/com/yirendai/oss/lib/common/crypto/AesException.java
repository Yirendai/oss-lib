package com.yirendai.oss.lib.common.crypto;

import lombok.Getter;

@SuppressWarnings("serial")
public class AesException extends RuntimeException {

  @Getter
  private final AesError aesError;

  AesException(final AesError aesError) {
    super(aesError.getMessage());
    this.aesError = aesError;
  }

  AesException(final AesError aesError, final Throwable cause) {
    super(aesError.getMessage(), cause);
    this.aesError = aesError;
  }

  /**
   * AES error.
   * 
   * @author zhanghaolun
   */
  @SuppressWarnings("PMD.SingularField")
  @Getter
  public enum AesError {

    OK(0, ""),
    VALIDATE_SIGNATURE_ERROR(-40001, "签名验证错误"),
    PARSE_XML_ERROR(-40002, "XML解析失败"),
    COMPUTE_SIGNATURE_ERROR(-40003, "SHA加密生成签名失败"),
    ILLEGAL_AES_KEY(-40004, "SymmetricKey非法"),
    VALIDATE_CORP_ID_ERROR(-40005, "corpid校验失败"),
    ENCRYPT_AES_ERROR(-40006, "AES加密失败"),
    DECRYPT_AES_ERROR(-40007, "AES解密失败"),
    ILLEGAL_BUFFER(-40008, "解密后得到的buffer非法");
    
    private final int code;
    private final String message;

    AesError(final int code, final String message) {
      this.code = code;
      this.message = message;
    }
  }
}
