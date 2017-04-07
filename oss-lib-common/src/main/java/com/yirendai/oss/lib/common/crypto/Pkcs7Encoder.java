package com.yirendai.oss.lib.common.crypto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * 提供基于PKCS7算法的加解密接口.
 */
public enum Pkcs7Encoder {

  PKCS7_UTF8_BLOCK32(UTF_8, 32);

  private final Charset charset;
  private final int blockSize;

  Pkcs7Encoder(final Charset charset, final int blockSize) {
    this.charset = charset;
    this.blockSize = blockSize;
  }

  /**
   * 将数字转化成ASCII码对应的字符，用于对明文进行补码.
   *
   * @param value 需要转化的数字
   * @return 转化得到的字符
   */
  private static char chr(final int value) {
    return (char) (byte) (value & 0xFF);
  }

  /**
   * 获得对明文进行补位填充的字节.
   *
   * @param count 需要进行填充补位操作的明文字节个数
   * @return 补齐用的字节数组
   */
  public byte[] encode(final int count) {
    // 计算需要填充的位数
    final int toPad = this.blockSize - (count % this.blockSize);
    final int amountToPad = toPad == 0 ? this.blockSize : toPad;
    // 获得补位所用的字符
    final char padChr = Pkcs7Encoder.chr(amountToPad);
    final StringBuilder sb = new StringBuilder();
    for (int index = 0; index < amountToPad; index++) {
      sb.append(padChr);
    }
    return sb.toString().getBytes(this.charset);
  }

  /**
   * 删除解密后明文的补位字符.
   *
   * @param decrypted 解密后的明文
   * @return 删除补位字符后的明文
   */
  public byte[] decode(final byte[] decrypted) {
    final int pad = (int) decrypted[decrypted.length - 1];
    final int padAmount = pad < 1 || pad > this.blockSize ? 0 : pad;
    return Arrays.copyOfRange(decrypted, 0, decrypted.length - padAmount);
  }
}
