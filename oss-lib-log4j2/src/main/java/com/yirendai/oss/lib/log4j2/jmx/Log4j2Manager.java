package com.yirendai.oss.lib.log4j2.jmx;

import static com.google.common.base.Preconditions.checkNotNull;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@ManagedResource(//
  objectName = "ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator",
  description = "oss log4j extend Bean")
public class Log4j2Manager implements Log4j2ManagerMBean {

  private static final String EMPTY = "";

  @Override
  public void setLoggerLevel(final String loggerName, final String level) {
    if (loggerName != null && level != null) {
      final Level logLevel = Level.toLevel(level.trim(), null);
      if (logLevel != null) {
        this.setLoggerLevel(loggerName.trim(), logLevel);
      }
    }
  }

  private void setLoggerLevel(final String loggerName, final Level level) {
    checkNotNull(loggerName, "loggerName must not null");
    checkNotNull(level, "level must not null");

    final LoggerContext lc = LoggerContext.getContext(false);
    final Map<String, LoggerConfig> map = lc.getConfiguration().getLoggers();
    final LoggerConfig loggerConfig = map.get(loggerName);
    if (loggerConfig != null) {
      loggerConfig.setLevel(level);
      lc.updateLoggers();
    } else {
      final Logger logger = lc.getLogger(loggerName);
      if (logger != null) {
        logger.setLevel(level);
      }
    }
  }

  @Override
  public String getLoggerLevel(final String loggerName) {
    String retValue = EMPTY;

    if (loggerName != null) {
      final String loggerNameFin = loggerName.trim();
      final LoggerContext lc = LoggerContext.getContext(false);
      final Logger logger = lc.getLogger(loggerNameFin);
      if (logger != null && logger.getLevel() != null) {
        retValue = logger.getLevel().toString();
      }
    }

    return retValue;
  }

  @Override
  public List<String> getLoggerList() {
    final LoggerContext lc = (LoggerContext) LogManager.getContext(false);
    final List<String> strList = new ArrayList<>();

    final Collection<Logger> loggerCollection = lc.getLoggers();
    final List<Logger> loggerList = new ArrayList<>(loggerCollection);

    // 添加配置中的logger
    final Set<String> keySetConfig = lc.getConfiguration().getLoggers().keySet();
    for (final String keyConfig : keySetConfig) {
      if (!lc.hasLogger(keyConfig)) {
        final Logger logger = lc.getLogger(keyConfig);
        loggerList.add(logger);
      }
    }

    loggerList.sort(new LoggerComparator());

    for (final Logger logger : loggerList) {
      strList.add(logger.getName());
    }

    return strList;
  }

  @Override
  public String getLoggerEffectiveLevel(final String loggerName) {
    String retValue = EMPTY;
    if (loggerName != null) {
      final String loggerNameFin = loggerName.trim();

      final LoggerContext lc = LoggerContext.getContext(false);
      final Logger logger = lc.getLogger(loggerNameFin);
      if (logger != null) {
        retValue = logger.getLevel().toString();
      }
    }

    return retValue;
  }

  @Override
  public void setRootLoggerLevel(final String levelStr) {
    final LoggerContext lc = LoggerContext.getContext(false);
    final Logger logger = lc.getRootLogger();

    final Level logLevel = Level.toLevel(levelStr, null);
    if (logLevel != null) {
      logger.get().setLevel(logLevel);
      lc.updateLoggers();
    }
  }

  @Override
  public String getRootLoggerLevel() {
    final LoggerContext lc = LoggerContext.getContext(false);
    final Logger logger = lc.getRootLogger();

    String retValue = EMPTY;
    if (logger != null && logger.getLevel() != null) {
      retValue = logger.getLevel().toString();
    }

    return retValue;
  }

  public static final class LoggerComparator implements Comparator<Logger>, Serializable {

    private static final long serialVersionUID = 1074614190743916148L;

    @Override
    public int compare(final Logger l1, final Logger l2) {
      int retValue;
      if (l1.getName().equals(l2.getName())) {
        retValue = 0;
      } else {
        if (l1.getName().equals(LogManager.ROOT_LOGGER_NAME)) {
          retValue = -1;
        } else if (l2.getName().equals(LogManager.ROOT_LOGGER_NAME)) {
          retValue = 1;
        } else {
          retValue = l1.getName().compareTo(l2.getName());
        }
      }

      return retValue;
    }
  }
}
