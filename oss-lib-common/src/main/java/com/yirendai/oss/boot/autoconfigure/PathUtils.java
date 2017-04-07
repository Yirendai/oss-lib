package com.yirendai.oss.boot.autoconfigure;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.util.AntPathMatcher;

import java.util.function.Predicate;

/**
 * Created by zhanghaolun on 16/11/3.
 */
public abstract class PathUtils {

  private PathUtils() {
  }

  /**
   * Any path satisfies this condition.
   *
   * @return predicate that is always true
   */
  public static Predicate<String> any() {
    return t -> true;
  }

  /**
   * No path satisfies this condition.
   *
   * @return predicate that is always false
   */
  public static Predicate<String> none() {
    return t -> false;
  }

  /**
   * Predicate that evaluates the supplied regular expression.
   *
   * @param pathRegex - regex
   * @return predicate that matches a particular regex
   */
  public static Predicate<String> regex(final String pathRegex) {
    return input -> input.matches(pathRegex);
  }

  /**
   * Predicate that evaluates the supplied ant pattern.
   *
   * @param antPattern - ant Pattern
   * @return predicate that matches a particular ant pattern
   */
  public static Predicate<String> ant(final String antPattern) {
    return input -> {
      AntPathMatcher matcher = new AntPathMatcher();
      return matcher.match(antPattern, input);
    };
  }

  public static Predicate<String> managementPaths(final String managementContextPath) {
    final Predicate<String> result;
    if (isNotBlank(managementContextPath)) {
      result = regex(managementContextPath + "/.*") //
        .or(ant(managementContextPath)) //
        .or(ant(managementContextPath + "" + ".json")) //
        .or(regex("/env(\\..+|/.*)?")) // environment-manager-mvc-endpoint@org.springframework.cloud.context
        .or(regex("/restart(\\..+|/.*)?")) // restart-mvc-endpoint@org.springframework.cloud.context
      ;
    } else {
      result = regex("/archaius(\\..+|/.*)?") // endpoint-mvc-adapter
        .or(regex("/autoconfig(\\..+|/.*)?")) //
        .or(regex("/beans(\\..+|/.*)?"))
        .or(regex("/configprops(\\..+|/.*)?"))
        .or(regex("/dump(\\..+|/.*)?"))
        .or(regex("/features(\\..+|/.*)?"))
        .or(regex("/info(\\..+|/.*)?"))
        .or(regex("/mappings(\\..+|/.*)?"))
        .or(regex("/trace(\\..+|/.*)?"))
        .or(regex("/env(\\..+|/.*)?")) // environment-mvc-endpoint,
        // environment-manager-mvc-endpoint@org.springframework.cloud.context
        .or(regex("/pause(\\..+|/.*)?")) // generic-postable-mvc-endpoint
        .or(regex("/refresh(\\..+|/.*)?"))
        .or(regex("/resume(\\..+|/.*)?"))
        .or(regex("/actuator(\\..+|/.*)?")) // hal-json-mvc-endpoint
        .or(regex("/health(\\..+|/.*)?")) // health-mvc-endpoint
        .or(regex("/heapdump(\\..+|/.*)?")) // heapdump-mvc-endpoint
        .or(regex("/jolokia(\\..+|/.*)?")) // jolokia-mvc-endpoint
        .or(regex("/logfile(\\..+|/.*)?")) // log-file-mvc-endpoint
        .or(regex("/metrics(\\..+|/.*)?")) // metrics-mvc-endpoint
        .or(regex("/restart(\\..+|/.*)?")) // restart-mvc-endpoint@org.springframework.cloud.context
        .or(regex("/shutdown(\\..+|/.*)?")) // shutdown-mvc-endpoint@org.springframework.boot.actuate
      ;
    }
    return result;
  }

  public static Boolean isManagementPath(final String managementContextPath, final String servletPath) {
    return managementPaths(managementContextPath).test(servletPath);
  }
}
