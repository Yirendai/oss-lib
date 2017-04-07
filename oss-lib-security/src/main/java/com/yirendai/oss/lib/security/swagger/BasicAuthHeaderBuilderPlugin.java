package com.yirendai.oss.lib.security.swagger;

import com.google.common.collect.Lists;

import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.boot.autoconfigure.AppSecurityProperties;
import com.yirendai.oss.boot.autoconfigure.AppType;
import com.yirendai.oss.lib.common.BasicAuthUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;

import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.swagger.common.SwaggerPluginSupport;

import java.util.List;

/**
 * Created by zhanghaolun on 16/11/1.
 */
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
public class BasicAuthHeaderBuilderPlugin implements OperationBuilderPlugin {

  private final ParameterBuilder parameterBuilder = new ParameterBuilder();

  @Autowired
  private AppProperties appProperties;
  @Autowired(required = false)
  private SecurityProperties securityProperties;

  @Override
  public void apply(final OperationContext operationContext) {
    final Boolean basicEnabled = this.securityProperties != null && this.securityProperties.getBasic().isEnabled();
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();

    final String loginEndpoint = appSecurityProperties.getLoginProcessingUrl();
    // Get endpoint request mapping
    final String mapping = operationContext.requestMappingPattern();

    // Create auth header parameter
    if ((basicEnabled || appSecurityProperties.getEnabled()) && !appProperties.getType().equals(AppType.RESOURCE)
        && !appProperties.getType().equals(AppType.TEMPLATE) && !loginEndpoint.equals(mapping)) {
      final String description = "Authorization: Basic <_your_token_>, " + //
        "echo \"Basic $(echo -ne \\\"$username:$password\\\" | base64)\"";
      final List<Parameter> parameters = Lists.newArrayList(
        this.parameterBuilder
          .parameterType("header")
          .name(BasicAuthUtils.BASIC_AUTH_HEADE_NAME)
          .modelRef(new ModelRef("string"))
          .defaultValue("Basic ")
          .description(description)
          .allowMultiple(false)
          .required(false)
          .build() //
      );

      // Add parameter to endpoint documentation
      operationContext.operationBuilder().parameters(parameters);
    }
  }

  @Override
  public boolean supports(final DocumentationType delimiter) {
    return DocumentationType.SWAGGER_2.equals(delimiter);
  }
}
