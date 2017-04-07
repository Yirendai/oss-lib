package com.yirendai.oss.lib.common;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Date;

/**
 * Created by haolun on 17/1/10.
 */
public class DateTimeFormatterWrapper extends DateFormat {

  private final DateTimeFormatter dateTimeFormatter;

  public DateTimeFormatterWrapper(final DateTimeFormatter dateTimeFormatter) {
    this.dateTimeFormatter = dateTimeFormatter;
  }

  @Override
  public StringBuffer format(final Date date, final StringBuffer toAppendTo, final FieldPosition fieldPosition) {
    this.dateTimeFormatter.printTo(toAppendTo, new DateTime(date, Defaults.UTC_P8));
    return toAppendTo;
  }

  @Override
  public Date parse(final String source, final ParsePosition pos) {
    final MutableDateTime mutableDateTime = new MutableDateTime();
    this.dateTimeFormatter.parseInto(mutableDateTime, source, pos.getIndex());
    return mutableDateTime.toDate();
  }
}
