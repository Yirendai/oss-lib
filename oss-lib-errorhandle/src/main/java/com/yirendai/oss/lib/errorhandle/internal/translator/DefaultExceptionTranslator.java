package com.yirendai.oss.lib.errorhandle.internal.translator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.collect.ImmutableMap;

import com.yirendai.oss.lib.common.msginterpolate.MessageInterpolator;
import com.yirendai.oss.lib.common.msginterpolate.MessageInterpolatorAware;
import com.yirendai.oss.lib.common.msginterpolate.NoOpMessageInterpolator;
import com.yirendai.oss.lib.errorhandle.api.ExceptionTranslator;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestAttributes;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Created by zhanghaolun on 16/8/10.
 */
@Slf4j
public class DefaultExceptionTranslator implements ExceptionTranslator, MessageInterpolatorAware {

  static final String DEFAULT_EXCEPTION_CLASS = "default";
  private static final String FIELD_STATUS = "status";
  private static final String FIELD_TEMPLATE = "template";

  private MessageInterpolator messageInterpolator;
  @Setter
  private Locale locale;
  @Setter
  private Comparator<Location> locationComparator;
  private List<MessageSource> messageSources;

  public void setMessageSources(final List<MessageSource> messageSources) {
    checkArgument(messageSources != null && !messageSources.isEmpty(), "messageSources must not null or empty");
    this.messageSources = unmodifiableList(messageSources);
  }

  public void setMessageInterpolator(final MessageInterpolator messageInterpolator) {
    this.messageInterpolator = messageInterpolator != null ? messageInterpolator : new NoOpMessageInterpolator();
  }

  @Override
  public Optional<Location> find(final Throwable throwable) {
    final Optional<Location> result;

    if (throwable != null) {
      final List<Location> locations = newLinkedList();

      Integer sourceOrder = 0;
      for (final MessageSource source : this.messageSources) {

        Boolean found = FALSE;
        Integer level = 0;
        for (Class type = throwable.getClass(); type != Throwable.class; type = type.getSuperclass()) {
          final Optional<String> template = this.template(source, type.getName());
          final Optional<Integer> status = this.status(source, type.getName());
          if (template.isPresent() && status.isPresent()) {
            found = TRUE;
            locations.add(new Location(source, sourceOrder, type.getName(), level));
            break;
          }
          level++;
        }

        if (!found) {
          final Optional<String> defaultTemplate = this.defaultTemplate(source);
          final Optional<Integer> defaultStatus = this.defaultStatus(source);
          if (defaultTemplate.isPresent() && defaultStatus.isPresent()) {
            locations.add(new Location(source, sourceOrder, DEFAULT_EXCEPTION_CLASS, Integer.MAX_VALUE));
          }
        }

        sourceOrder++;
      }

      locations.sort(this.locationComparator);
      result = locations.isEmpty() ? Optional.empty() : Optional.of(locations.get(0));
    } else {
      result = Optional.empty();
    }


    return result;
  }

  public Optional<String> defaultTemplate(final MessageSource messageSource) {
    return this.template(messageSource, DEFAULT_EXCEPTION_CLASS);
  }

  public Optional<Integer> defaultStatus(final MessageSource messageSource) {
    return this.status(messageSource, DEFAULT_EXCEPTION_CLASS);
  }

  @Override
  public Optional<String> localizedMessage( //
    final String template, //
    final RequestAttributes request, //
    final Throwable throwable, //
    final Map<String, Serializable> contextVariables //
  ) {
    final Optional<String> result;
    if (template != null) {
      final Map<String, Object> variables = ImmutableMap.<String, Object>builder() //
        .putAll(contextVariables != null ? contextVariables : ImmutableMap.of()) //
        .put("req", request) //
        .put("ex", throwable) //
        .build();
      final String localizedMessage = this.messageInterpolator.interpolate(template, variables);
      result = Optional.ofNullable(localizedMessage);
    } else {
      result = Optional.empty();
    }
    return result;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Optional<Integer> status(final Location location) {
    return location != null ? this.status(location.getSource(), location.getKey()) : Optional.empty();
  }

  protected Optional<Integer> status(final MessageSource messageSource, final String type) {
    final String status = this.getValue(messageSource, type, FIELD_STATUS);
    return isNotBlank(status) ? Optional.of(Integer.parseInt(status)) : Optional.empty();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Optional<String> template(final Location location) {
    return location != null ? this.template(location.getSource(), location.getKey()) : Optional.empty();
  }

  protected Optional<String> template(final MessageSource messageSource, final String type) {
    final String template = this.getValue(messageSource, type, FIELD_TEMPLATE);
    return isNotBlank(template) ? Optional.of(template) : Optional.empty();
  }

  protected String getValue(final MessageSource messageSource, final String type, final String field) {
    final Locale locale = this.locale != null ? this.locale : LocaleContextHolder.getLocale();
    return getValue(messageSource, type, field, locale);
  }

  protected static String getValue( //
    final MessageSource messageSource, //
    final String type, //
    final String field, //
    final Locale locale //
  ) {
    final String key = type + "." + field;
    return messageSource.getMessage(key, null, null, locale);
  }
}
