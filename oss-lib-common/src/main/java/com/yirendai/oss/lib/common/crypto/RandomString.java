package com.yirendai.oss.lib.common.crypto;

import java.util.Random;

/**
 * Created by zhanghaolun on 16/8/17.
 */
public interface RandomString {

  String generate(int length);

  /**
   * Random strings.
   *
   * @author zhanghaolun
   */
  enum RandomStrings implements RandomString {

    /**
     * 随机生成BASE62字符串.
     */
    RANDOM_BASE62(new RandomStringImpl(RandomStringImpl.BASE62));

    private final RandomStringImpl instance;

    RandomStrings(final RandomStringImpl instance) {
      this.instance = instance;
    }

    @Override
    public String generate(final int length) {
      return this.instance.generate(length);
    }
  }

  final class RandomStringImpl implements RandomString {

    private static final String BASE62 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final String base;

    private RandomStringImpl(final String base) {
      this.base = base;
    }

    private static String random(final String base, final int length) {
      final Random random = new Random();
      final StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length; i++) {
        final int number = random.nextInt(base.length());
        sb.append(base.charAt(number));
      }
      return sb.toString();
    }

    @Override
    public String generate(final int length) {
      return random(this.base, length);
    }
  }
}
