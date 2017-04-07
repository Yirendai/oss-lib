package com.yirendai.oss.lib.swagger;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;

/**
 * Created by zhanghaolun on 16/11/1.
 */
public abstract class SwaggerUtils {

  private SwaggerUtils() {
  }

  public static ApiInfo apiInfo(final String title, final String description) {
    return new ApiInfoBuilder()
      //.contact(new Contact("haolun zhang", "", "haolunzhang@yirendai.com"))
      .description(description)
      //.license("Apache License Version 2.0")
      //.licenseUrl("https://github.com/IBM-Bluemix/news-aggregator/blob/master/LICENSE")
      //.termsOfServiceUrl("http://www-03.ibm.com/software/sla/sladb.nsf/sla/bm?Open")
      .title(title)
      //.version("1.0")
      .build();
  }
}
