package com.yirendai.oss.lib.errorhandle.api;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.yirendai.oss.lib.errorhandle.api.ApplicationException.isApplicationError;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhanghaolun on 16/9/22.
 */
public final class ApplicationExceptions {

  static final Pattern PATTERN_EXPRESSION = Pattern.compile("(#\\{[^}]+\\}|\\{\\})");

  private ApplicationExceptions() {
  }

  public static void checkArgument( //
      final boolean expression, //
      final String format, final Serializable... arguments //
  ) {
    if (!expression) {
      throw applicationException(BAD_REQUEST, format, arguments);
    }
  }

  public static <T> T checkNotNull( //
      final T object, //
      final HttpStatus status, final String format, final Serializable... arguments //
  ) {
    if (object == null) {
      throw applicationException(status, format, arguments);
    }
    return object;
  }

  public static void check( //
      final boolean expression, //
      final HttpStatus status, final String format, final Serializable... arguments //
  ) {
    if (!expression) {
      throw applicationException(status, format, arguments);
    }
  }

  public static ApplicationException wrap( //
      final Throwable throwable, //
      final HttpStatus status, final String format, final Serializable... arguments //
  ) {
    if (isApplicationError(throwable)) {
      return (ApplicationException) throwable;
    } else {
      return applicationException(status, format, arguments);
    }
  }

  public static ApplicationException applicationException( //
      final HttpStatus status, final String format, final Serializable... arguments //
  ) {
    final String template = toTemplate(format);
    final Map<String, Serializable> contextVariables = toContextVariables(template, arguments);
    return new ApplicationException(status, template, contextVariables);
  }

  public static String toTemplate(final String format) {
    final List<String[]> found = newLinkedList();

    final Matcher matcher = PATTERN_EXPRESSION.matcher(format);
    int index = 0;
    while (matcher.find()) {
      final String text = matcher.group(0);
      final String el = "#{arg" + index + "}";
      found.add(new String[]{text, isEl(text) ? text : el});
      index++;
    }

    String template = format;
    for (final String[] element : found) {
      if (!element[0].equals(element[1])) {
        template = StringUtils.replace(template, element[0], element[1], 1);
      }
    }

    return template;
  }

  static Map<String, Serializable> toContextVariables(final String template, final Serializable[] arguments) {
    final Map<String, Serializable> contextVariables = newLinkedHashMap();

    final Matcher matcher = PATTERN_EXPRESSION.matcher(template);
    int index = 0;
    while (matcher.find()) {
      final String text = matcher.group(0);
      final String name = text.substring(2, text.length() - 1);
      final Serializable value = index < arguments.length ? arguments[index] : null;
      contextVariables.put(name, value);
      index++;
    }

    return contextVariables;
  }

  static boolean isEl(final String text) {
    return text.charAt(0) == '#';
  }
}
