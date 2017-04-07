package com.yirendai.oss.lib.common;

import static org.joda.time.format.DateTimeFormat.forPattern;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.nio.charset.Charset;

/**
 * Created by zhanghaolun on 16/6/20.
 */
public abstract class Defaults {

  private Defaults() {
  }

  public static final Charset UTF_8 = Charset.forName("UTF-8");

  public static final String CRON_EVERY_30_SECONDS = "0/30 * * * * ?";

  public static final DateTimeZone UTC_P8 = DateTimeZone.forID("Asia/Shanghai");

  public static final DateTime EPOCH = new DateTime(0L);

  public static final DateTimeFormatter yyyyMMdd_HHmmss;
  public static final DateTimeFormatter ISO8601;

  public static DateTime now() {
    return DateTime.now(UTC_P8);
  }

  public static Integer minutesSinceEpoch(final DateTime dateTime) {
    return Minutes.minutesBetween(EPOCH, dateTime).getMinutes();
  }

  /**
   * see: http://stackoverflow.com/questions/15245307/java-simpledateformat-timezone-offset-with-minute-separated-by-colon
   * You can get the timezone offset formatted like +08:00
   * with the SimpleDateFormat in Java 7 (yyyy-MM-dd'T'HH:mm:ss.SSSXXX)
   * with the Joda's DateTimeFormat (yyyy-MM-dd'T'HH:mm:ss.SSSZZ)
   */
  public static final String PATTERN_JAVA_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
  public static final String PATTERN_JODA_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";

  static {
    yyyyMMdd_HHmmss = forPattern("yyyyMMdd-HH:mm:ss").withZone(UTC_P8);
    ISO8601 = DateTimeFormat.forPattern(PATTERN_JODA_ISO8601).withZone(UTC_P8);
  }
}
