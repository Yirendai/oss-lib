package com.yirendai.oss.boot.autoconfigure;

/**
 * Application type.
 * Created by zhanghaolun on 16/8/17.
 */
public enum AppType {


  /**
   * RESTful API and template pages, with all authentication method.
   */
  MIXED,

  /**
   * RESTful resource application with token and oauth authentication method only.
   */
  RESOURCE,
  
  /**
   * RESTful application, no template pages, with all authentication method.
   */
  RESTFUL,

  /**
   * Template based application, no RESTful API, with all authentication method.
   */
  TEMPLATE
}
