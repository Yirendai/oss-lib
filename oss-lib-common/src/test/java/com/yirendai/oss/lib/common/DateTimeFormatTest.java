package com.yirendai.oss.lib.common;

import static com.yirendai.oss.lib.common.Defaults.PATTERN_JODA_ISO8601;
import static com.yirendai.oss.lib.common.Defaults.UTC_P8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by haolun on 17/1/10.
 */
@Slf4j
public class DateTimeFormatTest {

  private static DateTime now;

  @BeforeClass
  public static void setUp() {
    DateTimeFormatTest.now = Defaults.now();
  }

  @Test
  public void testDateTimeFormat() {
    // JavaSimpleDateFormat
    final DateFormat formatJdk = new SimpleDateFormat(Defaults.PATTERN_JAVA_ISO8601);
    formatJdk.setTimeZone(UTC_P8.toTimeZone());
    log.info("JavaSimpleDateFormatFromPattern format: {}", formatJdk.format(now.toDate()));
    // JodaISODateTimeFormat
    final DateTimeFormatter formatterIso = ISODateTimeFormat.dateTime();
    final DateFormat formatterIsoWrapper = new DateTimeFormatterWrapper(formatterIso);
    log.info("JodaISODateTimeFormat format: {}", formatterIso.print(now));
    log.info("DateTimeFormatterWrapper format: {}", formatterIsoWrapper.format(now.toDate()));
    // JodaDateTimeFormatterFromPattern
    final DateTimeFormatter formatterFromPattern = DateTimeFormat.forPattern(PATTERN_JODA_ISO8601).withZone(UTC_P8);
    log.info("JodaDateTimeFormatterFromPattern format: {}", formatterFromPattern.print(now));
    // JacksonISO8601DateFormat
    final DateFormat dateFormat = ISO8601DateFormat.getDateTimeInstance();// not standard ISO8601
    log.info("JacksonISO8601DateFormat format: {}", dateFormat.format(now.toDate()));

    assertEquals(formatJdk.format(now.toDate()), formatterIso.print(now));
    assertEquals(formatJdk.format(now.toDate()), formatterIsoWrapper.format(now.toDate()));
    assertEquals(formatJdk.format(now.toDate()), formatterFromPattern.print(now));

    assertNotEquals(formatterIso.print(now), dateFormat.format(now.toDate()));
  }
}
