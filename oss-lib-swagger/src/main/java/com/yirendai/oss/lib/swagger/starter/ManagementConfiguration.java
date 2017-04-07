package com.yirendai.oss.lib.swagger.starter;

import static com.google.common.base.Predicates.or;
import static com.yirendai.oss.lib.swagger.SwaggerUtils.apiInfo;
import static com.yirendai.oss.lib.swagger.starter.NoManagementConfiguration.MANAGEMENT_PATHS;

import com.google.common.base.Predicate;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.yirendai.oss.boot.autoconfigure.PathUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Optional;

/**
 * Created by zhanghaolun on 16/11/18.
 */
@ConditionalOnClass({ManagementServerProperties.class})
@Configuration
@Slf4j
public class ManagementConfiguration {

  public static final String DOCKET_MANAGEMENT = "docketManagement";

  @Autowired(required = false)
  private ManagementServerProperties managementServerProperties;

  @ConditionalOnMissingBean(name = DOCKET_MANAGEMENT)
  @Bean(name = DOCKET_MANAGEMENT)
  public Docket docketManagement() {
    final Docket docket = new Docket(DocumentationType.SWAGGER_2) //
      .apiInfo(apiInfo("spring-boot-starter-actuator", "spring boot's management endpoints")) //
      .groupName("management endpoints") //
      .select() //
      .apis(or(RequestHandlerSelectors.basePackage("org.springframework.boot.actuate"), //
        RequestHandlerSelectors.basePackage("org.springframework.cloud.context")))
      .paths(this.managementPaths())
      .build();

    final Optional<ResolvedType> modelResolvedError = modelResolvedError();
    return modelResolvedError.isPresent() ? docket.additionalModels(modelResolvedError.get()) : docket;
  }

  @Bean(name = MANAGEMENT_PATHS)
  public Predicate<String> managementPaths() {
    // TODO EnvironmentManagerMvcEndpoint @RequestParam Map<String, String> params issus
    // https://github.com/springfox/springfox/issues/1481
    final String managementContextPath = this.managementServerProperties != null ? //
      this.managementServerProperties.getContextPath() : "";
    return input -> PathUtils.managementPaths(managementContextPath).test(input);
  }

  static Optional<ResolvedType> modelResolvedError() {
    Optional<ResolvedType> result;
    try {
      result = Optional.ofNullable(new TypeResolver().resolve( //
        Class.forName("com.yirendai.oss.lib.errorhandle.api.ResolvedError")));
    } catch (final ClassNotFoundException ex) {
      log.trace("Swagger can't find class com.yirendai.oss.lib.errorhandle.api.ResolvedError," + //
        "so api will not show this info in swagger api document.", ex);
      result = Optional.empty();
    }
    return result;
  }
}
