package com.yirendai.oss.lib.security.swagger;

import com.google.common.collect.Lists;

import com.yirendai.oss.boot.autoconfigure.AppProperties;
import com.yirendai.oss.boot.autoconfigure.AppSecurityProperties;
import com.yirendai.oss.lib.security.api.Security;

import org.springframework.beans.factory.annotation.Autowired;
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
public class AuthenticationTokenHeaderBuilderPlugin implements OperationBuilderPlugin {

  private final ParameterBuilder parameterBuilder = new ParameterBuilder();

  @Autowired
  private AppProperties appProperties;

  @Override
  public void apply(final OperationContext operationContext) {
    final AppSecurityProperties appSecurityProperties = this.appProperties.getSecurity();

    final String loginEndpoint = appSecurityProperties.getLoginProcessingUrl();
    // Get endpoint request mapping
    final String mapping = operationContext.requestMappingPattern();

    // Check if private api endpoint
    if (appSecurityProperties.getEnabled() && !loginEndpoint.equals(mapping)) {
      // Create auth header parameter
      final String description = "Authentication token (see " + loginEndpoint + ")";
      final List<Parameter> parameters = Lists.newArrayList( //
        this.parameterBuilder
          .parameterType("header")
          .name(Security.HEADER_AUTH_TOKEN)
          .modelRef(new ModelRef("string"))
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
