package com.yirendai.oss.lib.common;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
public final class DiscoverableEnums<T extends Enum<T> & DiscoverableEnum<T>> {

  private final Map<Class<?>, Map<String, T>> type2TextConstantMap;
  private final Map<String, T> allTextConstantMap;

  private DiscoverableEnums(final String basePackage, final Class<T> type) {
    final Set<Class<T>> discoverableEnums = scanDiscoverableEnums(basePackage, type);
    final Map<Class<?>, Map<String, T>> typeToText2ConstantMap = newLinkedHashMap();
    final Map<String, T> text2ConstantAllInOneMap = newLinkedHashMap();
    final Map<String, Collection<String>> discoverableEnumTexts = newLinkedHashMap();
    discoverableEnums.forEach(discoverableEnum -> {
      final Map<String, T> text2ConstantMap = text2ConstantMap(discoverableEnum);
      typeToText2ConstantMap.put(discoverableEnum, text2ConstantMap);
      text2ConstantMap.entrySet().forEach(entry -> {
        final String text = entry.getKey();
        final T constant = entry.getValue();
        if (text2ConstantAllInOneMap.containsKey(text)) {
          throw new IllegalArgumentException("'" + text + "' of '" + constant + "' conflict with '"
            + text + "' of '" + text2ConstantAllInOneMap.get(text).getDeclaringClass() + "'");
        }
        text2ConstantAllInOneMap.put(text, constant);
      });
      discoverableEnumTexts.put(StringUtils.lowerCaseFirstChar(discoverableEnum.getSimpleName()),
        ImmutableList.copyOf(enumTexts(discoverableEnum)));
    });

    this.type2TextConstantMap = ImmutableMap.copyOf(typeToText2ConstantMap);
    this.allTextConstantMap = ImmutableMap.copyOf(text2ConstantAllInOneMap);
  }

  /**
   * for validator.
   *
   * @param type type
   * @param text text
   * @return contains
   */
  public boolean contains(final Class<?> type, final String text) {
    return this.type2TextConstantMap.get(type).get(text) != null; // may null pointer
  }

  /**
   * for String2DiscoverableEnumConverter.
   *
   * @param source text or name
   * @return enum instance
   */
  public T convert(final Object source) {
    final T result;
    if (source != null) {
      result = this.allTextConstantMap.get(source.toString());
    } else {
      result = null;
    }
    return result;
  }

  /**
   * for String2DiscoverableEnumConverter.
   *
   * @param source     text or name
   * @param targetType targetType
   * @return enum instance
   */
  @SuppressWarnings("unchecked")
  public Object convert(final Object source, final TypeDescriptor targetType) {
    final Object result;
    if (source != null) {
      result = this.parse((Class<T>) targetType.getType(), source.toString());
    } else {
      result = null;
    }
    return result;
  }

  /**
   * parse enum.
   *
   * @param targetType targetType
   * @param source     text or name
   * @return enum instance
   */
  public Optional<T> parse(final Class<T> targetType, final String source) {
    final String textOrName = source.trim().toUpperCase();
    final Optional<T> result = this.parseByText(targetType, textOrName);
    return result.isPresent() ? result : this.parseByName(targetType, textOrName);
  }

  Optional<T> parseByText(final Class<T> targetType, final String text) {
    return Optional.ofNullable(this.type2TextConstantMap.get(targetType).get(text)); // may be null
  }

  Optional<T> parseByName(final Class<T> targetType, final String name) {
    T result = null;
    try {
      final T found = Enum.valueOf(targetType, name);
      if (isNotBlank(found.getText())) {
        result = found;
      }
    } catch (final RuntimeException ignored) {
      // no-op
      log.info("error parseByName '{}' of type '{}'.", name, targetType, ignored);
    }
    return Optional.ofNullable(result);
  }

  static <T extends Enum<T> & DiscoverableEnum<?>> Map<String, T> text2ConstantMap(
    final Class<T> type) {
    final Map<String, T> map = newLinkedHashMap();
    for (final T constant : type.getEnumConstants()) {
      if (isNotBlank(constant.getText())) {
        map.put(constant.getText(), constant);
      }
    }
    return unmodifiableMap(map);
  }

  static <T extends Enum<T> & DiscoverableEnum<T>> Collection<String> enumTexts(
    final Class<T> type) {
    // Lambda error: Invalid receiver type class java.lang.Enum; not a subtype of implementation
    // type interface DiscoverableEnum
    final Set<String> result = Sets.newLinkedHashSet();
    // Note: must use for loop here!
    // lambda will trigger an 'Invalid receiver type class java.lang.Enum,
    // not a subtype of implementation type interface XXX' error;
    // see: https://github.com/orfjackal/retrolambda/issues/69
    for (final T tag : Arrays.asList(type.getEnumConstants())) {
      if (isNotBlank(tag.getText())) {
        result.add(tag.getText());
      }
    }
    return result;
  }

  static <T extends Enum<T> & DiscoverableEnum<T>> Set<Class<T>> scanDiscoverableEnums( //
    final String basePackage, final Class<T> type) {
    // InterfaceFilter(type, false) not working here.
    final TypeFilter includeFilter = new FileAndClasspathUtils.AssignableFilter(type, false, false);
    return FileAndClasspathUtils.scan(basePackage, includeFilter);
  }

  /**
   * scan.
   *
   * @param basePackage base package
   * @param type        type to find
   * @param <T>         enum type
   * @return found
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static <T extends Enum<T> & DiscoverableEnum<T>> DiscoverableEnums<T> discoverableEnums( //
    final String basePackage, final Class<?> type //
  ) {
    return new DiscoverableEnums(basePackage, type);
  }
}
