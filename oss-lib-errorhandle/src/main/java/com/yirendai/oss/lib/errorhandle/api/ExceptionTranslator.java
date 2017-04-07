package com.yirendai.oss.lib.errorhandle.api;

import static com.google.common.base.Preconditions.checkNotNull;

import lombok.Getter;
import lombok.ToString;

import org.springframework.context.HierarchicalMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.web.context.request.RequestAttributes;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/**
 * ExceptionTranslator.
 *
 * <p>
 * Created by zhanghaolun on 16/8/9.
 * </p>
 */
public interface ExceptionTranslator {

  @ToString
  final class Location {

    /**
     * Order first.
     */
    public static final Comparator<Location> ORDER_FIRST_COMPARATOR = (o1, o2) -> {
      final int result;

      if (o1 != null && o2 != null) {
        final int sourceOrderResult = o1.sourceOrder.compareTo(o2.sourceOrder);
        final int levelResult = o1.level.compareTo(o2.level);
        result = sourceOrderResult != 0 ? sourceOrderResult : levelResult;
      } else if (o1 == null) {
        result = 1;
      } else {
        result = -1;
      }

      return result;
    };

    /**
     * Hierarchy first.
     */
    public static final Comparator<Location> HIERARCHY_FIRST_COMPARATOR = (o1, o2) -> {
      final int result;

      if (o1 != null && o2 != null) {
        final int sourceOrderResult = o1.sourceOrder.compareTo(o2.sourceOrder);
        final int levelResult = o1.level.compareTo(o2.level);
        result = levelResult != 0 ? levelResult : sourceOrderResult;
      } else if (o1 == null) {
        result = 1;
      } else {
        result = -1;
      }

      return result;
    };

    @Getter
    private final MessageSource source;
    @Getter
    private final Integer sourceOrder;
    @Getter
    private final String key;
    @Getter
    private final Integer level;

    public Location( //
      final MessageSource source, //
      final Integer sourceOrder, //
      final String key, //
      final Integer level //
    ) {
      this.source = checkNotNull(source);
      this.sourceOrder = checkNotNull(sourceOrder);
      this.key = checkNotNull(key);
      this.level = checkNotNull(level);
    }
  }

  @Deprecated
  static HierarchicalMessageSource rootMessageSource(final MessageSource messageSource) {
    final HierarchicalMessageSource result;
    if (messageSource instanceof HierarchicalMessageSource) {
      final MessageSource parent = ((HierarchicalMessageSource) messageSource).getParentMessageSource();
      result = parent != null ? //
        rootMessageSource(parent) //
        : (HierarchicalMessageSource) messageSource; //
    } else {
      result = null;
    }
    return result;
  }

  /**
   * locate info.
   *
   * @param throwable The exception to handle and get data from.
   * @return info location.
   */
  Optional<Location> find(Throwable throwable);

  /**
   * Translate exception and generates localized message.
   *
   * @param template         template
   * @param request          The current request attributes.
   * @param throwable        throwable
   * @param contextVariables The variables in context
   * @return A TranslateResult.
   */
  Optional<String> localizedMessage( //
    String template, //
    RequestAttributes request, //
    Throwable throwable, //
    Map<String, Serializable> contextVariables //
  );

  Optional<Integer> status(Location location);

  Optional<String> template(Location location);
}
