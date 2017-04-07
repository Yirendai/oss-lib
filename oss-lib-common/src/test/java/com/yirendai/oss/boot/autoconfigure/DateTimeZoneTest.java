package com.yirendai.oss.boot.autoconfigure;

import static org.junit.Assert.assertEquals;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.TimeZone;

@Slf4j
public class DateTimeZoneTest {

  @Test
  public void testDefaultZoneByRunning() {
    final DateTime now = DateTime.now();
    final String fromSystemProperty = System.getProperty("user.timezone");
    final String fromTimeZoneGetDefault = TimeZone.getDefault().getID();
    if (StringUtils.isNotBlank(fromSystemProperty)) {
      assertEquals(fromSystemProperty, fromTimeZoneGetDefault);
      assertEquals(fromSystemProperty, now.getZone().getID());
    }
    log.info("fromSystemProperty: {}", fromSystemProperty);
    log.info("fromTimeZoneGetDefault: {}", fromTimeZoneGetDefault);
    log.info("fromDateTimeNowGetZone: {}", now.getZone().getID());
    assertEquals(fromTimeZoneGetDefault, now.getZone().getID());
  }
}
