package com.yirendai.oss.boot.autoconfigure;

import org.springframework.beans.factory.annotation.Value;

/**
 * Created by zhanghaolun on 16/9/22.
 */
public class AppLogProperties {

  @Value("${log.flag:logTrace}")
  private String flag = "logTrace";
}
