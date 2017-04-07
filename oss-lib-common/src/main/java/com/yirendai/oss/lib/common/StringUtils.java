package com.yirendai.oss.lib.common;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;

public final class StringUtils {

  public static String dropComment(final String text, final char commentMark) {
    return Arrays.stream(text.split("\n")).filter(line -> isNotBlank(line) && line.charAt(0) != commentMark)
      .collect(joining());
  }

  public static String lowerCaseFirstChar(final String string) {
    return isNotBlank(string) ? string.substring(0, 1).toLowerCase() + string.substring(1) : "";
  }

  public static String upperCaseFirstChar(final String string) {
    return isNotBlank(string) ? string.substring(0, 1).toUpperCase() + string.substring(1) : "";
  }

  private StringUtils() {
  }
}
