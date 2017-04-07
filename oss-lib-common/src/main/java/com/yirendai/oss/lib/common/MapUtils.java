package com.yirendai.oss.lib.common;

import java.util.Map;

public final class MapUtils {

  private MapUtils() {
  }

  /**
   * Puts entries from the {@code source} map into the {@code target} map, but without overriding
   * any existing entry in {@code target} map, i.e. put only if the key does not exist in the
   * {@code target} map.
   *
   * @param <K>    Key
   * @param <V>    Value
   * @param target The target map where to put new entries.
   * @param source The source map from which read the entries.
   */
  public static <K, V> void putAllIfAbsent(final Map<K, V> target, final Map<K, V> source) {
    source.entrySet().stream()
        .filter(entry -> !target.containsKey(entry.getKey()))
        .forEach(entry -> target.put(entry.getKey(), entry.getValue()));
  }
}
