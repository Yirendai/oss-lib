package com.yirendai.oss.lib.log4j2.jmx;

import java.util.List;

public interface Log4j2ManagerMBean {

  void setLoggerLevel(String loggerName, String levelStr);

  String getLoggerLevel(String loggerName);

  List<String> getLoggerList();

  String getLoggerEffectiveLevel(String loggerName);
  
  void setRootLoggerLevel(String levelStr);
  
  String getRootLoggerLevel();
}
